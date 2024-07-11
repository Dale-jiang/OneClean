package com.kk.newcleanx.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kk.newcleanx.data.local.APP_MANAGER
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.DEVICE_STATUS
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.databinding.AcCleanResultBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.adapter.MainListAdapter

class CleanResultActivity : AllFilePermissionActivity<AcCleanResultBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, CleanResultActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private var adapter: MainListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {

        adapter = MainListAdapter(this) {
            when (it.type) {
                BIG_FILE_CLEAN -> {}
                APP_MANAGER -> {}
                DEVICE_STATUS -> {}
                EMPTY_FOLDER -> {}
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


}