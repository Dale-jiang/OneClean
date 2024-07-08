package com.kk.newcleanx.ui.common

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity

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
            requestAllFilePermission {
                if (it.not()) {
                    Toast.makeText(this, "伟授权", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "已授权", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun test() {
        binding.hello.text = "你好"
    }
}