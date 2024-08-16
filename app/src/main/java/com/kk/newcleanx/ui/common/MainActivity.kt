package com.kk.newcleanx.ui.common

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.kk.newcleanx.data.local.KEY_NOTICE_FUNCTION
import com.kk.newcleanx.data.local.NoticeType
import com.kk.newcleanx.data.local.SCAN_ANTIVIRUS
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.data.local.showNotificationPerDialogTime
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.adapter.MainListAdapter
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.antivirus.AntivirusScanningActivity
import com.kk.newcleanx.ui.functions.appmanager.AppManagerActivity
import com.kk.newcleanx.ui.functions.bigfile.BigFileCleanActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.ui.functions.deviceinfo.DeviceInfoActivity
import com.kk.newcleanx.ui.functions.empty.EmptyFolderActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.hasNotificationPermission
import com.kk.newcleanx.utils.CommonUtils.isAtLeastAndroid13
import com.kk.newcleanx.utils.CommonUtils.isAtLeastAndroid8
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.showAntivirusNotice
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Suppress("DEPRECATION")
class MainActivity : AllFilePermissionActivity<AcMainBinding>() {

    companion object {
        var showBackAd = false
        fun start(context: Context, noticeType: NoticeType?) {
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                noticeType?.apply {
                    putExtra(KEY_NOTICE_FUNCTION, noticeType)
                }
            })
        }
    }

    private var animator: Animator? = null
    private var adapter: MainListAdapter? = null
    private var loadingJob: Job? = null

    private var noticeType: NoticeType? = null
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (hasNotificationPermission()) {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "yes"))
        } else {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "no"))
        }
    }

    private val notificationSetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isToSettings = false
        if (hasNotificationPermission()) {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "yes"))
        } else {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "no"))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        noticeType = intent.getParcelableExtra(KEY_NOTICE_FUNCTION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TbaHelper.eventPost("home_page")
        noticeType = intent?.getParcelableExtra(KEY_NOTICE_FUNCTION)

        initAdapter()
        setStorageInfo()
        binding.ivSetting.setOnClickListener {
            showBackAd = true
            SettingActivity.start(this)
        }
        binding.btnScan.setOnClickListener {

            TbaHelper.eventPost("ho_clean")

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

    }


    private fun initAdapter() {

        adapter = MainListAdapter(this) {
            when (it.type) {

                SCAN_ANTIVIRUS -> {
                    showAntivirusNotice { res ->
                        if (res) {
                            requestAllFilePermission { success ->
                                if (success) {
                                    showBackAd = true
                                    TbaHelper.eventPost("antivirus_scan", hashMapOf("mg_source" to "home"))
                                    AntivirusScanningActivity.start(this)
                                }
                            }
                        }
                    }
                }

                BIG_FILE_CLEAN -> {
                    TbaHelper.eventPost("ho_big")
                    requestAllFilePermission { success ->
                        if (success) {
                            showBackAd = true
                            BigFileCleanActivity.start(this)
                        }
                    }
                }

                APP_MANAGER -> {
                    TbaHelper.eventPost("ho_appmanager")
                    showBackAd = true
                    AppManagerActivity.start(this)
                }

                DEVICE_STATUS -> {
                    TbaHelper.eventPost("ho_device")
                    showBackAd = true
                    DeviceInfoActivity.start(this)
                }

                EMPTY_FOLDER -> {
                    TbaHelper.eventPost("ho_empty")
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

        if (showBackAd && noticeType == null) {
            showBackAd = false
            showFullAd { }
        }

        checkNoticeJump()
    }

    private fun checkNoticeJump() {
        if (noticeType != null) {
            requestAllFilePermission {
                if (it) {
                    when (noticeType!!.toPage) {

                        "clean" -> {
                            if (CommonUtils.checkIfCanClean()) {
                                JunkScanningActivity.start(this)
                            } else {
                                CleanResultActivity.start(this, CleanType.JunkType)
                            }
                        }

                        "antivirus" -> {
                            if ("front_notice" == noticeType!!.scene) {
                                TbaHelper.eventPost("antivirus_scan", hashMapOf("mg_source" to "bar"))
                            } else {
                                TbaHelper.eventPost("antivirus_scan", hashMapOf("mg_source" to "pop"))
                            }
                            AntivirusScanningActivity.start(this)
                        }

                        "big_file" -> BigFileCleanActivity.start(this)

                        "empty_folder" -> EmptyFolderActivity.start(this)
                    }
                }
                noticeType = null
            }
        } else {
            if (!hasNotificationPermission() && !showBackAd) {
                requestNotificationPer()
            }
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

    private fun requestNotificationPer() = run {
        if (isAtLeastAndroid13() && !hasNotificationPermission() && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (System.currentTimeMillis() - showNotificationPerDialogTime > 24 * 60 * 60 * 1000) {
                showNotificationPerDialogTime = System.currentTimeMillis()
                CustomAlertDialog(this).showDialog(title = getString(R.string.app_name),
                        message = getString(R.string.request_notice_per_des),
                        positiveButtonText = getString(R.string.got_it),
                        negativeButtonText = "",
                        onPositiveButtonClick = {
                            it.dismiss()
                            showNotificationPerSetting()
                        },
                        onNegativeButtonClick = {})
            }
        }
    }


    private fun showNotificationPerSetting() {
        runCatching {
            val intent = if (isAtLeastAndroid8()) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, app.packageName) }
            } else Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                putExtra("app_package", app.packageName)
                putExtra("app_uid", app.applicationInfo.uid)
            }
            isToSettings = true
            notificationSetLauncher.launch(intent)
        }
    }

    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_back_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_back_int"))

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
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_main_nat"))
        ADManager.ocMainNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocMainNatLoader.canShow(this@MainActivity)) {
                    ad?.destroy()
                    ADManager.ocMainNatLoader.showNativeAd(this@MainActivity, binding.adFr, "oc_main_nat") {
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