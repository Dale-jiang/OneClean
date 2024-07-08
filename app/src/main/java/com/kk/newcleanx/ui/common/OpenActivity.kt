package com.kk.newcleanx.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import com.kk.newcleanx.databinding.AcOpenBinding
import com.kk.newcleanx.ui.MainActivity
import com.kk.newcleanx.ui.base.BaseActivity

class OpenActivity : BaseActivity<AcOpenBinding>() {

    private val countDownTimer = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val elapsedSeconds = (10000 - millisUntilFinished) / 1000
            if (elapsedSeconds >= 3 && checkCondition()) {
                navigateToNextPage()
                cancel()
            }
        }

        override fun onFinish() {
            navigateToNextPage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViews() // onBackPressedDispatcher.addCallback {}
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
        return arrayOf(true, false).random()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

}