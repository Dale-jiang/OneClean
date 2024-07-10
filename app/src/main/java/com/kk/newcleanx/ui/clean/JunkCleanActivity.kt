package com.kk.newcleanx.ui.clean

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.junkCleanTimeTag
import com.kk.newcleanx.databinding.AcJunkCleanBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.clean.vm.JunkCleanViewModel
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JunkCleanActivity : BaseActivity<AcJunkCleanBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkCleanActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<JunkCleanViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var isCompleted = false
    private var animator: ValueAnimator? = null
    private var mProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            toolbar.tvTitle.text = ""
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            onBackPressedDispatcher.addCallback {
                if (btnContinue.isVisible.not()) {
                    onBackClicked()
                }
            }

            btnContinue.setOnClickListener {}

            startProgress()
            viewModel.cleanJunk()

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

    @SuppressLint("SetTextI18n")
    private fun startProgress() {
        val startTime = System.currentTimeMillis()
        val maxTime = 10000L
        val progressUpdateInterval = 50L

        handler.post(object : Runnable {

            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                mProgress = if (elapsedTime < maxTime) {
                    (elapsedTime.toFloat() / maxTime * 80).toInt()
                } else {
                    80
                }
                binding.tvPercent.text = "${mProgress}%"
                if (isCompleted && elapsedTime > 2000) {
                    animateProgressTo100(mProgress)
                } else {
                    handler.postDelayed(this, progressUpdateInterval)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun animateProgressTo100(progress: Int) {
        animator = ValueAnimator.ofInt(progress, 100)
        animator?.apply {
            duration = 600
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                binding.tvPercent.text = "${value}%"
                mProgress = value
            }
            start()
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