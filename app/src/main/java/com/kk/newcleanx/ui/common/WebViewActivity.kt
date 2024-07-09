package com.kk.newcleanx.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.kk.newcleanx.data.local.BLANK_URL
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.databinding.AcWebviewBinding
import com.kk.newcleanx.ui.base.BaseActivity

class WebViewActivity : BaseActivity<AcWebviewBinding>() {

    companion object {
        fun start(context: Context, url: String) {
            context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra(INTENT_KEY, url)
            })
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            setWebView()
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        binding.webView.run {
            loadUrl(intent?.getStringExtra(INTENT_KEY) ?: BLANK_URL)
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    binding.toolbar.tvTitle.text = title
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                    binding.progressBar.isVisible = 100 != newProgress
                }
            }
        }
    }

}