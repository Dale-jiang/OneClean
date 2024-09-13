package com.kk.newcleanx.ui.common

import android.os.Bundle
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.INTENT_KEY_1
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.AcPermissionSettingDialogBinding
import com.kk.newcleanx.ui.base.BaseActivity

class PermissionSettingDialogActivity : BaseActivity<AcPermissionSettingDialogBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            isToSettings = true
            val title = intent?.getStringExtra(INTENT_KEY) ?: getString(R.string.user_guide)
            val message = intent?.getStringExtra(INTENT_KEY_1) ?: getString(R.string.please_ensure_this_option_is_enabled)

            tvTitle.text = title
            tvMessage.text = message

            root.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            tvBtn.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isToSettings = true
    }

}