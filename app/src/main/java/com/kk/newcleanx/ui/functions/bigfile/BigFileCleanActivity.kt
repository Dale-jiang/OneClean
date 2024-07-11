package com.kk.newcleanx.ui.functions.bigfile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.CleanType
import com.kk.newcleanx.databinding.AcBigFileCleanBinding
import com.kk.newcleanx.databinding.LayoutBigFileFilterBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.functions.bigfile.adapter.BigFileFilterAdapter
import com.kk.newcleanx.ui.functions.bigfile.vm.BigFileCleanViewModel
import com.kk.newcleanx.ui.functions.empty.adapter.EmptyFoldersListAdapter

class BigFileCleanActivity : AllFilePermissionActivity<AcBigFileCleanBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, BigFileCleanActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<BigFileCleanViewModel>()
    private var popupWindow: PopupWindow? = null
    private var typeFilter = -1
    private val adapter by lazy {
        EmptyFoldersListAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.createFilterList()

        binding.apply {
            toolbar.tvTitle.text = getString(R.string.big_files)
            recyclerView.adapter = adapter

            startProgress(minWaitTime = 0L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }

            isCompleted = true


            layoutType.setOnClickListener {
                if (typeFilter == 0) {
                    popupWindow?.dismiss()
                    typeFilter = -1
                } else showPopWindow(0)
            }

            layoutSize.setOnClickListener {
                if (typeFilter == 1) {
                    popupWindow?.dismiss()
                    typeFilter = -1
                } else showPopWindow(1)
            }

            layoutTime.setOnClickListener {
                if (typeFilter == 2) {
                    popupWindow?.dismiss()
                    typeFilter = -1
                } else showPopWindow(2)
            }

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

    private fun showPopWindow(type: Int) {
        typeFilter = type
        val popBinding = LayoutBigFileFilterBinding.inflate(layoutInflater)
        popBinding.apply {
            popupWindow = PopupWindow(
                root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
            )
            popupWindow?.isFocusable = false
            popupWindow?.isOutsideTouchable = true
            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val mFilterAdapter = BigFileFilterAdapter(this@BigFileCleanActivity) {
                when (type) {
                    0 -> {
                        binding.tvType.text = ContextCompat.getString(this@BigFileCleanActivity, it.nameId)
                    }

                    1 -> {
                        binding.tvSize.text = ContextCompat.getString(this@BigFileCleanActivity, it.nameId)
                    }

                    2 -> {
                        binding.tvTime.text = ContextCompat.getString(this@BigFileCleanActivity, it.nameId)
                    }
                }

                popupWindow?.dismiss()
                typeFilter = -1
            }

            when (type) {
                0 -> {
                    mFilterAdapter.initData(viewModel.typeList)
                }

                1 -> {
                    mFilterAdapter.initData(viewModel.sizeList)
                }

                2 -> {
                    mFilterAdapter.initData(viewModel.timeList)
                }
            }

            recyclerView.itemAnimator = null
            recyclerView.adapter = mFilterAdapter

            val translateAnimation = TranslateAnimation(0, 0.0f, 0, 0.0f, 1, -1.0f, 1, 0.0f)
            translateAnimation.duration = 360L
            popBinding.recyclerView.startAnimation(translateAnimation)
            popupWindow?.showAsDropDown(binding.layoutFilter, 0, 0)
        }
    }


}