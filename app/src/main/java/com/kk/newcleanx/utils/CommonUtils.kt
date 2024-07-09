package com.kk.newcleanx.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.kk.newcleanx.data.local.app

object CommonUtils {

     fun getTotalStorageByManager(): Long = let {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager = app.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            } else getTotalStorageSize()
        } catch (e: Throwable) {
            getTotalStorageSize()
        }
    }

    private fun getTotalStorageSize(): Long = let {
        val statFs = StatFs(Environment.getExternalStorageDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        totalBlocks * blockSize
    }

     fun getUsedStorageByManager(): Long = let {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager = app.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT) - storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
            } else getUsedStorageSizeByStatFs()
        } catch (e: Throwable) {
            getUsedStorageSizeByStatFs()
        }
    }

    private fun getUsedStorageSizeByStatFs(): Long = let {
        val statFs = StatFs(Environment.getExternalStorageDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong
        (totalBlocks - availableBlocks) * blockSize
    }


    val storagePermissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun hasAllStoragePermission(): Boolean = let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) hasAllFilesAccess() else hasStorageAccess()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasAllFilesAccess(): Boolean = let { Environment.isExternalStorageManager() }

    private fun hasStorageAccess(): Boolean = let {
        storagePermissions.all { ContextCompat.checkSelfPermission(app, it) == PackageManager.PERMISSION_GRANTED }
    }
}