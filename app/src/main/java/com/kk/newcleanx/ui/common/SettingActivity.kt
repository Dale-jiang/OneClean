package com.kk.newcleanx.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.USER_AGREEMENT_URL
import com.kk.newcleanx.data.local.USER_PRIVACY_URL
import com.kk.newcleanx.databinding.AcSettingBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity

class SettingActivity : AllFilePermissionActivity<AcSettingBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.string_setting)
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }

        binding.tvPrivacy.setOnClickListener {
            WebViewActivity.start(this, USER_PRIVACY_URL)
        }
        binding.tvAgreement.setOnClickListener {
            WebViewActivity.start(this, USER_AGREEMENT_URL)
        }

    }

}