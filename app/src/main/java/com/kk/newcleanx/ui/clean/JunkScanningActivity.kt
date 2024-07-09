package com.kk.newcleanx.ui.clean

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcJunkScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity

class JunkScanningActivity : AllFilePermissionActivity<AcJunkScanningBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkScanningActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@JunkScanningActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
            toolbar.tvTitle.text = getString(R.string.string_scanning)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


}