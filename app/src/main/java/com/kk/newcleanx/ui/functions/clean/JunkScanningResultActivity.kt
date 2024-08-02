package com.kk.newcleanx.ui.functions.clean

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.JunkDetails
import com.kk.newcleanx.data.local.junkDataList
import com.kk.newcleanx.databinding.AcJunkScanningResultBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.clean.adapter.JunkScanningResultAdapter
import com.kk.newcleanx.utils.formatStorageSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JunkScanningResultActivity : BaseActivity<AcJunkScanningResultBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkScanningResultActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }


    private val adapter by lazy {
        JunkScanningResultAdapter(this@JunkScanningResultActivity) {
            getSelectSize()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            getSelectSize()
            showNatAd()

            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@JunkScanningResultActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
            toolbar.tvTitle.text = getString(R.string.clean_junk)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { finish() }

            btnClean.setOnClickListener {
                JunkCleanActivity.start(this@JunkScanningResultActivity, CleanType.JunkType)
                finish()
            }

            binding.recyclerView.itemAnimator = null
            binding.recyclerView.adapter = adapter
            adapter.initData(junkDataList)
        }
    }

    private fun getSelectSize() {
        val selectList = junkDataList.filterIsInstance<JunkDetails>().filter { it.select }
        if (selectList.isEmpty()) {
            binding.btnClean.isEnabled = false
            binding.btnClean.alpha = 0.5f
        } else {
            binding.btnClean.isEnabled = true
            binding.btnClean.alpha = 1f
        }
        binding.tvJunkSize.text = selectList.sumOf { it.fileSize }.formatStorageSize()
    }

    private var ad: AdType? = null
    private fun showNatAd() {

        if (ADManager.isOverAdMax()) return
        ADManager.ocScanNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocScanNatLoader.canShow(this@JunkScanningResultActivity)) {
                    ad?.destroy()
                    ADManager.ocScanNatLoader.showNativeAd(this@JunkScanningResultActivity, binding.adFr, "oc_scan_nat") {
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