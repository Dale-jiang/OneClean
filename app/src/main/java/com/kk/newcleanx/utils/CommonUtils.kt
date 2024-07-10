package com.kk.newcleanx.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.kk.newcleanx.data.local.app
import java.io.File
import java.util.LinkedList

object CommonUtils {


     fun getApkIcon(apkFilePath: String): Drawable? = let {
        try {
            val packageManager = app.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES)
            packageInfo?.applicationInfo?.apply {
                sourceDir = apkFilePath
                publicSourceDir = apkFilePath
                return@let packageManager.getApplicationIcon(this)
            }
            return@let null
        } catch (e: Throwable) {
            return@let null
        }
    }

    fun getFileSize(file: File): Long = let {
        val pool = LinkedList<File>().also { it.offer(file) }
        var size = 0L
        while (pool.isNotEmpty()) {
            val pop = pool.pop()
            if (!pop.exists()) continue
            if (pop.isDirectory) {
                pop.listFiles()?.forEach {
                    pool.offer(it)
                }
                size += 4096
                continue
            }
            size += pop.length()
        }
        size
    }

    fun getFolderJunkRegex(folder: String): String = let {
        ".*(\\\\|/)$folder(\\\\|/|$).*"
    }

    fun getFileJunkRegex(file: String): String = let {
        ".+" + file.replace(".", "\\.") + "$"
    }


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