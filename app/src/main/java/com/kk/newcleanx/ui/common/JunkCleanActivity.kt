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
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.junkCleanTimeTag
import com.kk.newcleanx.databinding.AcJunkCleanBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.clean.vm.JunkCleanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class JunkCleanActivity : AllFilePermissionActivity<AcJunkCleanBinding>() {

    companion object {
        fun start(context: Context, type: CleanType) {
            context.startActivity(Intent(context, JunkCleanActivity::class.java).apply {
                putExtra(INTENT_KEY, type)
            })
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

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
                CleanResultActivity.start(this@JunkCleanActivity)
                finish()
            }

            startProgress(endDuration = 600, minWaitTime = 2000) {
                mProgress = it
                binding.tvPercent.text = "${mProgress}%"
            }

            // TODO:
            if (type == CleanType.JunkType) {
                tvTip.text = getString(R.string.junk_cleaning)
                viewModel.cleanJunk()
            } else if (type == CleanType.EmptyFolderType) {
                tvTip.text = getString(R.string.empty_folders_cleaning)
                viewModel.cleanEmptyFolders()
            }

            viewModel.completeObserver.observe(this@JunkCleanActivity) {
                lifecycleScope.launch {

                    isCompleted = true

                    while (mProgress < 100) delay(50L)

                    viewLottie.isVisible = false
                    viewLottie.cancelAnimation()
                    ivComplete.isVisible = true
                    btnContinue.isVisible = true
                    tvTip.isVisible = false
                    tvFinished.isVisible = true
                    tvPercent.isVisible = false
                    toolbar.ivBack.isInvisible = true
                    junkCleanTimeTag = System.currentTimeMillis()
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

}