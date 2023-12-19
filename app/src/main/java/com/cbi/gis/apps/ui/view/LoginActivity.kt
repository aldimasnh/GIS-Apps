package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.model.LoginModel
import com.cbi.gis.apps.data.repository.LoginRepository
import com.cbi.gis.apps.ui.viewModel.LoginViewModel
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import kotlinx.android.synthetic.main.activity_login.clParentLogin
import kotlinx.android.synthetic.main.activity_login.incEtPass
import kotlinx.android.synthetic.main.activity_login.incEtUser
import kotlinx.android.synthetic.main.activity_login.loadingLogin
import kotlinx.android.synthetic.main.activity_login.mbFinger
import kotlinx.android.synthetic.main.activity_login.mbLogin
import kotlinx.android.synthetic.main.activity_login.mcbRemember
import kotlinx.android.synthetic.main.activity_login.svParentLogin
import kotlinx.android.synthetic.main.activity_login.tvCreateAccount
import kotlinx.android.synthetic.main.activity_login.tvForgotLogin
import kotlinx.android.synthetic.main.edit_text_view.view.etTempLyt
import kotlinx.android.synthetic.main.edit_text_view.view.ilTempLyt

class LoginActivity : AppCompatActivity() {

    //update notification playstore
    private var appUpdate: AppUpdateManager? = null

    private var prefManager: PrefManager? = null
    private var user = ""
    private var pass = ""

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_login)
        AppUtils.fadeUpAnimation(clParentLogin)

        loadingLogin.visibility = View.VISIBLE
        val repository = LoginRepository(this, window, loadingLogin)
        viewModel =
            ViewModelProvider(this, LoginViewModel.Factory(repository))[LoginViewModel::class.java]
        viewModel.loginModel.observe(this) { result ->
            handleLoginResult(result)
        }

        prefManager = PrefManager(this)
        if (AppUtils.checkBiometricSupport(this) && prefManager!!.name.toString().isNotEmpty()) {
            mbFinger.visibility = View.VISIBLE
            biometricPrompt()
        }

        setViewLayout()
        initClick()

        appUpdate = AppUpdateManagerFactory.create(this)
        AppUtils.checkUpdateApp(this, appUpdate)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setViewLayout() {
        AppUtils.setupInputLayout(
            this,
            incEtUser.ilTempLyt,
            R.string.username,
            R.drawable.email,
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        )
        AppUtils.setupInputLayout(
            this,
            incEtPass.ilTempLyt,
            R.string.password,
            R.drawable.password,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            EditorInfo.IME_ACTION_DONE
        )

        if (prefManager!!.remember) {
            mcbRemember.isChecked = true
            if (prefManager!!.name.toString().isNotEmpty()) {
                incEtUser.etTempLyt.setText(prefManager!!.username, TextView.BufferType.SPANNABLE)
                incEtUser.etTempLyt.isEnabled = false
                incEtUser.etTempLyt.setTextColor(Color.BLACK)
                user = prefManager!!.username.toString()
            }
        } else {
            mcbRemember.isChecked = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClick() {
        tvForgotLogin.setOnClickListener {
            AlertDialogUtility.alertDialog(
                this,
                getString(R.string.caution),
                getString(R.string.desc_under_development),
                "warning.json"
            )
        }
        tvCreateAccount.setOnClickListener {
            AlertDialogUtility.alertDialog(
                this,
                getString(R.string.caution),
                getString(R.string.desc_under_development),
                "warning.json"
            )
        }
        mbFinger.setOnClickListener {
            biometricPrompt()
        }
        mbLogin.setOnClickListener {
            AppUtils.hideKeyboard(this)

            if (user.isEmpty() || pass.isEmpty()) {
                AppUtils.handleInput(this, user, incEtUser.ilTempLyt)
                AppUtils.handleInput(this, pass, incEtPass.ilTempLyt)
                AlertDialogUtility.alertDialog(
                    this,
                    getString(R.string.caution),
                    getString(R.string.desc_check_account),
                    "warning.json"
                )
            } else {
                AppUtils.handleInput(this, user, incEtUser.ilTempLyt)
                AppUtils.handleInput(this, pass, incEtPass.ilTempLyt)
                loadingLogin.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingLogin)
                viewModel.loginUser(user, AppUtils.convertMD5(pass))
            }
        }
        mcbRemember.setOnCheckedChangeListener { _, isChecked ->
            prefManager!!.remember = isChecked
        }

        svParentLogin.setOnTouchListener { _, _ -> true }
        AppUtils.handleEditorActionAndScroll(
            this,
            incEtUser.etTempLyt,
            incEtPass.etTempLyt,
            svParentLogin,
            "start"
        )
        AppUtils.handleEditorActionAndScroll(
            this,
            incEtPass.etTempLyt,
            incEtPass.etTempLyt,
            svParentLogin,
            "end"
        )

        AppUtils.handleTextChanges(incEtUser.etTempLyt) { user = it }
        AppUtils.handleTextChanges(incEtPass.etTempLyt) {
            incEtPass.etTempLyt.requestFocus()
            svParentLogin.smoothScrollTo(0, incEtPass.etTempLyt.bottom)
            pass = it
        }
        AppUtils.checkSoftKeyboard(this, clParentLogin) {
            svParentLogin.smoothScrollTo(0, svParentLogin.top)
        }
    }

    private fun biometricPrompt() {
        AppUtils.showBiometricPrompt(this, prefManager!!.name.toString()) {
            runOnUiThread {
                loadingLogin.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingLogin)
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun handleLoginResult(result: LoginModel) {
        when (result.statusCode) {
            1 -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            else -> {
                AppUtils.closeLoadingLayout(loadingLogin)
                AlertDialogUtility.alertDialog(
                    this,
                    getString(R.string.caution),
                    result.message ?: getString(R.string.failed_login),
                    "warning.json"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppUtils.inProgressUpdate(this, appUpdate)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialogUtility.withTwoActions(
            this,
            getString(R.string.yes),
            getString(R.string.caution),
            getString(R.string.desc_confirm1),
            "warning.json"
        ) {
            finishAffinity()
        }
    }
}