package com.kk.newcleanx.ui.functions.clean

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.databinding.AcJunkScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.CleanResultActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.clean.vm.JunkScanningViewModel
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.startRotateAnim
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JunkScanningActivity : AllFilePermissionActivity<AcJunkScanningBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkScanningActivity::class.java))
        }
    }

    private val viewModel by viewModels<JunkScanningViewModel>()

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
                    startProgress { p ->
                        progressBar.progress = p
                    }
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
                val newList = ArrayList(viewModel.junkDetailsList)
                binding.run {
                    when (item.junkType) {
                        JunkType.APP_CACHE -> tvCacheSize.text =
                            newList.filter { it.junkType == JunkType.APP_CACHE }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.LOG_FILES -> tvLogSize.text =
                            newList.filter { it.junkType == JunkType.LOG_FILES }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.TEMP_FILES -> tvTempSize.text =
                            newList.filter { it.junkType == JunkType.TEMP_FILES }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.AD_JUNK -> tvAdSize.text =
                            newList.filter { it.junkType == JunkType.AD_JUNK }.sumOf { it.fileSize }.formatStorageSize()

                        JunkType.APK_FILES -> tvApkSize.text =
                            newList.filter { it.junkType == JunkType.APK_FILES }.sumOf { it.fileSize }.formatStorageSize()
                    }
                    tvJunkSize.text = newList.sumOf { it.fileSize }.formatStorageSize()
                }
            }

            scanningCompletedObserver.observe(this@JunkScanningActivity) {
                binding.tvTip.text = getString(R.string.string_junk)
                isCompleted = true
                setStopAnim()
                viewModel.createJunkDataList()
            }

            createJunkDataListObserver.observe(this@JunkScanningActivity) {
                lifecycleScope.launch {
                    while (binding.progressBar.progress < 100) delay(50L)
                    showFullAd {
                        if (it) {
                            JunkScanningResultActivity.start(this@JunkScanningActivity)
                            finish()
                        } else {
                            CleanResultActivity.start(this@JunkScanningActivity, CleanType.JunkType)
                            finish()
                        }
                    }
                }

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


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_scan_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_int"))

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@JunkScanningActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@JunkScanningActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@JunkScanningActivity)
                b.invoke()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.scanningJob?.cancel()
    }

}