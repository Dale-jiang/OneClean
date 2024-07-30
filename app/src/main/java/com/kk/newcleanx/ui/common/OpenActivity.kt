package com.kk.newcleanx.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
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

        ADManager.fm_launch.loadAd(this)

        onBackPressedDispatcher.addCallback {}

        lifecycleScope.launch {
            setViews()
        }

    }

    private fun setViews() {
        countDownTimer.start()
    }

    private fun navigateToNextPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkCondition(): Boolean {
        return ADManager.fm_launch.canShow(this)
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()) {
            b.invoke()
            return
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.fm_launch.canShow(this@OpenActivity)) {
                ADManager.fm_launch.showFullScreenAd(this@OpenActivity, "fm_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.fm_launch.loadAd(this@OpenActivity)
                b.invoke()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

}