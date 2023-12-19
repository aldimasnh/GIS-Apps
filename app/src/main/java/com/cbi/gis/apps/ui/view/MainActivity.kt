package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.repository.DataJobTypeRepository
import com.cbi.gis.apps.data.repository.DataUnitRepository
import com.cbi.gis.apps.ui.viewModel.DataJobTypeViewModel
import com.cbi.gis.apps.ui.viewModel.DataUnitViewModel
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import kotlinx.android.synthetic.main.activity_main.clParentMain
import kotlinx.android.synthetic.main.activity_main.headerMain
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.activity_main.mainMenu1
import kotlinx.android.synthetic.main.activity_main.mainMenu2
import kotlinx.android.synthetic.main.activity_main.tvLastUpdate
import kotlinx.android.synthetic.main.card_menu.view.cvMenu1
import kotlinx.android.synthetic.main.card_menu.view.cvMenu2
import kotlinx.android.synthetic.main.card_menu.view.ivMenu1
import kotlinx.android.synthetic.main.card_menu.view.ivMenu2
import kotlinx.android.synthetic.main.card_menu.view.tvMenu1
import kotlinx.android.synthetic.main.card_menu.view.tvMenu2
import kotlinx.android.synthetic.main.header_apps.view.ivKeluar

class MainActivity : AppCompatActivity() {

    private lateinit var dataJobTypeViewModel: DataJobTypeViewModel
    private lateinit var dataUnitViewModel: DataUnitViewModel
    private var prefManager: PrefManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_main)
        AppUtils.fadeUpAnimation(clParentMain)

        initViewModel()

        loadingMain.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingMain)

        prefManager = PrefManager(this)
        if (prefManager!!.isFirstTimeLaunch) {
            handleSynchronizeData()
        } else {
            AppUtils.closeLoadingLayout(loadingMain)
        }

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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setViewLayout() {
        tvLastUpdate.text = if (prefManager!!.lastUpdate!!.isNotEmpty()) {
            prefManager!!.lastUpdate
        } else {
            getString(R.string.last_update1)
        }

        mainMenu1.ivMenu1.setImageResource(R.drawable.ic_form_64)
        mainMenu1.tvMenu1.text = getString(R.string.menu1)
        mainMenu1.ivMenu2.setImageResource(R.drawable.ic_upload_64)
        mainMenu1.tvMenu2.text = getString(R.string.menu2)
        mainMenu2.ivMenu1.setImageResource(R.drawable.ic_synchronize_64)
        mainMenu2.tvMenu1.text = getString(R.string.menu3)
        mainMenu2.ivMenu2.setImageResource(R.drawable.ic_dashboard_64)
        mainMenu2.tvMenu2.text = getString(R.string.menu4)
    }

    private fun initClick() {
        mainMenu1.cvMenu1.setOnClickListener {
            loadingMain.visibility = View.VISIBLE
            AppUtils.showLoadingLayout(this, window, loadingMain)

            val intent = Intent(this, DailyReportActivity::class.java)
            startActivity(intent)
        }
        mainMenu1.cvMenu2.setOnClickListener {
            loadingMain.visibility = View.VISIBLE
            AppUtils.showLoadingLayout(this, window, loadingMain)

            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
        mainMenu2.cvMenu1.setOnClickListener {
            handleSynchronizeData("yes")
        }
        mainMenu2.cvMenu2.setOnClickListener {
            AlertDialogUtility.alertDialog(
                this,
                getString(R.string.caution),
                getString(R.string.desc_under_development),
                "warning.json"
            )
        }
        headerMain.ivKeluar.setOnClickListener {
            AlertDialogUtility.withTwoActions(
                this,
                getString(R.string.yes),
                getString(R.string.caution),
                getString(R.string.desc_confirm3),
                "warning.json"
            ) {
                loadingMain.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingMain)

                prefManager!!.isFirstTimeLaunch = true
                prefManager!!.lastUpdate = ""
                prefManager!!.session = false
                prefManager!!.userid = ""
                prefManager!!.userno = ""
                prefManager!!.name = ""
                prefManager!!.username = ""
                prefManager!!.email = ""
                prefManager!!.password = ""
                prefManager!!.id_jabatan = 0
                prefManager!!.remember = false
                prefManager!!.md5Job = ""
                prefManager!!.md5Unit = ""

                dataJobTypeViewModel.deleteDataJobType()
                dataUnitViewModel.deleteDataUnit()

                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    private fun handleSynchronizeData(arg: String? = "") {
        if (AppUtils.checkConnectionDevice(this)) {
            if (arg!!.isNotEmpty()) {
                AlertDialogUtility.withTwoActions(
                    this,
                    getString(R.string.yes),
                    getString(R.string.caution),
                    getString(R.string.desc_confirm2),
                    "warning.json"
                ) {
                    loadingMain.visibility = View.VISIBLE
                    AppUtils.showLoadingLayout(this, window, loadingMain)
                    synchronizeData(arg)
                }
            } else {
                synchronizeData()
            }
        } else {
            AlertDialogUtility.withSingleAction(
                this,
                getString(R.string.try_again),
                getString(R.string.failed),
                getString(R.string.error_volley3),
                "error.json"
            ) {
                handleSynchronizeData(arg)
            }
        }
    }

    private fun synchronizeData(arg: String? = "") {
        AppUtils.synchronizeJobType(
            this,
            prefManager!!,
            dataJobTypeViewModel,
            dataUnitViewModel,
            loadingMain,
            arg
        )
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