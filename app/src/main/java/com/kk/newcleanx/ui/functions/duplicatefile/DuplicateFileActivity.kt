package com.kk.newcleanx.ui.functions.duplicatefile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.DUPLICATE_FILES_CLEAN
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.busObserver
import com.kk.newcleanx.data.local.duplicateFiles
import com.kk.newcleanx.databinding.AcDuplicateFileCleanBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.duplicatefile.adapter.DuplicateFileCleanAdapter
import com.kk.newcleanx.ui.functions.duplicatefile.vm.DuplicateFileCleanViewModel
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.opFiles
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuplicateFileActivity : AllFilePermissionActivity<AcDuplicateFileCleanBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DuplicateFileActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar
    }

    private val viewModel by viewModels<DuplicateFileCleanViewModel>()
    private var adapter: DuplicateFileCleanAdapter? = null
    private var deleteSize = 0L

    @SuppressLint("NotifyDataSetChanged")
    private val deleteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            viewModel.refreshData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            tvTitle.text = getString(R.string.duplicate_files)

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    showFullAd {
                        clLoading.isVisible = false
                        viewLottie.cancelAnimation()
                        if (duplicateFiles.isNotEmpty()) Toast.makeText(this@DuplicateFileActivity, getString(R.string.same_has_been_selected), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            viewModel.getDuplicateFileList()

            tvRight.setOnClickListener {
                duplicateFiles.forEach { it.select = false }
                adapter?.notifyDataSetChanged()
                changeButtonView()
            }

            btnClean.setOnClickListener {

                CustomAlertDialog(this@DuplicateFileActivity).showDialog(title = getString(R.string.app_name),
                    message = getString(R.string.duplicate_file_delete_tip),
                    positiveButtonText = getString(R.string.string_ok),
                    negativeButtonText = getString(R.string.string_cancel),
                    onPositiveButtonClick = { dialog ->
                        deleteLauncher.launch(Intent(this@DuplicateFileActivity, DuplicateFileCleanActivity::class.java).apply {
                            putExtra(INTENT_KEY, deleteSize.formatStorageSize())
                        })
                        dialog.dismiss()
                    },
                    onNegativeButtonClick = {})

            }

            ivScanBack.setOnClickListener {
                finish()
            }

            ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        initObservers()

    }


    private fun initObservers() {

        busObserver.observe(this) {
            if (it == DUPLICATE_FILES_CLEAN) {
                duplicateFiles.clear()
                finish()
                busObserver.postValue("")
            }
        }

        viewModel.completeObserver.observe(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.ivEmpty.isVisible = duplicateFiles.isEmpty()
                binding.recyclerView.itemAnimator = null
                adapter = DuplicateFileCleanAdapter(this@DuplicateFileActivity, duplicateFiles,
                    click = { data ->
                        CustomAlertDialog(this@DuplicateFileActivity).showDialog(title = data.name,
                            message = data.path,
                            positiveButtonText = getString(R.string.string_preview),
                            negativeButtonText = "",
                            onPositiveButtonClick = { dialog ->
                                dialog.dismiss()
                                this@DuplicateFileActivity.opFiles(data.path)
                            },
                            onNegativeButtonClick = {})
                    },
                    change = { changeButtonView() })

                binding.recyclerView.adapter = adapter
                changeButtonView()
                isCompleted = true
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun changeButtonView() {
        val size = adapter?.getList()?.filter { it.select }?.sumOf { it.size } ?: 0
        if (size <= 0) {
            binding.btnClean.isEnabled = false
            binding.btnClean.setBackgroundResource(R.drawable.shape_d9d9d9_r24)
            binding.btnClean.text = getString(R.string.string_clean)
            deleteSize = 0
        } else {
            binding.btnClean.isEnabled = true
            binding.btnClean.setBackgroundResource(R.drawable.ripple_clean_continue_btn)
            binding.btnClean.text = "${getString(R.string.string_clean)}(${size.formatStorageSize()})"
            deleteSize = size
        }
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax() || ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_scan_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_int"))
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@DuplicateFileActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@DuplicateFileActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@DuplicateFileActivity)
                b.invoke()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        duplicateFiles.clear()
    }

}