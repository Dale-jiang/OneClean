package com.kk.newcleanx.ui.common

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.KEY_NOTICE_FUNCTION
import com.kk.newcleanx.data.local.NoticeType
import com.kk.newcleanx.data.local.isFirstStartup
import com.kk.newcleanx.databinding.AcOpenBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.antivirus.AntivirusScanningActivity
import com.kk.newcleanx.ui.functions.bigfile.BigFileCleanActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.ui.functions.empty.EmptyFolderActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.hasNotificationPermission
import com.kk.newcleanx.utils.CommonUtils.isAtLeastAndroid13
import com.kk.newcleanx.utils.startFrontNoticeService
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class OpenActivity : BaseActivity<AcOpenBinding>() {


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (hasNotificationPermission()) {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "yes"))
        } else {
            TbaHelper.eventPost("permiss_notifi", hashMapOf("res" to "no"))
        }
    }


    private val noticeType by lazy { intent?.getParcelableExtra<NoticeType>(KEY_NOTICE_FUNCTION) }

    private val countDownTimer = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val elapsedSeconds = (10000 - millisUntilFinished) / 1000
            if (elapsedSeconds >= 3 && checkCondition()) {
                cancel()
                showFullAd {
                    navigateToNextPage()
                }
            }
        }

        override fun onFinish() {
            navigateToNextPage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TbaHelper.postSessionEvent()
        TbaHelper.eventPost("loading_page")
        startFrontNoticeService()

        if (!hasNotificationPermission() && isAtLeastAndroid13()) {
            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        onBackPressedDispatcher.addCallback {}
        lifecycleScope.launch {
            if (isFirstStartup) requestUMPInfo() else doAdProgress()
        }

    }

    private fun loadingAd() {
        lifecycleScope.launch {

            ADManager.ocLaunchLoader.loadAd(this@OpenActivity)
            ADManager.ocScanIntLoader.loadAd(this@OpenActivity)
            ADManager.ocCleanIntLoader.loadAd(this@OpenActivity)

            ADManager.ocScanNatLoader.loadAd(this@OpenActivity)
            ADManager.ocCleanNatLoader.loadAd(this@OpenActivity)
            ADManager.ocMainNatLoader.loadAd(this@OpenActivity)
        }
    }

    private fun doAdProgress() {
        loadingAd()
        countDownTimer.start()
    }

    private fun requestUMPInfo() {

        val paramsBuilder = ConsentRequestParameters.Builder()
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(this).setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("E0B0FE3B4C530E91951B4FC4C86CF0E9").build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, paramsBuilder.build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { doAdProgress() }
        }, { doAdProgress() })

        if (BuildConfig.DEBUG) {
            consentInformation.reset()
        }
    }

    private fun navigateToNextPage() {

        if (noticeType == null) { //open
            isFirstStartup = false
            MainActivity.start(this, null)
        } else {

            when (noticeType!!.toPage) {

                "clean" -> {
                    if (CommonUtils.hasAllStoragePermission()) {
                        startActivities(
                                arrayOf(
                                        Intent(this, MainActivity::class.java),
                                        if (CommonUtils.checkIfCanClean()) {
                                            Intent(this, JunkScanningActivity::class.java)
                                        } else {
                                            Intent(this, CleanResultActivity::class.java).apply {
                                                putExtra(INTENT_KEY, CleanType.JunkType)
                                            }
                                        }
                                ))
                    } else MainActivity.start(this, noticeType)
                }

                "antivirus" -> {
                    if (CommonUtils.hasAllStoragePermission()) {
                        startActivities(arrayOf(Intent(this, MainActivity::class.java), Intent(this, AntivirusScanningActivity::class.java)))
                    } else MainActivity.start(this, noticeType)
                }

                "big_file" -> {
                    if (CommonUtils.hasAllStoragePermission()) {
                        startActivities(arrayOf(Intent(this, MainActivity::class.java), Intent(this, BigFileCleanActivity::class.java)))
                    } else MainActivity.start(this, noticeType)
                }

                "empty_folder" -> {
                    if (CommonUtils.hasAllStoragePermission()) {
                        startActivities(arrayOf(Intent(this, MainActivity::class.java), Intent(this, EmptyFolderActivity::class.java)))
                    } else MainActivity.start(this, noticeType)
                }

                else -> MainActivity.start(this, null)
            }


        }

        finish()
    }

    private fun checkCondition(): Boolean {
        return ADManager.ocLaunchLoader.canShow(this)
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()) {
            b.invoke()
            return
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocLaunchLoader.canShow(this@OpenActivity)) {
                ADManager.ocLaunchLoader.showFullScreenAd(this@OpenActivity, "fm_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocLaunchLoader.loadAd(this@OpenActivity)
                b.invoke()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

}