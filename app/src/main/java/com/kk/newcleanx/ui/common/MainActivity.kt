package com.kk.newcleanx.ui.common

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.APP_MANAGER
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.DEVICE_STATUS
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.data.local.SCAN_ANTIVIRUS
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.adapter.MainListAdapter
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.antivirus.AntivirusScanningActivity
import com.kk.newcleanx.ui.functions.appmanager.AppManagerActivity
import com.kk.newcleanx.ui.functions.bigfile.BigFileCleanActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.ui.functions.deviceinfo.DeviceInfoActivity
import com.kk.newcleanx.ui.functions.empty.EmptyFolderActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class MainActivity : AllFilePermissionActivity<AcMainBinding>() {

    companion object {
        var showBackAd = false
    }


    private var animator: Animator? = null
    private var adapter: MainListAdapter? = null
    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
        setStorageInfo()
        binding.ivSetting.setOnClickListener {
            showBackAd = true
            SettingActivity.start(this)
        }
        binding.btnScan.setOnClickListener {
            requestAllFilePermission {
                if (it) {
                    showBackAd = true
                    if (CommonUtils.checkIfCanClean()) {
                        JunkScanningActivity.start(this)
                    } else {
                        CleanResultActivity.start(this, CleanType.JunkType)
                    }
                }

            }
        }

        requestAllFilePermission {}

    }

    private fun initAdapter() {

        adapter = MainListAdapter(this) {
            when (it.type) {

                SCAN_ANTIVIRUS -> {
                    requestAllFilePermission { success ->
                        if (success) {
                            showBackAd = true
                            AntivirusScanningActivity.start(this)
                        }
                    }
                }

                BIG_FILE_CLEAN -> {
                    requestAllFilePermission { success ->
                        if (success) {
                            showBackAd = true
                            BigFileCleanActivity.start(this)
                        }
                    }
                }

                APP_MANAGER -> {
                    showBackAd = true
                    AppManagerActivity.start(this)
                }

                DEVICE_STATUS -> {
                    showBackAd = true
                    DeviceInfoActivity.start(this)
                }

                EMPTY_FOLDER -> {
                    requestAllFilePermission { success ->
                        if (success) {
                            showBackAd = true
                            EmptyFolderActivity.start(this)
                        }
                    }
                }
            }
        }

        val layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter!!.getItemViewType(position) == 0) {
                    2
                } else {
                    1
                }
            }
        }
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        startLoading()
        lifecycleScope.launch {
            delay(1000L)
            showMainNatAd()
        }

        if (showBackAd) {
            showBackAd = false
            showFullAd { }
        }

    }


    private fun startLoading() {
        loadingJob?.cancel()
        loadingJob = lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {
                val usePercent = setStorageInfo()
                progressBar.isIndeterminate = true
                delay(1960L)
                setCircleProgress(usePercent)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setStorageInfo(): Int {
        binding.apply {
            val total = CommonUtils.getTotalStorageByManager()
            val use = CommonUtils.getUsedStorageByManager()

            totalStorage.text = " / ${total.formatStorageSize()}"
            usedStorage.text = use.formatStorageSize()
            val usePercent = ((use / total.toFloat()) * 100).toInt()
            percent.text = "${usePercent}%"

            return usePercent
        }
    }

    private fun setCircleProgress(end: Int) {
        binding.progressBar.run {

            isIndeterminate = false

            val colorId = if (end > 75) {
                R.color.color_30e382
            } else if (end > 50) {
                R.color.color_2dd99f
            } else {
                R.color.primary
            }
            setIndicatorColor(ContextCompat.getColor(this@MainActivity, colorId))
            animator = ValueAnimator.ofInt(0, end).apply {
                duration = 500L * ceil(end.toDouble() / 25.toDouble()).toLong()
                addUpdateListener {
                    (it.animatedValue as? Int)?.apply {
                        progress = this
                    }
                }
            }
            animator?.start()
        }
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_back_int

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@MainActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@MainActivity, "oc_back_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@MainActivity)
                b.invoke()
            }
        }

    }


    private var ad: AdType? = null
    private fun showMainNatAd() {

        if (ADManager.isOverAdMax()) return
        ADManager.ocMainNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocMainNatLoader.canShow(this@MainActivity)) {
                    ad?.destroy()
                    ADManager.ocMainNatLoader.showNativeAd(this@MainActivity, binding.adFr, "nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null

        loadingJob?.cancel()
        loadingJob = null

        ad?.destroy()
    }

}