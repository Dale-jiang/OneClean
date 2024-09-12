package com.kk.newcleanx.ui.functions.duplicatefile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.databinding.AcDuplicateFileCleanBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.duplicatefile.vm.DuplicateFileCleanViewModel
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuplicateFileCleanActivity : AllFilePermissionActivity<AcDuplicateFileCleanBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DuplicateFileCleanActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<DuplicateFileCleanViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.tvTitle.text = getString(R.string.duplicate_files)
        //    recyclerView.adapter = adapter

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    showFullAd {
                        clLoading.isVisible = false
                        viewLottie.cancelAnimation()
                    }
                }
            }
            isCompleted = true

            btnClean.setOnClickListener {
                JunkCleanActivity.start(this@DuplicateFileCleanActivity, CleanType.EmptyFolderType)
                finish()
            }

            ivScanBack.setOnClickListener {
                finish()
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }


    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_scan_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_int"))
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@DuplicateFileCleanActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@DuplicateFileCleanActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@DuplicateFileCleanActivity)
                b.invoke()
            }
        }
    }

}