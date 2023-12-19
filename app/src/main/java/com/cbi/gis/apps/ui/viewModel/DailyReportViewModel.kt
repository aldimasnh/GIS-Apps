package com.cbi.gis.apps.ui.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.database.DatabaseHelper
import com.cbi.gis.apps.data.model.DataDailyModel
import com.cbi.gis.apps.data.repository.DailyReportRepository
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
class DailyReportViewModel(
    application: Application,
    private val context: Context,
    private val dailyRepo: DailyReportRepository,
    private val loadingView: View? = null,
    private val prefManager: PrefManager? = null
) : AndroidViewModel(application) {

    private val _insertionResult = MutableLiveData<Boolean>()
    val insertionResult: LiveData<Boolean> get() = _insertionResult

    private val _deleteItemResult = MutableLiveData<Boolean>()
    val deleteItemResult: LiveData<Boolean> get() = _deleteItemResult

    private val _countItemResult = MutableLiveData<String>()
    val countItemResult: LiveData<String> get() = _countItemResult

    private val _countArchiveResult = MutableLiveData<String>()
    val countArchiveResult: LiveData<String> get() = _countArchiveResult

    private val _maintenceWlList = MutableLiveData<List<DataDailyModel>>()
    val maintenceWlList: LiveData<List<DataDailyModel>> get() = _maintenceWlList

    private val _arcMaintenceWlList = MutableLiveData<List<DataDailyModel>>()
    val arcMaintenceWlList: LiveData<List<DataDailyModel>> get() = _arcMaintenceWlList

    private val noDailyUpload = ArrayList<String>()
    private var uploading = false

    fun insertDailyReport(
        no_daily: String,
        id_jnsdr: Int,
        id_unit: Int,
        date: String,
        target: Int,
        progress: Int,
        keterangan: String,
        archive: Int
    ) {
        viewModelScope.launch {
            try {
                val dataDailyReport = DataDailyModel(
                    0, no_daily, id_jnsdr, id_unit, date, target, progress, keterangan, archive
                )
                val isInserted = dailyRepo.insertDailyReport(dataDailyReport)
                _insertionResult.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertionResult.value = false
            }
        }
    }

    fun getCountDailyReport() {
        viewModelScope.launch {
            try {
                _countItemResult.value = dailyRepo.getCountDailyReport().toString()
            } catch (e: Exception) {
                e.printStackTrace()
                _countItemResult.value = "0"
            }
        }
    }

    fun getCountArchiveDailyReport() {
        viewModelScope.launch {
            try {
                _countArchiveResult.value = dailyRepo.getCountDailyReport(1).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                _countArchiveResult.value = "0"
            }
        }
    }

    fun loadDailyReport() {
        viewModelScope.launch {
            val dataDailyReport = withContext(Dispatchers.IO) {
                dailyRepo.getAllDailyReport()
            }
            _maintenceWlList.value = dataDailyReport
        }
    }

    fun loadArcDailyReport() {
        viewModelScope.launch {
            val dataDailyReport = withContext(Dispatchers.IO) {
                dailyRepo.getAllDailyReport(1)
            }
            _arcMaintenceWlList.value = dataDailyReport
        }
    }

    fun deleteItemList(id: String) {
        viewModelScope.launch {
            try {
                val isDeleted = dailyRepo.deleteItem(id)
                _deleteItemResult.value = isDeleted
                getCountDailyReport()
                loadDailyReport()
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteItemResult.value = false
            }
        }
    }

    fun statusUpload(): Boolean = uploading

    fun uploadToServer() {
        noDailyUpload.clear()
        val dailyReportList = dailyRepo.getAllDailyReport()
        val noDailyList = dailyReportList.map { it.no_daily }
        var completedRequests = 0

        for (i in noDailyList.indices) {
            val postRequest: StringRequest = object : StringRequest(
                Method.POST, AppUtils.mainServer,
                Response.Listener { response ->
                    try {
                        val jObj = JSONObject(response)

                        val messageCheckData = try {
                            jObj.getString(AppUtils.TAG_MESSAGE)
                        } catch (e: Exception) {
                            e.toString()
                        }
                        val successResponseCheckData = try {
                            jObj.getInt(AppUtils.TAG_SUCCESSCODE)
                        } catch (e: Exception) {
                            0
                        }

                        if (successResponseCheckData == 2) {
                            noDailyUpload.add(noDailyList[i])
                        }

                        Log.d(AppUtils.LOG_UPLOAD, "messageCheckData: $messageCheckData")
                    } catch (e: JSONException) {
                        Log.e(AppUtils.LOG_UPLOAD, "Failed to parse server response: ${e.message}")
                    } finally {
                        completedRequests++

                        if (completedRequests == noDailyList.size) {
                            uploading = true
                            uploadAsyncDataServer()
                        }
                    }
                },
                Response.ErrorListener {
                    Log.e(AppUtils.LOG_UPLOAD, "Terjadi kesalahan koneksi: $it")

                    completedRequests++

                    if (completedRequests == noDailyList.size) {
                        uploadToServer()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params[AppUtils.TAG_USERNAME] = prefManager!!.username.toString()
                    params[AppUtils.TAG_PASSWORD] = prefManager.password.toString()
                    params[AppUtils.TAG_REQUESTDATA] = "check"
                    params[DatabaseHelper.DB_NODAILY] = noDailyList[i]
                    return params
                }
            }
            val queue = Volley.newRequestQueue(context)
            queue.cache.clear()
            queue.add(postRequest)
        }
    }

    private fun uploadAsyncDataServer() {
        val dailyReportList = dailyRepo.getAllDailyReport()
        for (i in noDailyUpload.indices) {
            dailyReportList.map { data ->
                if (noDailyUpload[i] == data.no_daily) {
                    val params = mapOf(
                        AppUtils.TAG_USERNAME to prefManager!!.username.toString(),
                        AppUtils.TAG_PASSWORD to prefManager.password.toString(),
                        AppUtils.TAG_REQUESTDATA to "post",
                        DatabaseHelper.DB_NODAILY to data.no_daily,
                        DatabaseHelper.DB_IDJNSDR to data.id_jnsdr.toString(),
                        DatabaseHelper.DB_IDUNIT to data.id_unit.toString(),
                        "id_user" to prefManager.userid.toString(),
                        DatabaseHelper.DB_DATE to data.date,
                        DatabaseHelper.DB_TARGET to data.target.toString(),
                        DatabaseHelper.DB_PROGRESS to data.progress.toString(),
                        DatabaseHelper.DB_KETERANGAN to data.keterangan,
                        "no_user" to prefManager.userno.toString()
                    )

                    AppUtils.uploadDataRows(context, AppUtils.mainServer, params, object :
                        AppUtils.UploadCallback {
                        override fun onUploadComplete(
                            message: String,
                            success: Int
                        ) {
                            if (success == 1) {
                                if (data.archive == 0) {
                                    if (dailyRepo.updateArchiveDailyReport(data.id.toString())) {
                                        getCountArchiveDailyReport()
                                        getCountDailyReport()
                                        loadDailyReport()

                                        Log.d(AppUtils.LOG_UPLOAD, "Success archive!")
                                    } else {
                                        Log.e(AppUtils.LOG_UPLOAD, "Failed archive!")
                                    }
                                }
                            }
                        }
                    })
                }
            }

            if (i == noDailyUpload.indices.last) {
                uploading = false
                AppUtils.closeLoadingLayout(loadingView!!)
                AlertDialogUtility.alertDialog(
                    context,
                    context.getString(R.string.success),
                    context.getString(R.string.desc_info6),
                    "success.json"
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val context: Context,
        private val dailyRepo: DailyReportRepository,
        private val loadingView: View? = null,
        private val prefManager: PrefManager? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyReportViewModel::class.java)) {
                return DailyReportViewModel(
                    application,
                    context,
                    dailyRepo,
                    loadingView,
                    prefManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}