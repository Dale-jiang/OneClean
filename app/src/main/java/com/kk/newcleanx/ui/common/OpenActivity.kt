package com.kk.newcleanx.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.kk.newcleanx.data.local.isFirstStartup
import com.kk.newcleanx.databinding.AcOpenBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpenActivity : BaseActivity<AcOpenBinding>() {


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { doAdProgress() }
        }, { doAdProgress() })
    }


    private fun navigateToNextPage() {
        isFirstStartup = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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