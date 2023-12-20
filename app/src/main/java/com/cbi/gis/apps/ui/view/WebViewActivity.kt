package com.cbi.gis.apps.ui.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cbi.gis.apps.R
import com.cbi.gis.apps.utils.AlertDialogUtility
import com.cbi.gis.apps.utils.AppUtils
import kotlinx.android.synthetic.main.activity_webview.loadingWeb

@Suppress("DEPRECATION")
class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webviewGIS)

        webView.settings.allowFileAccess = true
        webView.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.javaScriptEnabled = true
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.loadUrl(AppUtils.dashboardServer)

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)

            val cookies: String? = CookieManager.getInstance().getCookie(url)
            if (cookies != null) {
                request.addRequestHeader("cookie", cookies)
            }

            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimetype)
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
        }
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
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
}