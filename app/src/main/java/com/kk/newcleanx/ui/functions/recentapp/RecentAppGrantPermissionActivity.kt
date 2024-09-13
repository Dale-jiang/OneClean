package com.kk.newcleanx.ui.functions.recentapp

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcRecentAppPermissionBinding
import com.kk.newcleanx.ui.base.BaseActivity

class RecentAppGrantPermissionActivity : BaseActivity<AcRecentAppPermissionBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, RecentAppGrantPermissionActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.tvTitle.text = getString(R.string.grant_permission)
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@RecentAppGrantPermissionActivity, R.color.main_text_color), PorterDuff.Mode.SRC_IN)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnOk.setOnClickListener {
                // TODO:  
            }
        }
    }

}