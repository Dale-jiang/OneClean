package com.kk.newcleanx.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        binding.clean.setOnClickListener {
            requestAllFilePermission {
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