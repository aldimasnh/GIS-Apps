package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.repository.DailyReportRepository
import com.cbi.gis.apps.data.repository.DataJobTypeRepository
import com.cbi.gis.apps.data.repository.DataUnitRepository
import com.cbi.gis.apps.ui.viewModel.DailyReportViewModel
import com.cbi.gis.apps.ui.viewModel.DataJobTypeViewModel
import com.cbi.gis.apps.ui.viewModel.DataUnitViewModel
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_daily.clParentDaily
import kotlinx.android.synthetic.main.activity_daily.headerDaily
import kotlinx.android.synthetic.main.activity_daily.incEtInfoDaily
import kotlinx.android.synthetic.main.activity_daily.incEtJobDaily
import kotlinx.android.synthetic.main.activity_daily.incEtProgressDaily
import kotlinx.android.synthetic.main.activity_daily.incEtTargetDaily
import kotlinx.android.synthetic.main.activity_daily.incEtTglDaily
import kotlinx.android.synthetic.main.activity_daily.incEtUnitDaily
import kotlinx.android.synthetic.main.activity_daily.mbSaveDaily
import kotlinx.android.synthetic.main.activity_daily.svParentDaily
import kotlinx.android.synthetic.main.dropdown_view.view.actTempLyt
import kotlinx.android.synthetic.main.dropdown_view.view.ddTempLyt
import kotlinx.android.synthetic.main.edit_text_view.view.etTempLyt
import kotlinx.android.synthetic.main.edit_text_view.view.ilTempLyt
import kotlinx.android.synthetic.main.header_apps.view.ivBack
import kotlinx.android.synthetic.main.header_apps.view.ivKeluar
import kotlinx.android.synthetic.main.header_apps.view.ivLogoHeader
import kotlinx.android.synthetic.main.header_apps.view.tvHeaderLeft
import kotlinx.android.synthetic.main.header_apps.view.tvHeaderRight
import java.util.Calendar

class DailyReportActivity : AppCompatActivity() {

    private lateinit var dataJobTypeViewModel: DataJobTypeViewModel
    private lateinit var dataUnitViewModel: DataUnitViewModel
    private lateinit var dailyReportViewModel: DailyReportViewModel
    private var prefManager: PrefManager? = null

    private var editTextList: List<TextInputEditText>? = null
    private var dateDaily: Calendar? = null
    private var target = 0
    private var progress = 0
    private var idJobType = 0
    private var idUnit = 0
    private var information = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_daily)
        AppUtils.fadeUpAnimation(clParentDaily)

        initViewModel()
        prefManager = PrefManager(this)
        editTextList = listOf(
            incEtTargetDaily.etTempLyt,
            incEtProgressDaily.etTempLyt,
            incEtInfoDaily.etTempLyt
        )

        setViewLayout()
        initClick()
    }

    private fun initViewModel() {
        dataJobTypeViewModel = ViewModelProvider(
            this,
            DataJobTypeViewModel.Factory(application, DataJobTypeRepository(this))
        )[DataJobTypeViewModel::class.java]
        dataUnitViewModel = ViewModelProvider(
            this,
            DataUnitViewModel.Factory(application, DataUnitRepository(this))
        )[DataUnitViewModel::class.java]
        dailyReportViewModel = ViewModelProvider(
            this,
            DailyReportViewModel.Factory(application, this, DailyReportRepository(this))
        )[DailyReportViewModel::class.java]
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun setViewLayout() {
        // Header
        headerDaily.tvHeaderLeft.text = getString(R.string.form2)
        headerDaily.tvHeaderRight.text = " ${getString(R.string.form1)}"
        headerDaily.tvHeaderRight.typeface =
            ResourcesCompat.getFont(this, R.font.poppins_bolditalic)
        headerDaily.ivLogoHeader.visibility = View.GONE
        headerDaily.ivBack.visibility = View.VISIBLE
        headerDaily.ivKeluar.setImageResource(R.drawable.gis_white)
        val layoutParams = headerDaily.ivKeluar.layoutParams
        layoutParams.width = 150
        layoutParams.height = 150
        headerDaily.ivKeluar.layoutParams = layoutParams

        // Form Daily
        incEtTglDaily.etTempLyt.focusable = View.NOT_FOCUSABLE
        incEtTargetDaily.etTempLyt.filters = arrayOf(android.text.InputFilter.LengthFilter(3))
        incEtProgressDaily.etTempLyt.filters = arrayOf(android.text.InputFilter.LengthFilter(3))
        AppUtils.setupInputLayout(
            this,
            incEtTglDaily.ilTempLyt,
            R.string.date,
            R.drawable.ic_date,
            InputType.TYPE_NULL
        )
        AppUtils.setupInputLayout(
            this,
            incEtJobDaily.ddTempLyt,
            R.string.job_type,
            R.drawable.ic_job,
            InputType.TYPE_NULL
        )
        AppUtils.setupInputLayout(
            this,
            incEtTargetDaily.ilTempLyt,
            R.string.target,
            R.drawable.ic_target,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        )
        AppUtils.setupInputLayout(
            this,
            incEtProgressDaily.ilTempLyt,
            R.string.progress,
            R.drawable.ic_progress,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
            EditorInfo.IME_ACTION_DONE
        )
        AppUtils.setupInputLayout(
            this,
            incEtUnitDaily.ddTempLyt,
            R.string.unit,
            R.drawable.ic_measure,
            InputType.TYPE_NULL
        )
        AppUtils.setupInputLayout(
            this,
            incEtInfoDaily.ilTempLyt,
            R.string.information,
            R.drawable.ic_information,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
            EditorInfo.IME_ACTION_DONE
        )
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initClick() {
        svParentDaily.setOnTouchListener { _, _ -> true }

        val calendar = Calendar.getInstance()
        val dateNow = Calendar.getInstance()
        dateNow.set(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        incEtTglDaily.etTempLyt.setOnClickListener {
            AppUtils.hideKeyboard(this)
            svParentDaily.smoothScrollTo(0, svParentDaily.top)

            val initialYear = dateDaily?.get(Calendar.YEAR) ?: calendar.get(Calendar.YEAR)
            val initialMonth = dateDaily?.get(Calendar.MONTH) ?: calendar.get(Calendar.MONTH)
            val initialDay =
                dateDaily?.get(Calendar.DAY_OF_MONTH) ?: calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)

                    incEtTglDaily.etTempLyt.setText(
                        "$selectedYear-${
                            AppUtils.setPadNumbers(
                                (selectedMonth + 1).toString(),
                                2
                            )
                        }-${AppUtils.setPadNumbers(selectedDay.toString(), 2)}"
                    )
                    dateDaily = selectedDate
                },
                initialYear,
                initialMonth,
                initialDay
            )

            datePickerDialog.datePicker.minDate = dateNow.timeInMillis
            datePickerDialog.show()
        }

        dataJobTypeViewModel.dataJobTypeList.observe(this) { data ->
            val idJobArr = data.map { it.id }.toTypedArray()

            setupAutoCompleteTextView(
                incEtJobDaily,
                data.map { it.nama }.toTypedArray()
            ) { parent, _, position, _ ->
                Log.d("cekData", "job: " + parent.getItemAtPosition(position).toString())
                idJobType = idJobArr[position]
            }
        }
        dataJobTypeViewModel.loadDataJobType()

        dataUnitViewModel.dataUnitList.observe(this) { data ->
            val idUnitArr = data.map { it.id }.toTypedArray()

            setupAutoCompleteTextView(
                incEtUnitDaily,
                data.map { it.nama }.toTypedArray()
            ) { parent, _, position, _ ->
                Log.d("cekData", "unit: " + parent.getItemAtPosition(position).toString())
                idUnit = idUnitArr[position]
            }
        }
        dataUnitViewModel.loadDataUnit()

        editTextList?.forEach { editText ->
            AppUtils.handleEditorActionAndScroll(
                this,
                editText,
                when (editText) {
                    incEtTargetDaily.etTempLyt -> incEtProgressDaily.etTempLyt
                    else -> editText
                },
                svParentDaily,
                when (editText) {
                    incEtTargetDaily.etTempLyt -> "start"
                    else -> "end"
                }
            )

            AppUtils.handleTextChanges(editText) {
                when (editText) {
                    incEtTargetDaily.etTempLyt -> target = try {
                        it.toInt()
                    } catch (e: Exception) {
                        0
                    }

                    incEtProgressDaily.etTempLyt -> progress = try {
                        it.toInt()
                    } catch (e: Exception) {
                        0
                    }

                    else -> information = it
                }
                editText.requestFocus()
                svParentDaily.smoothScrollTo(
                    0,
                    editText.bottom + if (editText == incEtInfoDaily.etTempLyt) 150 else 0
                )
            }
        }

        AppUtils.checkSoftKeyboard(this, clParentDaily) {
            svParentDaily.smoothScrollTo(0, svParentDaily.top)
        }

        mbSaveDaily.setOnClickListener {
            val fixDated = try {
                "${dateDaily!!.get(Calendar.YEAR)}-${
                    AppUtils.setPadNumbers(
                        (dateDaily!!.get(
                            Calendar.MONTH
                        ) + 1).toString(), 2
                    )
                }-${
                    AppUtils.setPadNumbers(dateDaily!!.get(Calendar.DAY_OF_MONTH).toString(), 2)
                }"
            } catch (e: Exception) {
                ""
            }

            Log.d("cekData", "dateDaily: $fixDated")
            Log.d("cekData", "no_daily: ${AppUtils.generateNoDailyReport("23")}")
            Log.d("cekData", "target: $target")
            Log.d("cekData", "progress: $progress")
            Log.d("cekData", "idJobType: $idJobType")
            Log.d("cekData", "idUnit: $idUnit")
            Log.d("cekData", "info: $information")

            if (fixDated.isEmpty() || idJobType == 0 || target == 0 || progress == 0 || idUnit == 0 || information.isEmpty()) {
                AppUtils.handleInput(this, fixDated, incEtTglDaily.ilTempLyt)
                AppUtils.handleInput(this, idJobType.toString(), incEtJobDaily.ddTempLyt)
                AppUtils.handleInput(this, target.toString(), incEtTargetDaily.ilTempLyt)
                AppUtils.handleInput(this, progress.toString(), incEtProgressDaily.ilTempLyt)
                AppUtils.handleInput(this, idUnit.toString(), incEtUnitDaily.ddTempLyt)
                AppUtils.handleInput(this, information, incEtInfoDaily.ilTempLyt)

                AlertDialogUtility.alertDialog(
                    this,
                    getString(R.string.caution),
                    getString(R.string.desc_info5),
                    "warning.json"
                )
            } else {
                AppUtils.handleInput(this, fixDated, incEtTglDaily.ilTempLyt)
                AppUtils.handleInput(this, idJobType.toString(), incEtJobDaily.ddTempLyt)
                AppUtils.handleInput(this, target.toString(), incEtTargetDaily.ilTempLyt)
                AppUtils.handleInput(this, progress.toString(), incEtProgressDaily.ilTempLyt)
                AppUtils.handleInput(this, idUnit.toString(), incEtUnitDaily.ddTempLyt)
                AppUtils.handleInput(this, information, incEtInfoDaily.ilTempLyt)

                AlertDialogUtility.withTwoActions(
                    this,
                    getString(R.string.yes),
                    getString(R.string.caution),
                    getString(R.string.desc_save_data),
                    "warning.json"
                ) {
                    dailyReportViewModel.insertDailyReport(
                        no_daily = AppUtils.generateNoDailyReport(prefManager!!.userid.toString()),
                        id_jnsdr = idJobType,
                        id_unit = idUnit,
                        date = fixDated,
                        target = target,
                        progress = progress,
                        keterangan = information,
                        archive = 0
                    )
                    dailyReportViewModel.insertionResult.observe(this) { isSuccess ->
                        if (isSuccess) {
                            AlertDialogUtility.alertDialogAction(
                                this,
                                getString(R.string.success),
                                getString(R.string.desc_success),
                                "success.json"
                            ) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            AlertDialogUtility.alertDialog(
                                this,
                                getString(R.string.failed),
                                getString(R.string.desc_failed),
                                "error.json"
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAutoCompleteTextView(
        view: View,
        listArray: Array<String>? = null,
        onItemClickListener: AdapterView.OnItemClickListener
    ) {
        val adapterItems =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listArray!!)
        view.actTempLyt.setAdapter(adapterItems)

        val bottomScreenHeight = AppUtils.getBottomScreenHeight(
            windowManager,
            window
        ) - if (view == incEtJobDaily) 900 else 1450
        view.actTempLyt.dropDownHeight = bottomScreenHeight

        view.actTempLyt.setOnTouchListener { _, event ->
            AppUtils.hideKeyboard(this)
            svParentDaily.smoothScrollTo(0, svParentDaily.top)
            if (event.action == MotionEvent.ACTION_UP) {
                view.actTempLyt.showDropDown()
            }
            false
        }
        view.ddTempLyt.setEndIconOnClickListener(null)
        view.actTempLyt.onItemClickListener = onItemClickListener
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialogUtility.withTwoActions(
            this,
            getString(R.string.yes),
            getString(R.string.caution),
            getString(R.string.desc_confirm4),
            "warning.json"
        ) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}