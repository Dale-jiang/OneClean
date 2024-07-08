package com.kk.newcleanx.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.kk.newcleanx.data.local.alreadyRequestStoragePermissions
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.ui.common.PermissionSettingDialogActivity
import com.kk.newcleanx.utils.CommonUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class AllFilePermissionActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected var mBlack: (Boolean) -> Unit = {}

    private val permissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isToSettings = false
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
    }

    override fun onDestroy() {
        super.onDestroy()
        isToSettings = false
    }
}