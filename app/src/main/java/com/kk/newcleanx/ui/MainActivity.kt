package com.kk.newcleanx.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.databinding.AcMainBinding

class MainActivity : BaseActivity<AcMainBinding>() {

    override val isLight by lazy { true }
    override val isBottomPadding by lazy { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        test()

        onBackPressedDispatcher.addCallback {
            Toast.makeText(this@MainActivity, "!!!!!!!!", Toast.LENGTH_LONG).show()
        }

        binding.hello.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }


    private fun test() {
        binding.hello.text = "你好"
    }
}