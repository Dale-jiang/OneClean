package com.kk.newcleanx.ui.clean

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import com.kk.newcleanx.databinding.AcJunkCleanBinding
import com.kk.newcleanx.ui.base.BaseActivity

class JunkCleanActivity : BaseActivity<AcJunkCleanBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkCleanActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            toolbar.tvTitle.text = ""
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            onBackPressedDispatcher.addCallback { }

            btnContinue.setOnClickListener {

            }

        }
    }


}