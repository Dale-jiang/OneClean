package com.kk.newcleanx.ui.common

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
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.junkCleanTimeTag
import com.kk.newcleanx.databinding.AcJunkCleanBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.clean.vm.JunkCleanViewModel
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class JunkCleanActivity : AllFilePermissionActivity<AcJunkCleanBinding>() {

    companion object {
        fun start(context: Context, type: CleanType, isFormGuide: Boolean = false) {
            context.startActivity(Intent(context, JunkCleanActivity::class.java).apply {
                putExtra(INTENT_KEY, type)
                putExtra("new_guide", isFormGuide)
            })
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val isFormGuide by lazy { intent?.getBooleanExtra("new_guide", false) ?: false }
    private val viewModel by viewModels<JunkCleanViewModel>()
    private var mProgress = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            val type = intent?.getSerializableExtra(INTENT_KEY) as? CleanType

            toolbar.tvTitle.text = ""
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            onBackPressedDispatcher.addCallback {
                if (btnContinue.isVisible.not()) {
                    onBackClicked()
                }
            }

            btnContinue.setOnClickListener {
                if (type != CleanType.BigFileType) {
                    CleanResultActivity.start(this@JunkCleanActivity, type, isFormGuide)
                }
                finish()
            }

            startProgress(endDuration = 600, minWaitTime = 3000) {
                mProgress = it
                binding.tvPercent.text = "${mProgress}%"
            }

            when (type) {
                CleanType.JunkType -> {
                    tvTip.text = getString(R.string.junk_cleaning)
                    viewModel.cleanJunk()
                }

                CleanType.EmptyFolderType -> {
                    tvTip.text = getString(R.string.empty_folders_cleaning)
                    viewModel.cleanEmptyFolders()
                }

                CleanType.BigFileType -> {
                    viewModel.cleanBigFiles()
                }

                else -> {}
            }

            viewModel.completeObserver.observe(this@JunkCleanActivity) {
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
                        if (type == CleanType.JunkType) {
                            junkCleanTimeTag = System.currentTimeMillis()
                        }

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
            if (ADManager.ocCleanIntLoader.canShow(this@JunkCleanActivity)) {
                ADManager.ocCleanIntLoader.showFullScreenAd(this@JunkCleanActivity, "oc_clean_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocCleanIntLoader.loadAd(this@JunkCleanActivity)
                b.invoke()
            }
        }

    }


}