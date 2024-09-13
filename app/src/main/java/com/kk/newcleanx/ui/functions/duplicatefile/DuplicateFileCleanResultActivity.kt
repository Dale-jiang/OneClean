package com.kk.newcleanx.ui.functions.duplicatefile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.data.local.DUPLICATE_FILES_CLEAN
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.JUNK_CLEAN
import com.kk.newcleanx.data.local.SCAN_ANTIVIRUS
import com.kk.newcleanx.data.local.busObserver
import com.kk.newcleanx.databinding.AcCleanResultBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.CleanResultActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.antivirus.AntivirusScanningActivity
import com.kk.newcleanx.ui.functions.bigfile.BigFileCleanActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.ui.functions.duplicatefile.adapter.DuplicateCleanResultListAdapter
import com.kk.newcleanx.ui.functions.empty.EmptyFolderActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.showAntivirusNotice
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuplicateFileCleanResultActivity : AllFilePermissionActivity<AcCleanResultBinding>() {

    companion object {
        fun start(context: Context, tips: String) {
            context.startActivity(Intent(context, DuplicateFileCleanResultActivity::class.java).apply {
                putExtra(INTENT_KEY, tips)
            })
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private var adapter: DuplicateCleanResultListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
        showNatAd()

        val tips = intent?.getStringExtra(INTENT_KEY)
        binding.apply {
            tvTips2.text = getString(R.string.a_total_of_has_been_deleted, tips)
        }

        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {

        adapter = DuplicateCleanResultListAdapter(this) {
            when (it.type) {
                BIG_FILE_CLEAN -> {
                    busObserver.postValue(DUPLICATE_FILES_CLEAN)
                    BigFileCleanActivity.start(this)
                    finish()
                }

                JUNK_CLEAN -> {
                    busObserver.postValue(DUPLICATE_FILES_CLEAN)
                    if (CommonUtils.checkIfCanClean()) {
                        JunkScanningActivity.start(this)
                    } else {
                        CleanResultActivity.start(this, CleanType.JunkType)
                    }
                    finish()
                }

                SCAN_ANTIVIRUS -> {

                    showAntivirusNotice { res ->
                        if (res) {
                            busObserver.postValue(DUPLICATE_FILES_CLEAN)
                            AntivirusScanningActivity.start(this)
                            finish()
                        }
                    }

                }

                EMPTY_FOLDER -> {
                    busObserver.postValue(DUPLICATE_FILES_CLEAN)
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
                if (ADManager.ocCleanNatLoader.canShow(this@DuplicateFileCleanResultActivity)) {
                    ad?.destroy()
                    ADManager.ocCleanNatLoader.showNativeAd(this@DuplicateFileCleanResultActivity, binding.adFr, "oc_clean_nat") {
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