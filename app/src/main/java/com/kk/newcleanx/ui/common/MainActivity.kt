package com.kk.newcleanx.ui.common

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import com.kk.newcleanx.data.local.USER_AGREEMENT_URL
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.BaseActivity

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
            WebViewActivity.start(this, USER_AGREEMENT_URL)
        }
    }


    private fun test() {
        binding.hello.text = "你好"
    }
}