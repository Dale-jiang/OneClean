package com.kk.newcleanx.ui.functions.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.kk.newcleanx.databinding.AcJunkScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.clean.vm.JunkCleanViewModel

class EmptyFolderActivity : AllFilePermissionActivity<AcJunkScanningBinding>() {
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

    }
}