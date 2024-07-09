package com.kk.newcleanx.ui.common

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity

class MainActivity : AllFilePermissionActivity<AcMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        test()

        onBackPressedDispatcher.addCallback {
            Toast.makeText(this@MainActivity, "!!!!!!!!", Toast.LENGTH_LONG).show()
        }

        binding.hello.setOnClickListener {
            requestAllFilePermission {
                Toast.makeText(this, if (it) "success" else "failed", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun test() {
        binding.hello.text = "你好"
    }
}