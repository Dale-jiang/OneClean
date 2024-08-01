package com.kk.newcleanx.ui.functions.appmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcAppManagerBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.appmanager.adapter.AppManagerAdapter


class AppManagerActivity : AllFilePermissionActivity<AcAppManagerBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AppManagerActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private var adapter: AppManagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapter()

        binding.apply {

            toolbar.tvTitle.text = getString(R.string.app_manager)

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }

            isCompleted = true

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

    }


    private fun initAdapter() {
        adapter = AppManagerAdapter(this) {}
        binding.recyclerView.adapter = adapter
    }

}