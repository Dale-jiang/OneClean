package com.kk.newcleanx.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.kk.newcleanx.data.local.isFirstStartup
import com.kk.newcleanx.databinding.AcGuideCleanPageBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity

class GuideCleanPageActivity : AllFilePermissionActivity<AcGuideCleanPageBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, GuideCleanPageActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.clean.startAnimation(
            ScaleAnimation(
                1.0f, 1.05f, 1.0f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).also {
                it.duration = 600
                it.repeatCount = Animation.INFINITE
                it.repeatMode = Animation.REVERSE
            }
        )

        binding.clean.setOnClickListener {
            requestAllFilePermission(false) {
                if (it) {
                    isFirstStartup = false
                    startActivities(
                        arrayOf(
                            Intent(this, MainActivity::class.java),
                            Intent(this, JunkScanningActivity::class.java)
                        )
                    )
                    finish()
                }
            }
        }

        binding.skip.setOnClickListener {
            isFirstStartup = false
            MainActivity.start(this, null)
            finish()
        }


    }

}