package com.kk.newcleanx.ui.base

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.alreadyRequestStoragePermissions
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.ui.common.PermissionSettingDialogActivity
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.utils.CommonUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class AllFilePermissionActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected var mBlack: (Boolean) -> Unit = {}
    protected var mProgressBlack: (Int) -> Unit = {}

    private val handler = Handler(Looper.getMainLooper())
    protected var isCompleted = false
    private var animator: ValueAnimator? = null

    private val permissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
       // isToSettings = false
        if (CommonUtils.hasAllStoragePermission()) {
            mBlack(true)
        } else mBlack(false)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (CommonUtils.hasAllStoragePermission()) mBlack(true) else mBlack(false)
    }

    fun requestAllFilePermission(block: (Boolean) -> Unit) {
        this.mBlack = block
        if (CommonUtils.hasAllStoragePermission()) {
            mBlack(true)
            return
        }
        showRequestDialog {
            if (it) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    isToSettings = true
                    permissionResult.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${this@AllFilePermissionActivity.packageName}")
                    })

                    lifecycleScope.launch {
                        delay(400)
                        startActivity(Intent(this@AllFilePermissionActivity, PermissionSettingDialogActivity::class.java))
                    }

                } else {
                    if (alreadyRequestStoragePermissions.not()) {
                        alreadyRequestStoragePermissions = true
                        permissionLauncher.launch(CommonUtils.storagePermissions)
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        permissionLauncher.launch(CommonUtils.storagePermissions)
                    } else {
                        isToSettings = true
                        permissionResult.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${this@AllFilePermissionActivity.packageName}")
                        })
                    }
                }
            } else mBlack(false)
        }

    }


    private fun showRequestDialog(block: (Boolean) -> Unit) {
        CustomAlertDialog(this).showDialog(title = getString(R.string.app_name),
                                           message = getString(R.string.grant_permission_to_use),
                                           positiveButtonText = getString(R.string.string_ok),
                                           negativeButtonText = getString(R.string.string_cancel),
                                           onPositiveButtonClick = {
                                               block(true)
                                               it.dismiss()
                                           },
                                           onNegativeButtonClick = {
                                               block(false)
                                           })
    }

    protected fun startProgress(
        maxTime: Long = 10000L,
        progressUpdateInterval: Long = 50L,
        elapsedProgress: Int = 80,
        endDuration: Long = 500,
        minWaitTime: Long = 0L,
        block: (Int) -> Unit
    ) {
        this.mProgressBlack = block
        val startTime = System.currentTimeMillis()
        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = if (elapsedTime < maxTime) {
                    (elapsedTime.toFloat() / maxTime * elapsedProgress).toInt()
                } else elapsedProgress

                mProgressBlack(progress)
                if (isCompleted && elapsedTime > minWaitTime) {
                    animateProgressTo100(endDuration, progress)
                } else {
                    handler.postDelayed(this, progressUpdateInterval)
                }
            }
        })
    }

    private fun animateProgressTo100(endDuration: Long, progress: Int) {
        animator = ValueAnimator.ofInt(progress, 100)
        animator?.apply {
            duration = endDuration
            addUpdateListener { animation ->
                mProgressBlack(animation.animatedValue as Int)
            }
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        animator?.cancel()
        animator = null
//        isToSettings = false
    }
}