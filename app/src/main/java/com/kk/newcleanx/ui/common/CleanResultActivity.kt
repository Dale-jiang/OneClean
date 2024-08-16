package com.kk.newcleanx.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kk.newcleanx.data.local.APP_MANAGER
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.DEVICE_STATUS
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.databinding.AcCleanResultBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.adapter.CleanResultListAdapter
import com.kk.newcleanx.ui.common.adapter.MainListAdapter
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.appmanager.AppManagerActivity
import com.kk.newcleanx.ui.functions.bigfile.BigFileCleanActivity
import com.kk.newcleanx.ui.functions.deviceinfo.DeviceInfoActivity
import com.kk.newcleanx.ui.functions.empty.EmptyFolderActivity
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class CleanResultActivity : AllFilePermissionActivity<AcCleanResultBinding>() {

    companion object {
        fun start(context: Context, type: CleanType?) {
            context.startActivity(Intent(context, CleanResultActivity::class.java).apply {
                putExtra(INTENT_KEY, type)
            })
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private var adapter: CleanResultListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
        showNatAd()

        val type = intent?.getSerializableExtra(INTENT_KEY) as? CleanType
        binding.apply {

            if (type != CleanType.JunkType) {
                tvTips2.isVisible = false
            }

        }

        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {

        adapter = CleanResultListAdapter(this) {
            when (it.type) {
                BIG_FILE_CLEAN -> {
                    BigFileCleanActivity.start(this)
                    finish()
                }

                APP_MANAGER -> {
                    AppManagerActivity.start(this)
                    finish()
                }

                DEVICE_STATUS -> {
                    DeviceInfoActivity.start(this)
                    finish()
                }

                EMPTY_FOLDER -> {
                    EmptyFolderActivity.start(this)
                    finish()
                }
            }
        }

        val layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter!!.getItemViewType(position) == 0) {
                    2
                } else {
                    1
                }
            }
        }
        binding.recyclerView.adapter = adapter
    }


    private var ad: AdType? = null
    private fun showNatAd() {

        if (ADManager.isOverAdMax()) return
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_clean_nat"))
        ADManager.ocCleanNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocCleanNatLoader.canShow(this@CleanResultActivity)) {
                    ad?.destroy()
                    ADManager.ocCleanNatLoader.showNativeAd(this@CleanResultActivity, binding.adFr, "oc_clean_nat") {
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