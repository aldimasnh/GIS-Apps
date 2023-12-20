package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.model.DataDailyModel
import com.cbi.gis.apps.data.repository.DailyReportRepository
import com.cbi.gis.apps.data.repository.DataJobTypeRepository
import com.cbi.gis.apps.ui.adapter.UploadAdapter
import com.cbi.gis.apps.ui.viewModel.DailyReportViewModel
import com.cbi.gis.apps.ui.viewModel.DataJobTypeViewModel
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import com.cbi.gis.apps.utils.PrefManager
import kotlinx.android.synthetic.main.activity_upload.clParentUpload
import kotlinx.android.synthetic.main.activity_upload.clUploadData
import kotlinx.android.synthetic.main.activity_upload.fbUploadData
import kotlinx.android.synthetic.main.activity_upload.headerUpload
import kotlinx.android.synthetic.main.activity_upload.ivIconSortBy
import kotlinx.android.synthetic.main.activity_upload.ivNavLeft
import kotlinx.android.synthetic.main.activity_upload.ivNavRight
import kotlinx.android.synthetic.main.activity_upload.llSortUpload
import kotlinx.android.synthetic.main.activity_upload.loadingUpload
import kotlinx.android.synthetic.main.activity_upload.navbarLeft
import kotlinx.android.synthetic.main.activity_upload.navbarRight
import kotlinx.android.synthetic.main.activity_upload.rvListData
import kotlinx.android.synthetic.main.activity_upload.tvNavLeft
import kotlinx.android.synthetic.main.activity_upload.tvNavRight
import kotlinx.android.synthetic.main.activity_upload.tvTotalNow
import kotlinx.android.synthetic.main.activity_upload.tvTotalUpload
import kotlinx.android.synthetic.main.header_apps.view.ivBack
import kotlinx.android.synthetic.main.header_apps.view.ivKeluar
import kotlinx.android.synthetic.main.header_apps.view.ivLogoHeader
import kotlinx.android.synthetic.main.header_apps.view.tvHeaderLeft
import kotlinx.android.synthetic.main.header_apps.view.tvHeaderRight

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class UploadActivity : AppCompatActivity(), UploadAdapter.OnDeleteClickListener {

    private lateinit var dailyReportViewModel: DailyReportViewModel
    private lateinit var jobTypeViewModel: DataJobTypeViewModel
    private var uploadAdapter: UploadAdapter? = null

    private var totalList = 0
    private var firstScroll = false
    private var firstPage = true
    private var sortedBool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_upload)
        AppUtils.fadeUpAnimation(clParentUpload)

        initializeViewModelAdapter()
        setViewLayout()

        loadingUpload.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingUpload)

        dailyReportViewModel.getCountDailyReport()
        dailyReportViewModel.countItemResult.observe(this) {
            tvTotalNow.text = it
            totalList = it.toInt()
        }

        dailyReportViewModel.getCountArchiveDailyReport()
        dailyReportViewModel.countArchiveResult.observe(this) {
            tvTotalUpload.text = it
        }

        rvListData.layoutManager = LinearLayoutManager(this)
        rvListData.adapter = uploadAdapter

        rvListData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                try {
                    if (firstScroll && firstPage) {
                        if (dy <= 0) {
                            runOnUiThread {
                                fbUploadData.visibility = View.VISIBLE
                            }
                        } else {
                            runOnUiThread {
                                fbUploadData.visibility = View.GONE
                            }
                        }
                    }
                } finally {
                    firstScroll = true
                }
            }
        })

        if (firstPage) {
            try {
                loadDataFirstPage()
            } finally {
                firstPage = false
            }
        }

        initializeClick()
    }

    private fun initializeViewModelAdapter() {
        jobTypeViewModel = ViewModelProvider(
            this,
            DataJobTypeViewModel.Factory(application, DataJobTypeRepository(this))
        )[DataJobTypeViewModel::class.java]
        dailyReportViewModel = ViewModelProvider(
            this,
            DailyReportViewModel.Factory(
                application,
                this,
                DailyReportRepository(this),
                loadingUpload,
                PrefManager(this)
            )
        )[DailyReportViewModel::class.java]
        uploadAdapter = UploadAdapter(this, jobTypeViewModel, this)
    }

    private fun initializeClick() {
        headerUpload.ivBack.setOnClickListener {
            backToMain()
        }

        navbarLeft.setOnClickListener {
            if (firstPage) {
                try {
                    handleNavbarClick(true)
                } finally {
                    firstPage = false
                }
            }
        }

        navbarRight.setOnClickListener {
            if (!firstPage) {
                try {
                    handleNavbarClick(false)
                } finally {
                    firstPage = true
                }
            }
        }

        llSortUpload.setOnClickListener {
            if (totalList != 0) {
                sortedBool = !sortedBool
                if (!sortedBool) {
                    ivIconSortBy.scaleY = 1f
                    uploadAdapter!!.toggleSortingOrder()
                } else {
                    ivIconSortBy.scaleY = -1f
                    uploadAdapter!!.toggleSortingOrder()
                }
            }
        }

        fbUploadData.setOnClickListener {
            if (AppUtils.checkConnectionDevice(this)) {
                if (totalList != 0) {
                    AlertDialogUtility.withTwoActions(
                        this,
                        getString(R.string.yes),
                        getString(R.string.caution),
                        getString(R.string.desc_confirm6),
                        "warning.json"
                    ) {
                        loadingUpload.visibility = View.VISIBLE
                        AppUtils.showLoadingLayout(this, window, loadingUpload)

                        AlertDialogUtility.alertDialog(
                            this,
                            getString(R.string.caution),
                            getString(R.string.desc_info7),
                            "warning.json"
                        )

                        dailyReportViewModel.uploadToServer()
                    }
                } else {
                    AlertDialogUtility.alertDialog(
                        this,
                        getString(R.string.caution),
                        getString(R.string.last_update1),
                        "warning.json"
                    )
                }
            } else {
                AlertDialogUtility.alertDialog(
                    this,
                    getString(R.string.caution),
                    getString(R.string.error_volley3),
                    "warning.json"
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setViewLayout() {
        // Header
        headerUpload.tvHeaderLeft.text = getString(R.string.split_upload)
        headerUpload.tvHeaderLeft.typeface = ResourcesCompat.getFont(this, R.font.poppins_light)
        headerUpload.tvHeaderRight.text = " ${getString(R.string.split_data)}"
        headerUpload.tvHeaderRight.typeface = ResourcesCompat.getFont(this, R.font.poppins_bold)
        headerUpload.ivLogoHeader.visibility = View.GONE
        headerUpload.ivBack.visibility = View.VISIBLE
        headerUpload.ivKeluar.setImageResource(R.drawable.ic_upload_64_2)
        val layoutParams = headerUpload.ivKeluar.layoutParams
        layoutParams.width = 110
        layoutParams.height = 110
        headerUpload.ivKeluar.layoutParams = layoutParams
    }

    private fun handleNavbarClick(isFirstPage: Boolean) {
        loadingUpload.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingUpload)

        clUploadData.visibility = if (isFirstPage) View.VISIBLE else View.GONE
        ivNavLeft.imageTintList =
            ColorStateList.valueOf(this.resources.getColor(if (isFirstPage) R.color.colorPrimary else R.color.grey_default))
        tvNavLeft.setTextColor(ColorStateList.valueOf(resources.getColor(if (isFirstPage) R.color.black else R.color.grey_card)))
        ivNavRight.imageTintList =
            ColorStateList.valueOf(this.resources.getColor(if (isFirstPage) R.color.grey_default else R.color.colorPrimary))
        tvNavRight.setTextColor(ColorStateList.valueOf(resources.getColor(if (isFirstPage) R.color.grey_card else R.color.black)))

        if (isFirstPage) {
            loadDataFirstPage()
        } else {
            dailyReportViewModel.arcDailyReportList.observe(this) {
                uploadAdapter!!.submitList(it)
                AppUtils.closeLoadingLayout(loadingUpload)
            }
            dailyReportViewModel.loadArcDailyReport()
        }
    }

    private fun loadDataFirstPage() {
        dailyReportViewModel.dailyReportList.observe(this) {
            uploadAdapter!!.submitList(it)
            AppUtils.closeLoadingLayout(loadingUpload)
        }
        dailyReportViewModel.loadDailyReport()
    }

    private fun backToMain() {
        loadingUpload.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingUpload)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (!dailyReportViewModel.statusUpload()) {
            backToMain()
        }
    }

    override fun onResume() {
        super.onResume()
        if (dailyReportViewModel.statusUpload()) {
            loadingUpload.visibility = View.VISIBLE
            AppUtils.showLoadingLayout(this, window, loadingUpload)
        }
    }

    override fun onDeleteClick(item: DataDailyModel) {
        AlertDialogUtility.withTwoActions(
            this,
            getString(R.string.yes),
            getString(R.string.caution),
            getString(R.string.desc_confirm5),
            "warning.json"
        ) {
            dailyReportViewModel.deleteItemList(item.id.toString())
            dailyReportViewModel.deleteItemResult.observe(this) { isDeleted ->
                if (isDeleted) {
                    AlertDialogUtility.alertDialog(
                        this,
                        getString(R.string.success),
                        getString(R.string.desc_success_delete),
                        "success.json"
                    )
                } else {
                    AlertDialogUtility.alertDialog(
                        this,
                        getString(R.string.failed),
                        getString(R.string.desc_failed_delete),
                        "error.json"
                    )
                }
            }
        }
    }
}
