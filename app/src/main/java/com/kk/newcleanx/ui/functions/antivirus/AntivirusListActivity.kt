package com.kk.newcleanx.ui.functions.antivirus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.data.local.virusRiskList
import com.kk.newcleanx.databinding.AcAntivirusListBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.antivirus.adapter.AntivirusListAdapter
import com.kk.newcleanx.ui.functions.antivirus.vm.AntivirusListViewModel
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.showAntivirusDelete

class AntivirusListActivity : AllFilePermissionActivity<AcAntivirusListBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AntivirusListActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val viewModel by viewModels<AntivirusListViewModel>()
    private var packageNameTemp = ""
    private val unInstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (CommonUtils.isPackageInstalled(packageNameTemp).not()) {
            removeItem(packageNameTemp)
        }
    }

    private val adapter by lazy {
        AntivirusListAdapter(this) {
            if (it.packageName.isNotEmpty() && CommonUtils.isPackageInstalled(it.packageName)) {
                packageNameTemp = it.packageName
                unInstallApp()
            } else {
                showAntivirusDelete {
                    viewModel.deleteFile(it)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            tvNum.text = getString(R.string.threats_found, virusRiskList.size)
            recyclerView.adapter = adapter
            adapter.setDataList(virusRiskList)

            toolbar.tvTitle.text = getString(R.string.string_antivirus)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { onBackClicked() }
        }
        initObserver()
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        viewModel.apply {
            deleteObserver.observe(this@AntivirusListActivity) {
                removeItem(it)
            }
        }
    }

    private fun removeItem(path: String) {
        runCatching {
            val index = adapter.getDataList().indexOfFirst { it.path == path || it.packageName == path }
            adapter.getDataList().removeAt(index)
            adapter.notifyItemRemoved(index)
            val num = adapter.getDataList().size
            binding.tvNum.text = getString(R.string.threats_found, num)
            if (num <= 0) {
                AntivirusResultActivity.start(this@AntivirusListActivity, getString(R.string.all_threats_resolved))
                finish()
            }
        }
    }

    private fun onBackClicked() {
        finish()
    }

    @Suppress("DEPRECATION")
    private fun unInstallApp() {
        runCatching {
            isToSettings = true
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
            intent.data = Uri.parse("package:${packageNameTemp}")
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            unInstallLauncher.launch(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        virusRiskList.clear()
    }

}