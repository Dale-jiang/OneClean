package com.kk.newcleanx.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.cloakResult
import com.kk.newcleanx.data.local.distinctId
import com.kk.newcleanx.data.local.junkCleanTimeTag
import com.kk.newcleanx.utils.tba.ExceptionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.Calendar
import java.util.LinkedList
import java.util.UUID

object CommonUtils {


    fun isBlackUser() = let {
        cloakResult != "coronet"
    }

    fun isSameDay(time: Long): Boolean = let {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return time >= date && time < date + 86400000
    }


    @SuppressLint("PrivateApi")
    fun getTotalCapacity(context: Context): Double = let {
        val profileClazz = "com.android.internal.os.PowerProfile"
        try {
            val mPowerProfile = Class.forName(profileClazz).getConstructor(Context::class.java).newInstance(context)
            Class.forName(profileClazz).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            0.0
        }
    }

    fun checkIfCanClean(): Boolean = let {
        System.currentTimeMillis() - junkCleanTimeTag >= 2 * 60 * 1000
    }

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

    fun getTotalRamMemory(): Pair<Long, Long> = let {
        val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem
        val availMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availMemory
        (totalMemory to usedMemory)
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

    fun createDistinctId(): String = let {
        var uuid = distinctId
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString().replace("-", "")
            distinctId = uuid
        }
        uuid
    }

    @SuppressLint("HardwareIds")
    fun createAndroidId() = let {
        val id = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
        if ("9774d56d682e549c" == id) return ""
        id ?: ""
    }

    fun createHttpClient() = let {
        OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }).addInterceptor(ExceptionInterceptor()).build()
    }

    fun isPackageInstalled(packageName: String) = let {
        try {
            app.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getFileNameFromPath(filePath: String) = let {
        val file = File(filePath)
        if (file.exists()) {
            file.name
        } else ""
    }

}