package com.kk.newcleanx.ui.functions.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.emptyFoldersDataList
import com.kk.newcleanx.databinding.AcEmptyFolderBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.empty.adapter.EmptyFoldersListAdapter
import com.kk.newcleanx.ui.functions.empty.vm.EmptyFolderViewModel

class EmptyFolderActivity : AllFilePermissionActivity<AcEmptyFolderBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, EmptyFolderActivity::class.java))
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
            toolbar.tvTitle.text = getString(R.string.empty_folders)
            recyclerView.adapter = adapter

            startProgress(minWaitTime = 3000L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }
            viewModel.getEmptyFoldersList()

            btnClean.setOnClickListener {

            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        initObserver()

    }

    private fun initObserver() {
        viewModel.apply {
            scanningCompletedObserver.observe(this@EmptyFolderActivity) {
                isCompleted = true
                adapter.initData(emptyFoldersDataList)
                binding.tvNum.text = "${emptyFoldersDataList.size}"
            }
        }
    }
}