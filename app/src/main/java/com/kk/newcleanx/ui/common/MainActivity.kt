package com.kk.newcleanx.ui.common

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog

class MainActivity : AllFilePermissionActivity<AcMainBinding>() {

    override val isLight by lazy { true }
    override val isBottomPadding by lazy { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        test()

        onBackPressedDispatcher.addCallback {
            Toast.makeText(this@MainActivity, "!!!!!!!!", Toast.LENGTH_LONG).show()
        }

        binding.hello.setOnClickListener {
            CustomAlertDialog(this).showDialog(
                title = getString(R.string.app_name),
                message = getString(R.string.grant_permission_to_use),
                positiveButtonText = getString(R.string.string_ok),
                negativeButtonText = getString(R.string.string_cancel),
                onPositiveButtonClick = {

                },
                onNegativeButtonClick = {

                }
            )
        }
    }


    private fun test() {
        binding.hello.text = "你好"
    }
}