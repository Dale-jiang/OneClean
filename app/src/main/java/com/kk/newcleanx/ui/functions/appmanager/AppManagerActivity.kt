package com.kk.newcleanx.ui.functions.appmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.AcAppManagerBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.appmanager.adapter.AppManagerAdapter
import com.kk.newcleanx.ui.functions.appmanager.vm.AppManagerViewModel
import com.kk.newcleanx.utils.isPackageInstalled
import com.kk.newcleanx.utils.openAppDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AppManagerActivity : AllFilePermissionActivity<AcAppManagerBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AppManagerActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<AppManagerViewModel>()
    private var adapter: AppManagerAdapter? = null
    private var mIndex = -1
    private var mPackageName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapter()
        showMainNatAd()
        viewModel.getAppInfoList()

        binding.apply {

            toolbar.tvTitle.text = getString(R.string.app_manager)

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    showFullAd {
                        clLoading.isVisible = false
                        viewLottie.cancelAnimation()
                    }
                }
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        initObserver()

    }

    private fun initAdapter() {
        adapter = AppManagerAdapter(this) { data, index ->
            mPackageName = data.appPackageName
            mIndex = index
            this.openAppDetails(data.appPackageName)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun initObserver() {
        viewModel.appInfoListObserver.observe(this) {
            isCompleted = true
            adapter?.initData(it)
        }

    }

    override fun onResume() {
        super.onResume()
        isToSettings = false
        checkList()
    }


    private fun checkList() {
        runCatching {
            if (mPackageName.isNotEmpty() && mIndex != -1) {
                if (!isPackageInstalled(mPackageName)) {
                    adapter?.getDataList()?.removeAt(mIndex)
                    adapter?.notifyItemChanged(mIndex)
                }
            }
        }
    }

    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()) {
            b.invoke()
            return
        }

        // log : oc_scan_int

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@AppManagerActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@AppManagerActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@AppManagerActivity)
                b.invoke()
            }
        }
    }

    private var ad: AdType? = null
    private fun showMainNatAd() {
        if (ADManager.isOverAdMax()) return
        ADManager.ocScanNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocScanNatLoader.canShow(this@AppManagerActivity)) {
                    ad?.destroy()
                    ADManager.ocScanNatLoader.showNativeAd(this@AppManagerActivity, binding.adFr, "oc_scan_nat") {
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