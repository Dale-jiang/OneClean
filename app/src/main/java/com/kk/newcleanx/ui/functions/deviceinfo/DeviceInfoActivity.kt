package com.kk.newcleanx.ui.functions.deviceinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcDeviceInfoBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity

class DeviceInfoActivity : AllFilePermissionActivity<AcDeviceInfoBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DeviceInfoActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.tvTitle.text = getString(R.string.device_status)

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }

            isCompleted = true

            btnClean.setOnClickListener {
                JunkScanningActivity.start(this@DeviceInfoActivity)
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

    }
}