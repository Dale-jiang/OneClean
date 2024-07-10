package com.kk.newcleanx.ui.clean

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.databinding.AcJunkScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.clean.vm.JunkScanningViewModel
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.startRotateAnim

class JunkScanningActivity : AllFilePermissionActivity<AcJunkScanningBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkScanningActivity::class.java))
        }
    }

    private val viewModel by viewModels<JunkScanningViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var isCompleted = false
    private var animator: ValueAnimator? = null

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@JunkScanningActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
            toolbar.tvTitle.text = getString(R.string.string_scanning)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { onBackClicked() }
            requestAllFilePermission {
                if (it) {
                    setStartAnim()
                    viewModel.getAllJunk()
                    startProgress()
                } else finish()
            }
        }

        initObserver()

    }

    private fun initObserver() {

        viewModel.apply {
            pathChaneObserver.observe(this@JunkScanningActivity) {
                binding.tvPath.text = it
            }

            itemChaneObserver.observe(this@JunkScanningActivity) { item ->

                binding.run {
                    when (item.junkType) {
                        JunkType.APP_CACHE -> tvCacheSize.text =
                            viewModel.junkDetailsList.filter { it.junkType == JunkType.APP_CACHE }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.LOG_FILES -> tvLogSize.text =
                            viewModel.junkDetailsList.filter { it.junkType == JunkType.LOG_FILES }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.TEMP_FILES -> tvTempSize.text =
                            viewModel.junkDetailsList.filter { it.junkType == JunkType.TEMP_FILES }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.AD_JUNK -> tvAdSize.text =
                            viewModel.junkDetailsList.filter { it.junkType == JunkType.AD_JUNK }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.APK_FILES -> tvApkSize.text =
                            viewModel.junkDetailsList.filter { it.junkType == JunkType.APK_FILES }.sumOf { it.fileSize }.formatStorageSize()
                    }

                    tvJunkSize.text = viewModel.junkDetailsList.sumOf { it.fileSize }.formatStorageSize()

                }
            }

            scanningCompletedObserver.observe(this@JunkScanningActivity) {
                binding.tvTip.text = getString(R.string.string_junk)
                isCompleted = true
                setStopAnim()
            }


        }

    }

    private fun setStartAnim() {
        binding.apply {
            ivCacheLoading.startRotateAnim()
            ivApkLoading.startRotateAnim()
            ivLogLoading.startRotateAnim()
            ivAdLoading.startRotateAnim()
            ivTempLoading.startRotateAnim()
        }
    }

    private fun setStopAnim() {
        binding.apply {
            ivCacheLoading.clearAnimation()
            ivCacheLoading.setImageResource(R.drawable.scanning_item_complete)

            ivApkLoading.clearAnimation()
            ivApkLoading.setImageResource(R.drawable.scanning_item_complete)

            ivLogLoading.clearAnimation()
            ivLogLoading.setImageResource(R.drawable.scanning_item_complete)

            ivAdLoading.clearAnimation()
            ivAdLoading.setImageResource(R.drawable.scanning_item_complete)

            ivTempLoading.clearAnimation()
            ivTempLoading.setImageResource(R.drawable.scanning_item_complete)
        }
    }


    private fun startProgress() {
        val startTime = System.currentTimeMillis()
        val maxTime = 10000L // 10 seconds
        val progressUpdateInterval = 50L // 100 milliseconds

        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = if (elapsedTime < maxTime) {
                    (elapsedTime.toFloat() / maxTime * 80).toInt()
                } else {
                    80
                }
                binding.progressBar.progress = progress
                if (isCompleted) {
                    animateProgressTo100()
                } else {
                    handler.postDelayed(this, progressUpdateInterval)
                }
            }
        })
    }

    private fun animateProgressTo100() {
        animator = ValueAnimator.ofInt(binding.progressBar.progress, 100)
        animator?.apply {
            duration = 500
            addUpdateListener { animation ->
                binding.progressBar.progress = animation.animatedValue as Int
            }
            start()
        }
    }

    private fun onBackClicked() {
        CustomAlertDialog(this).showDialog(title = getString(R.string.string_tips),
                                           message = getString(R.string.string_scanning_stop_tip),
                                           positiveButtonText = getString(R.string.string_ok),
                                           negativeButtonText = getString(R.string.string_cancel),
                                           onPositiveButtonClick = {
                                               it.dismiss()
                                               finish()
                                           },
                                           onNegativeButtonClick = {})
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.scanningJob?.cancel()
        handler.removeCallbacksAndMessages(null)
        animator?.cancel()
        animator = null
    }


}