package com.kk.newcleanx.ui.functions.antivirus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.virusRiskList
import com.kk.newcleanx.databinding.AcAntivirusScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.antivirus.vm.AntivirusScanningViewModel
import com.kk.newcleanx.utils.showAntivirusNotice
import com.kk.newcleanx.utils.showAntivirusScanError
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AntivirusScanningActivity : AllFilePermissionActivity<AcAntivirusScanningBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AntivirusScanningActivity::class.java))
        }
    }

    private val viewModel by viewModels<AntivirusScanningViewModel>()

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

            toolbar.tvTitle.text = getString(R.string.string_antivirus)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { onBackClicked() }

            showAntivirusNotice {
                if (it) {
                    requestAllFilePermission { ok ->
                        if (ok) {
                            viewLottie.playAnimation()
                            viewModel.startScan()
                        } else finish()
                    }
                } else finish()
            }
        }

        initObserver()

    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {

        viewModel.apply {
            runningTask.observe(this@AntivirusScanningActivity) {
                setTaskProgress(it)
            }
            progress.observe(this@AntivirusScanningActivity) {
                binding.tvPercent.text = "$it%"
            }
            pathObserver.observe(this@AntivirusScanningActivity) {
                binding.tvStatus.text = getString(R.string.string_scanning)
                binding.tvPath.text = it
            }

            virusNumObserver.observe(this@AntivirusScanningActivity) {
                binding.apply {
                    changePageStyle()
                    ivVirus.setBackgroundResource(R.drawable.shape_circle_fa8661)
                    tvVirusNum.setBackgroundResource(R.drawable.shape_circle_red)
                    tvVirusNum.text = "$it"
                    progress2.setIndicatorColor(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_fa8661))
                }
            }

            malWareNumObserver.observe(this@AntivirusScanningActivity) {
                binding.apply {
                    changePageStyle()
                    ivMalware.setBackgroundResource(R.drawable.shape_circle_fa8661)
                    tvMalwareNum.setBackgroundResource(R.drawable.shape_circle_red)
                    tvMalwareNum.text = "$it"
                    progress2.setIndicatorColor(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_fa8661))
                }
            }

            complete.observe(this@AntivirusScanningActivity) {
                runCatching {
                    if (it == 888) {
                        showFullAd {
                            if (virusRiskList.isEmpty()){
                                TbaHelper.eventPost("antivirus_res_page", hashMapOf("res" to "no"))
                                AntivirusResultActivity.start(this@AntivirusScanningActivity, getString(R.string.no_threats_found))
                            }
                            else{
                                TbaHelper.eventPost("antivirus_res_page", hashMapOf("res" to "yes"))
                                AntivirusListActivity.start(this@AntivirusScanningActivity)
                            }
                            finish()
                        }
                    } else {
                        TbaHelper.eventPost("antivirus_scan_error", hashMapOf("code" to it))
                        TbaHelper.eventPost("antivirus_scan_error_popshow")
                        showAntivirusScanError { finish() }
                    }
                }
            }

        }

    }


    private fun changePageStyle() {
        binding.apply {

            runCatching {
                viewLottie.setAnimation("antivirus_yellow.json")
                viewLottie.imageAssetsFolder = "antivirus_yellow"
                viewLottie.playAnimation()
            }

            tvPercent.setTextColor(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_825f1b))
            tvStatus.setTextColor(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_c3af97))
            tvPath.setTextColor(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_c3af97))

            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
            viewBg.setBackgroundResource(R.drawable.shape_scan_page_bg)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTaskProgress(type: Int) = run {
        binding.apply {
            if (type == 0) {
                tvStatus.text = "${getString(R.string.engine_init)}..."
                progress1.isIndeterminate = true
                progress2.isIndeterminate = false
            } else {
                progress1.isIndeterminate = false
                progress1.progress = 100
                progress2.isIndeterminate = true
            }
        }
    }


    private fun onBackClicked() {
        CustomAlertDialog(this).showDialog(title = getString(R.string.string_tips),
                message = getString(R.string.string_scanning_stop_tip_virus),
                positiveButtonText = getString(R.string.string_ok),
                negativeButtonText = getString(R.string.string_cancel),
                onPositiveButtonClick = {
                    it.dismiss()
                    finish()
                },
                onNegativeButtonClick = {})
    }

    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()) {
            b.invoke()
            return
        }

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@AntivirusScanningActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@AntivirusScanningActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@AntivirusScanningActivity)
                b.invoke()
            }
        }
    }


}