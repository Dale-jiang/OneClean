package com.kk.newcleanx.ui.functions.duplicatefile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcJunkCleanBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.duplicatefile.vm.FileDeleteViewModel
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuplicateFileDeleteActivity : AllFilePermissionActivity<AcJunkCleanBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DuplicateFileDeleteActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<FileDeleteViewModel>()
    private var mProgress = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            tvTip.text = "duplicate files cleaning..."
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            onBackPressedDispatcher.addCallback {
                if (btnContinue.isVisible.not()) {
                    onBackClicked()
                }
            }

            btnContinue.setOnClickListener {
                setResult(RESULT_OK)
                finish()
            }

            startProgress(endDuration = 600, minWaitTime = 3000) {
                mProgress = it
                binding.tvPercent.text = "${mProgress}%"
            }

            viewModel.cleanDuplicateFiles()

            viewModel.completeObserver.observe(this@DuplicateFileDeleteActivity) {
                lifecycleScope.launch {

                    isCompleted = true

                    while (mProgress < 100) delay(50L)

                    showFullAd {
                        viewLottie.isVisible = false
                        viewLottie.cancelAnimation()
                        ivComplete.isVisible = true
                        btnContinue.isVisible = false
                        tvTip.isVisible = false
                        tvFinished.isVisible = true
                        tvPercent.isVisible = false
                        toolbar.ivBack.isInvisible = true

                        lifecycleScope.launch {
                            delay(600)
                            btnContinue.performClick()
                        }
                    }

                }
            }

        }
    }


    private fun onBackClicked() {
        CustomAlertDialog(this).showDialog(title = getString(R.string.string_tips),
            message = getString(R.string.string_cleaning_stop_tip),
            positiveButtonText = getString(R.string.string_ok),
            negativeButtonText = getString(R.string.string_cancel),
            onPositiveButtonClick = {
                it.dismiss()
                setResult(RESULT_OK)
                finish()
            },
            onNegativeButtonClick = {})
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_clean_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_clean_int"))

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocCleanIntLoader.canShow(this@DuplicateFileDeleteActivity)) {
                ADManager.ocCleanIntLoader.showFullScreenAd(this@DuplicateFileDeleteActivity, "oc_clean_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocCleanIntLoader.loadAd(this@DuplicateFileDeleteActivity)
                b.invoke()
            }
        }

    }


}