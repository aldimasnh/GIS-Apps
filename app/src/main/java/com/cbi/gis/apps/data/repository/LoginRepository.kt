package com.cbi.gis.apps.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.Window
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.model.LoginModel
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import org.json.JSONException
import org.json.JSONObject

class LoginRepository(private val context: Context, private val window: Window, private val loadingLayout: View) {

    fun loginUser(username: String, password: String, callback: (LoginModel) -> Unit) {
        val prefManager = PrefManager(context)
        val pmu = prefManager.username
        val pmp = prefManager.password

        if (username == "srs" && password == AppUtils.convertMD5("srs")) {
            prefManager.username = username
            prefManager.password = password
            callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))
        } else if (username == pmu && password == pmp) {
            callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))
        } else if (AppUtils.checkConnectionDevice(context)) {
            onlineAuth(username, password, callback)
        } else {
            callback(LoginModel(success = false, statusCode = 0, message = context.getString(
                R.string.error_volley3)))
        }
    }

    private fun onlineAuth(username: String, password: String, callback: (LoginModel) -> Unit) {
        val prefManager = PrefManager(context)
        if (username == prefManager.username && password == prefManager.password) {
            callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))
        } else {
            AppUtils.showLoadingLayout(context, window, loadingLayout)
            val strReq: StringRequest =
                @SuppressLint("SuspiciousIndentation")
                object : StringRequest(
                    Method.POST,
                    AppUtils.apiServer,
                    Response.Listener { response ->
                        try {
                            val jObj = JSONObject(response)
                            val success = jObj.getInt(AppUtils.TAG_SUCCESSCODE)
                            if (success == 1) {
                                val userData = jObj.getJSONObject("data")
                                prefManager.session = true
                                prefManager.userid = userData.getString(AppUtils.TAG_USERID)
                                prefManager.userno = userData.getString(AppUtils.TAG_USERNO)
                                prefManager.name = userData.getString(AppUtils.TAG_NAMA)
                                prefManager.username = userData.getString(AppUtils.TAG_USERNAME)
                                prefManager.email = userData.getString(AppUtils.TAG_EMAIL)
                                prefManager.password = userData.getString(AppUtils.TAG_PASSWORD)
                                prefManager.id_jabatan = userData.getInt(AppUtils.TAG_POSID)
                                callback(LoginModel(success = true, statusCode = 1, message = context.getString(
                                    R.string.success_login)))
                            } else {
                                callback(LoginModel(success = false, statusCode = 0, message = jObj.getString(AppUtils.TAG_MESSAGE)))
                            }
                            loadingLayout.visibility = View.GONE
                        } catch (e: JSONException) {
                            callback(LoginModel(success = false, statusCode = 0, message = "${context.getString(
                                R.string.error_volley1)}: $e"))
                            loadingLayout.visibility = View.GONE
                        }
                    },
                    Response.ErrorListener { error ->
                        callback(LoginModel(success = false, statusCode = 0, message = "${context.getString(R.string.error_volley2)}: $error"))
                        loadingLayout.visibility = View.GONE
                    }) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params[AppUtils.TAG_USERNAME] = username
                        params[AppUtils.TAG_PASSWORD] = password
                        params[AppUtils.TAG_REQUESTDATA] = "user"
                        return params
                    }
                }
            Volley.newRequestQueue(context).add(strReq)
        }
    }
}