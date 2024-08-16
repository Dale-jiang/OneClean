package com.kk.newcleanx.ui.common

import android.os.Bundle
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.AcPermissionSettingDialogBinding
import com.kk.newcleanx.ui.base.BaseActivity

class PermissionSettingDialogActivity : BaseActivity<AcPermissionSettingDialogBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

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