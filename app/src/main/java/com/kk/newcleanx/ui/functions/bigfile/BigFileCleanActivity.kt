package com.kk.newcleanx.ui.functions.bigfile

import android.annotation.SuppressLint
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
import com.kk.newcleanx.data.local.allBigFiles
import com.kk.newcleanx.databinding.AcBigFileCleanBinding
import com.kk.newcleanx.databinding.LayoutBigFileFilterBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.JunkCleanActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.bigfile.adapter.BigFileCleanAdapter
import com.kk.newcleanx.ui.functions.bigfile.adapter.BigFileFilterAdapter
import com.kk.newcleanx.ui.functions.bigfile.vm.BigFileCleanViewModel
import com.kk.newcleanx.utils.formatStorageSize

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
    private var adapter: BigFileCleanAdapter? = null
    private var refreshPage = false


    override fun onResume() {
        super.onResume()
        if (refreshPage) {
            refreshPage = false
            viewModel.filterBigFiles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapter()
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

            viewModel.getAllBigFiles()

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
                val dialog = CustomAlertDialog(this@BigFileCleanActivity)
                dialog.showDialog(title = getString(R.string.string_tips),
                                  message = getString(R.string.delete_big_file_tip),
                                  positiveButtonText = getString(R.string.string_ok),
                                  negativeButtonText = getString(R.string.string_cancel),
                                  onPositiveButtonClick = { d ->
                                      refreshPage = true
                                      JunkCleanActivity.start(this@BigFileCleanActivity, CleanType.BigFileType)
                                      d.dismiss()
                                  },
                                  onNegativeButtonClick = {})
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }
        initObserver()
    }

    @SuppressLint("SetTextI18n")
    private fun initAdapter() {
        adapter = BigFileCleanAdapter(this, click = {

            CustomAlertDialog(this).showDialog(title = "${it.name}---${it.mimeType}",
                                               message = it.path,
                                               positiveButtonText = getString(R.string.string_ok),
                                               negativeButtonText = "",
                                               onPositiveButtonClick = { dialog ->
                                                   dialog.dismiss()
                                               },
                                               onNegativeButtonClick = {})


        }, change = {

            val size = allBigFiles.filter { it.select }.sumOf { it.size }

            if (size <= 0) {
                binding.btnClean.isEnabled = false
                binding.btnClean.setBackgroundResource(R.drawable.shape_d9d9d9_r24)
                binding.btnClean.text = getString(R.string.string_clean)
            } else {
                binding.btnClean.isEnabled = true
                binding.btnClean.setBackgroundResource(R.drawable.ripple_clean_continue_btn)
                binding.btnClean.text = "${getString(R.string.string_clean)}(${size.formatStorageSize()})"
            }

        })
        binding.recyclerView.adapter = adapter
    }

    private fun initObserver() {
        viewModel.apply {
            completeObserver.observe(this@BigFileCleanActivity) {

                isCompleted = true

                binding.btnClean.isEnabled = false
                binding.btnClean.setBackgroundResource(R.drawable.shape_d9d9d9_r24)
                binding.btnClean.text = getString(R.string.string_clean)
                binding.ivEmpty.isVisible = it.isEmpty()
                adapter?.initData(it)
            }
        }
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
                viewModel.filterBigFiles()
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