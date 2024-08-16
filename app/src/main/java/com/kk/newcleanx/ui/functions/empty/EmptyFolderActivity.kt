package com.kk.newcleanx.ui.functions.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.emptyFoldersDataList
import com.kk.newcleanx.databinding.AcEmptyFolderBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.empty.adapter.EmptyFoldersListAdapter
import com.kk.newcleanx.ui.functions.empty.vm.EmptyFolderViewModel
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    showFullAd {
                        clLoading.isVisible = false
                        viewLottie.cancelAnimation()
                    }
                }
            }
            viewModel.getEmptyFoldersList()

            btnClean.setOnClickListener {
                JunkCleanActivity.start(this@EmptyFolderActivity, CleanType.EmptyFolderType)
                finish()
            }

            ivScanBack.setOnClickListener {
                finish()
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        initObserver()
        showNatAd()

    }

    private fun initObserver() {
        viewModel.apply {
            scanningCompletedObserver.observe(this@EmptyFolderActivity) {
                isCompleted = true
                adapter.initData(emptyFoldersDataList)
                binding.tvNum.text = "${emptyFoldersDataList.size}"
                if (emptyFoldersDataList.size <= 0) {
                    binding.ivEmpty.isVisible = true
                    binding.btnClean.isEnabled = false
                    binding.btnClean.setBackgroundResource(R.drawable.shape_d9d9d9_r24)
                } else {
                    binding.ivEmpty.isVisible = false
                    binding.btnClean.isEnabled = true
                    binding.btnClean.setBackgroundResource(R.drawable.ripple_clean_continue_btn)
                }
            }
        }
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()|| ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_scan_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_int"))
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@EmptyFolderActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@EmptyFolderActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@EmptyFolderActivity)
                b.invoke()
            }
        }
    }


    private var ad: AdType? = null
    private fun showNatAd() {

        if (ADManager.isOverAdMax()) {
            return
        }
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_nat"))
        ADManager.ocScanNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocScanNatLoader.canShow(this@EmptyFolderActivity)) {
                    ad?.destroy()
                    binding.adFr.isVisible = true
                    ADManager.ocScanNatLoader.showNativeAd(this@EmptyFolderActivity, binding.adFr, "oc_scan_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
    }


}