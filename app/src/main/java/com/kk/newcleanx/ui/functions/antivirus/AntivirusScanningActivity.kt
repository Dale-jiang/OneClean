package com.kk.newcleanx.ui.functions.antivirus

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcAntivirusScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.ui.functions.antivirus.vm.AntivirusScanningViewModel
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


            lifecycleScope.launch {
                delay(4000)
                viewLottie.setAnimation("antivirus_yellow.json")
                viewLottie.imageAssetsFolder = "antivirus_yellow"

                toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@AntivirusScanningActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
                viewBg.setBackgroundResource(R.drawable.shape_scan_page_bg)

            }

            toolbar.tvTitle.text = getString(R.string.string_antivirus)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { onBackClicked() }
            requestAllFilePermission { //                if (it) {
                //                    setStartAnim()
                //                    startProgress { p ->
                //                        progressBar.progress = p
                //                    }
                //                } else finish()
            }
        }

        initObserver()

    }

    private fun initObserver() {

        viewModel.apply {

        }

    }


    private fun onBackClicked() {
        CustomAlertDialog(this).showDialog(title = getString(R.string.string_tips),
                                           message = getString(R.string.string_scanning_stop_tip),
                                           positiveButtonText = getString(R.string.string_ok),
                                           negativeButtonText = getString(R.string.string_cancel),
                                           onPositiveButtonClick = {
                                               it.dismiss()
                                               finish()
                                           },
                                           onNegativeButtonClick = {})
    }


}