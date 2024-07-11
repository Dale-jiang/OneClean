package com.kk.newcleanx.ui.functions.bigfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.emptyFoldersDataList
import com.kk.newcleanx.databinding.AcBigFileCleanBinding
import com.kk.newcleanx.databinding.AcEmptyFolderBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.functions.empty.adapter.EmptyFoldersListAdapter
import com.kk.newcleanx.ui.functions.empty.vm.EmptyFolderViewModel

class BigFileCleanActivity : AllFilePermissionActivity<AcBigFileCleanBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, BigFileCleanActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<EmptyFolderViewModel>()
    private val adapter by lazy {
        EmptyFoldersListAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.tvTitle.text = getString(R.string.big_files)
            recyclerView.adapter = adapter

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }

            isCompleted = true

            btnClean.setOnClickListener {
                JunkCleanActivity.start(this@BigFileCleanActivity, CleanType.EmptyFolderType)
                finish()
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        initObserver()

    }

    private fun initObserver() {

    }
}