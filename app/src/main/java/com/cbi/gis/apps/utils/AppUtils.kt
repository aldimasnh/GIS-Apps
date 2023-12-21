@file:Suppress("DEPRECATION")

package com.cbi.gis.apps.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LifecycleOwner
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.gis.apps.R
import com.cbi.gis.apps.ui.viewModel.DataJobTypeViewModel
import com.cbi.gis.apps.ui.viewModel.DataUnitViewModel
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_view.view.blurLoadView
import kotlinx.android.synthetic.main.loading_view.view.lottieLoadAnimate
import kotlinx.android.synthetic.main.loading_view.view.overlayLoadView
import org.json.JSONException
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object AppUtils {

    const val dashboardServer = "http://192.168.1.15:8000/dashboard-gis/"
    const val apiServer = "http://192.168.1.15:8000/simonitoring/mobileapi"

    const val TAG_REQUESTDATA = "data"
    const val TAG_SUCCESSCODE = "code"
    const val TAG_MESSAGE = "message"
    const val TAG_USERID = "id_user"
    const val TAG_USERNO = "no_user"
    const val TAG_NAMA = "nama"
    const val TAG_EMAIL = "email"
    const val TAG_USERNAME = "username"
    const val TAG_PASSWORD = "password"
    const val TAG_POSID = "id_group"
    const val TAG_MD5 = "md5data"
    const val TAG_MD5APP = "md5app"

    private const val LOG_DATA_JOB_TYPE = "jobTypeLog"
    private const val LOG_DATA_UNIT = "unitLog"
    const val LOG_UPLOAD = "uploadLog"

    fun uploadDataRows(
        context: Context,
        urlInsert: String,
        params: Map<String, String>,
        callback: UploadCallback
    ) {
        var messageInsert: String
        var successResponseInsert = 0

        val postRequest: StringRequest = object : StringRequest(
            Method.POST, urlInsert,
            Response.Listener { response ->
                try {
                    val jObj = JSONObject(response)
                    messageInsert = try {
                        jObj.getString(TAG_MESSAGE)
                    } catch (e: Exception) {
                        e.toString()
                    }
                    successResponseInsert = try {
                        jObj.getInt(TAG_SUCCESSCODE)
                    } catch (e: Exception) {
                        0
                    }
                    Log.d(
                        LOG_UPLOAD,
                        "upload data -- m: $messageInsert, s: $successResponseInsert"
                    )

                    callback.onUploadComplete(messageInsert, successResponseInsert)
                } catch (e: JSONException) {
                    val error = context.getString(R.string.error_volley1) + ": $e"
                    messageInsert = error
                    Log.e(LOG_UPLOAD, error)

                    callback.onUploadComplete(messageInsert, successResponseInsert)
                }
            },
            Response.ErrorListener {
                val error = context.getString(R.string.error_volley2) + ": $it"
                messageInsert = error
                Log.e(LOG_UPLOAD, error)

                callback.onUploadComplete(messageInsert, successResponseInsert)
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.cache.clear()
        queue.add(postRequest)
    }

    fun formatDate(inputDateStr: String): String {
        val inputFormat = SimpleDateFormat("yyMMdd.HHmm", Locale("id", "ID"))
        val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(inputDateStr)
        return outputFormat.format(date!!)
    }

    fun generateNoDailyReport(userid: String): String {
        val dateFormat = SimpleDateFormat("yyMMdd.HHmmss", Locale.getDefault())
        val currentDate = Date()
        val dateResult = dateFormat.format(currentDate)

        return dateResult.substring(
            0,
            11
        ) + ".DLY." + dateResult.takeLast(2) + setPadNumbers(userid, 3)
    }

    fun setPadNumbers(str: String, length: Int): String {
        return str.padStart(length, '0')
    }

    fun handleEditorActionAndScroll(
        context: Context,
        editText: TextInputEditText,
        nextEditText: TextInputEditText,
        scrollView: ScrollView,
        str: String,
        view: View? = null
    ): Boolean {
        editText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT -> {
                    if (view != null) {
                        view.visibility = View.VISIBLE
                    }

                    if (str == "next") {
                        nextEditText.requestFocus()
                        scrollView.smoothScrollTo(0, nextEditText.bottom)
                        true
                    } else {
                        scrollView.smoothScrollTo(0, 0)
                        hideKeyboard(context as Activity)
                        true
                    }
                }

                else -> false
            }
        }

        return true
    }

    fun handleTextChanges(
        editText: TextInputEditText,
        onDataChange: (String) -> Unit
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onDataChange.invoke(editable?.toString() ?: "")
            }
        })
    }

    fun getBottomScreenHeight(windowManager: WindowManager, window: Window): Int {
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getRealSize(size)
        val screenHeight = size.y
        val contentViewTop = window.decorView.top
        return screenHeight - contentViewTop
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupInputLayout(
        context: Context,
        layout: TextInputLayout,
        hintResId: Int,
        iconResId: Int,
        inputType: Int = InputType.TYPE_TEXT_VARIATION_PERSON_NAME or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
        imeType: Int = EditorInfo.IME_ACTION_NEXT
    ) {
        layout.hint = context.getString(hintResId)
        layout.startIconDrawable = context.resources.getDrawable(iconResId, null)
        layout.editText?.inputType = inputType
        layout.editText?.imeOptions = imeType
    }

    fun synchronizeJobType(
        context: Context,
        prefManager: PrefManager,
        dataJobTypeVm: DataJobTypeViewModel,
        dataUnitVm: DataUnitViewModel,
        loaderView: View,
        update: String? = ""
    ) {
        if (update!!.isNotEmpty()) {
            AlertDialogUtility.alertDialog(
                context,
                context.getString(R.string.caution),
                context.getString(R.string.desc_info1),
                "warning.json"
            )
        }

        val strReq: StringRequest =
            @SuppressLint("SetTextI18n")
            object : StringRequest(
                Method.POST,
                apiServer,
                Response.Listener { response ->
                    try {
                        val jObj = JSONObject(response)
                        when (jObj.getInt(TAG_SUCCESSCODE)) {
                            1 -> {
                                Log.d(LOG_DATA_JOB_TYPE, jObj.getString(TAG_MESSAGE))

                                synchronizeUnit(
                                    context,
                                    prefManager,
                                    dataUnitVm,
                                    loaderView,
                                    update
                                )
                            }

                            2 -> {
                                Log.d(LOG_DATA_JOB_TYPE, jObj.getString(TAG_MESSAGE))

                                dataJobTypeVm.deleteDataJobType()
                                val dataListJobType = jObj.getJSONArray("data")
                                for (i in 0 until dataListJobType.length()) {
                                    val jsonObject = dataListJobType.getJSONObject(i)
                                    dataJobTypeVm.insertDataJobType(
                                        id = jsonObject.getInt("id_jnsdr"),
                                        nama = jsonObject.getString("nm_jnsdr")
                                    )
                                }

                                dataJobTypeVm.insertionResult.observe(
                                    context as LifecycleOwner
                                ) { isSuccess ->
                                    if (isSuccess) {
                                        Log.d(LOG_DATA_JOB_TYPE, jObj.getString(TAG_MESSAGE))
                                        prefManager.md5Job = jObj.getString(TAG_MD5)
                                    } else {
                                        Log.d(LOG_DATA_JOB_TYPE, jObj.getString(TAG_MESSAGE))
                                        dataJobTypeVm.deleteDataJobType()
                                    }
                                }

                                synchronizeUnit(
                                    context,
                                    prefManager,
                                    dataUnitVm,
                                    loaderView,
                                    update
                                )
                            }

                            else -> {
                                Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))

                                synchronizeUnit(
                                    context,
                                    prefManager,
                                    dataUnitVm,
                                    loaderView,
                                    update
                                )
                            }
                        }
                    } catch (e: JSONException) {
                        Log.d(
                            LOG_DATA_JOB_TYPE, "${
                                context.getString(
                                    R.string.error_volley1
                                )
                            }: $e"
                        )
                        e.printStackTrace()

                        AlertDialogUtility.withSingleAction(
                            context,
                            context.getString(R.string.try_again),
                            context.getString(R.string.failed),
                            context.getString(R.string.desc_failed_download),
                            "error.json"
                        ) {
                            synchronizeJobType(
                                context,
                                prefManager,
                                dataJobTypeVm,
                                dataUnitVm,
                                loaderView,
                                update
                            )
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(
                        LOG_DATA_JOB_TYPE,
                        "${context.getString(R.string.error_volley2)}: $error"
                    )

                    AlertDialogUtility.withSingleAction(
                        context,
                        context.getString(R.string.try_again),
                        context.getString(R.string.failed),
                        context.getString(R.string.desc_failed_download),
                        "error.json"
                    ) {
                        synchronizeJobType(
                            context,
                            prefManager,
                            dataJobTypeVm,
                            dataUnitVm,
                            loaderView,
                            update
                        )
                    }
                }) {

                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params[TAG_USERNAME] = prefManager.username.toString()
                    params[TAG_PASSWORD] = prefManager.password.toString()
                    params[TAG_REQUESTDATA] = "job"
                    params[TAG_MD5APP] = prefManager.md5Job.toString()
                    return params
                }
            }

        strReq.retryPolicy = DefaultRetryPolicy(
            90000,  // Socket timeout in milliseconds (30 seconds)
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(strReq)
    }

    private fun synchronizeUnit(
        context: Context,
        prefManager: PrefManager,
        dataUnitVm: DataUnitViewModel,
        loaderView: View,
        update: String? = ""
    ) {
        val strReq: StringRequest =
            @SuppressLint("SimpleDateFormat")
            object : StringRequest(
                Method.POST,
                apiServer,
                Response.Listener { response ->
                    try {
                        val jObj = JSONObject(response)
                        val success = jObj.getInt(TAG_SUCCESSCODE)

                        if (success == 1) {
                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))

                            if (update!!.isNotEmpty()) {
                                AlertDialogUtility.alertDialog(
                                    context,
                                    context.getString(R.string.success),
                                    context.getString(R.string.desc_info2),
                                    "success.json"
                                )
                            }
                        } else if (success == 2) {
                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))

                            dataUnitVm.deleteDataUnit()
                            val dataListUnit = jObj.getJSONArray("data")
                            for (i in 0 until dataListUnit.length()) {
                                val jsonObject = dataListUnit.getJSONObject(i)
                                dataUnitVm.insertDataUnit(
                                    id = jsonObject.getInt("id_satuan"),
                                    nama = jsonObject.getString("nm_satuan")
                                )
                            }

                            dataUnitVm.insertionResult.observe(
                                context as LifecycleOwner
                            ) { isSuccess ->
                                if (isSuccess) {
                                    Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
                                    prefManager.md5Unit = jObj.getString(TAG_MD5)
                                } else {
                                    Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
                                    dataUnitVm.deleteDataUnit()
                                }
                            }

                            if (update!!.isNotEmpty()) {
                                AlertDialogUtility.alertDialog(
                                    context,
                                    context.getString(R.string.success),
                                    context.getString(R.string.desc_info3),
                                    "success.json"
                                )
                            }
                        } else {
                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
                        }

                        val dateNow =
                            SimpleDateFormat("dd MMM yyyy HH:mm").format(Calendar.getInstance().time)
                                .toString()
                        prefManager.lastUpdate = dateNow

                        if (prefManager.isFirstTimeLaunch) {
                            prefManager.isFirstTimeLaunch = false
                        }

                        closeLoadingLayout(loaderView)
                    } catch (e: JSONException) {
                        Log.d(
                            LOG_DATA_UNIT, "${
                                context.getString(
                                    R.string.error_volley1
                                )
                            }: $e"
                        )
                        e.printStackTrace()

                        AlertDialogUtility.withSingleAction(
                            context,
                            context.getString(R.string.try_again),
                            context.getString(R.string.failed),
                            context.getString(R.string.desc_failed_download),
                            "error.json"
                        ) {
                            synchronizeUnit(context, prefManager, dataUnitVm, loaderView, update)
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(
                        LOG_DATA_UNIT,
                        "${context.getString(R.string.error_volley2)}: $error"
                    )

                    AlertDialogUtility.withSingleAction(
                        context,
                        context.getString(R.string.try_again),
                        context.getString(R.string.failed),
                        context.getString(R.string.desc_failed_download),
                        "error.json"
                    ) {
                        synchronizeUnit(context, prefManager, dataUnitVm, loaderView, update)
                    }
                }) {

                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params[TAG_USERNAME] = prefManager.username.toString()
                    params[TAG_PASSWORD] = prefManager.password.toString()
                    params[TAG_REQUESTDATA] = "unit"
                    params[TAG_MD5APP] = prefManager.md5Unit.toString()
                    return params
                }
            }

        strReq.retryPolicy = DefaultRetryPolicy(
            90000,  // Socket timeout in milliseconds (30 seconds)
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(strReq)
    }

    fun convertMD5(plaintext: String): String {
        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("MD5")
        md.update(plaintextBytes)
        val md5HashBytes = md.digest()
        return try {
            BigInteger(1, md5HashBytes).toString(16).padStart(32, '0')
        } catch (e: Exception) {
            plaintext
        }
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: return
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun checkSoftKeyboard(context: Context, view: View, function: () -> Unit) {
        var isKeyboardVisible = false
        val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val newHeight = view.height
            if (newHeight != 0) {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false
                    function()
                } else {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val isAcceptingText = imm.isAcceptingText
                    val activity = context as Activity
                    val focusedView = activity.currentFocus
                    val isKeyboardOpenAndActive = focusedView != null && isAcceptingText
                    if (isKeyboardOpenAndActive) {
                        isKeyboardVisible = true
                    }
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
    }

    fun checkBiometricSupport(context: Context): Boolean {
        when (BiometricManager.from(context).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                return BiometricManager.from(context)
                    .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                return false
            }

            else -> {
                return false
            }
        }
    }

    fun showBiometricPrompt(context: Context, nameUser: String, successCallback: () -> Unit) {
        val executor = Executors.newSingleThreadExecutor()

        val biometricPrompt = BiometricPrompt(
            context as AppCompatActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    successCallback.invoke()
                }
            })

        val textWelcome = context.getString(R.string.welcome_back)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                "${
                    textWelcome.substring(0, 1)
                        .toUpperCase(Locale.getDefault()) + textWelcome.substring(1).toLowerCase(
                        Locale.getDefault()
                    )
                } $nameUser."
            )
            .setSubtitle(context.getString(R.string.subtitle_prompt))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun checkUpdateApp(context: Context, appUpdate: AppUpdateManager? = null) {
        appUpdate?.appUpdateInfo?.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true)
                    .build()

                appUpdate.startUpdateFlowForResult(
                    updateInfo,
                    context as Activity,
                    appUpdateOptions,
                    100
                )
            }
        }
    }

    fun inProgressUpdate(context: Context, appUpdate: AppUpdateManager? = null) {
        appUpdate?.appUpdateInfo?.addOnSuccessListener { updateInfo ->
            if (updateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdate.completeUpdate()
            } else if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdate.startUpdateFlowForResult(
                    updateInfo,
                    AppUpdateType.IMMEDIATE,
                    context as Activity,
                    100
                )
            }
        }
    }

    fun checkConnectionDevice(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val con = try {
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state
        } catch (e: Exception) {
            NetworkInfo.State.DISCONNECTED
        }

        return con === NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state === NetworkInfo.State.CONNECTED
    }

    private fun blurViewLayout(context: Context, window: Window, blurView: BlurView) {
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground: Drawable? = decorView.background
        blurView.setupWith(rootView, RenderScriptBlur(context))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(1f)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showLoadingLayout(context: Context, window: Window, loadingView: View) {
        loadingView.lottieLoadAnimate.visibility = View.VISIBLE
        loadingView.overlayLoadView.visibility = View.VISIBLE
        loadingView.overlayLoadView.setOnTouchListener { _, _ -> true }
        blurViewLayout(context, window, loadingView.blurLoadView)
    }

    fun closeLoadingLayout(loaderView: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            loaderView.visibility = View.GONE
        }, 500)
    }

    fun fadeUpAnimation(view: View) {
        YoYo.with(Techniques.FadeIn)
            .duration(1500)
            .playOn(view)
    }

    fun transparentStatusNavBar(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    interface UploadCallback {
        fun onUploadComplete(message: String, success: Int)
    }
}