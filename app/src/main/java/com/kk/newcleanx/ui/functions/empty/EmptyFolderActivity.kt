package com.kk.newcleanx.ui.functions.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.kk.newcleanx.databinding.AcEmptyFolderBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.clean.vm.JunkCleanViewModel

class EmptyFolderActivity : AllFilePermissionActivity<AcEmptyFolderBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, EmptyFolderActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<JunkCleanViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            startProgress(minWaitTime = 3000L) {
                if (it >= 100) clLoading.isVisible = false
            }
            isCompleted = true
            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

    }
}