package com.kk.newcleanx.ui.clean

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcJunkScanningBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.utils.startRotateAnim

class JunkScanningActivity : AllFilePermissionActivity<AcJunkScanningBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, JunkScanningActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@JunkScanningActivity, R.color.color_83401b), PorterDuff.Mode.SRC_IN)
            toolbar.tvTitle.text = getString(R.string.string_scanning)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            onBackPressedDispatcher.addCallback { onBackClicked() }
            requestAllFilePermission {
                if (it) {
                    setStartAnim()
                } else finish()
            }

        }
    }


    private fun setStartAnim() {
        binding.apply {
            ivCacheLoading.startRotateAnim()
            ivApkLoading.startRotateAnim()
            ivLogLoading.startRotateAnim()
            ivAdLoading.startRotateAnim()
            ivTempLoading.startRotateAnim()
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