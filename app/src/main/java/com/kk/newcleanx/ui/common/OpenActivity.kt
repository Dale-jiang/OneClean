package com.kk.newcleanx.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.data.local.isFirstStartup
import com.kk.newcleanx.databinding.AcOpenBinding
import com.kk.newcleanx.ui.base.BaseActivity
import kotlinx.coroutines.launch

class OpenActivity : BaseActivity<AcOpenBinding>() {


    private val countDownTimer = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val elapsedSeconds = (10000 - millisUntilFinished) / 1000
            if (elapsedSeconds >= 3 && checkCondition()) {
                cancel()
                navigateToNextPage()
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
            doAdProgress()
        }

    }

    private fun doAdProgress() {
        countDownTimer.start()
    }

    private fun navigateToNextPage() {
        isFirstStartup = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkCondition(): Boolean {
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

}