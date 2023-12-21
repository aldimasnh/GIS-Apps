package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cbi.gis.apps.R
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import kotlinx.android.synthetic.main.activity_webview.loadingWeb
import kotlinx.android.synthetic.main.activity_webview.webviewGIS

class WebViewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_webview)

        val settings = webviewGIS.settings
        settings.javaScriptEnabled = true
        webviewGIS.webViewClient = WebViewClient()
        webviewGIS.loadUrl(AppUtils.dashboardServer)
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webviewGIS.canGoBack()) {
            webviewGIS.goBack()
        } else {
            AlertDialogUtility.withTwoActions(
                this,
                getString(R.string.yes),
                getString(R.string.caution),
                getString(R.string.desc_confirm7),
                "warning.json"
            ) {
                loadingWeb.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingWeb)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webviewGIS.onPause()
    }
}