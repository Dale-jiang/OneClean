package com.kk.newcleanx.ui.common

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.databinding.AcMainBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class MainActivity : AllFilePermissionActivity<AcMainBinding>() {


    private var animator: Animator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnScan.setOnClickListener { //                    requestAllFilePermission {
            //                        Toast.makeText(this, if (it) "success" else "failed", Toast.LENGTH_LONG).show()
            //                    }

            startLoading()

        }

        startLoading()
    }


    @SuppressLint("SetTextI18n")
    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {

                val total = CommonUtils.getTotalStorageByManager()
                val use = CommonUtils.getUsedStorageByManager()

                totalStorage.text = "/${total.formatStorageSize()}"
                usedStorage.text = use.formatStorageSize()
                val usePercent = ((use / total.toFloat()) * 100).toInt()
                percent.text = "${usePercent}%"

                progressBar.isIndeterminate = true
                delay(2200L)

                setCircleProgress(usePercent)
            }
        }
    }

    private fun setCircleProgress(end: Int) {
        binding.progressBar.isIndeterminate = false
        animator = ValueAnimator.ofInt(0, end).apply {
            duration = 500L * ceil(end.toDouble() / 25.toDouble()).toLong()
            addUpdateListener {
                (it.animatedValue as? Int)?.apply {
                    binding.progressBar.progress = this
                }
            }
        }
        animator?.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
    }

}