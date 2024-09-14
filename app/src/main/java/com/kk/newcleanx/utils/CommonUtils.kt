package com.kk.newcleanx.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.cloakResult
import com.kk.newcleanx.data.local.distinctId
import com.kk.newcleanx.data.local.junkCleanTimeTag
import okhttp3.OkHttpClient
import java.io.File
import java.util.Calendar
import java.util.LinkedList
import java.util.UUID

object CommonUtils {

    fun isTestMode() = let { BuildConfig.DEBUG }
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
        OkHttpClient.Builder().build()
    }

    fun isPackageInstalled(packageName: String) = let {
        try {
            app.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getFileNameFromPath(filePath: String): String = let {
        val file = File(filePath)
        if (file.exists()) {
            file.name
        } else ""
    }

    fun getFirInstallTime(): Long = let {
        try {
            app.packageManager.getPackageInfo(app.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            0L
        }
    }

    fun getLastUpdateTime(): Long = let {
        try {
            app.packageManager.getPackageInfo(app.packageName, 0).lastUpdateTime
        } catch (e: Exception) {
            0L
        }
    }

    fun getAdapterClassName(responseInfo: ResponseInfo?) = kotlin.runCatching {
        val mediationClzName = responseInfo?.mediationAdapterClassName ?: "admob"
        when {
            mediationClzName.contains("facebook", true) -> "facebook"
            mediationClzName.contains("applovin", true) -> "applovin"
            mediationClzName.contains("pangle", true) -> "pangle"
            mediationClzName.contains("mbridge.msdk", true) -> "mintegral"
            mediationClzName.contains("mintegral", true) -> "mintegral"
            else -> "admob"
        }
    }.getOrNull() ?: "admob"

    fun getAdTypeName(adType: String) = let {
        when (adType) {
            "int" -> "interstitial"
            "op" -> "open"
            "nat" -> "native"
            else -> "unknown"
        }
    }

    fun getPrecisionType(precisionTypeCode: Int) = let {
        when (precisionTypeCode) {
            AdValue.PrecisionType.ESTIMATED -> "ESTIMATED"
            AdValue.PrecisionType.PUBLISHER_PROVIDED -> "PUBLISHER_PROVIDED"
            AdValue.PrecisionType.PRECISE -> "PRECISE"
            else -> "UNKNOWN"
        }
    }

    fun isMiUI(): Boolean = let {
        val brands = listOf("redmi", "xiaomi")
        val manufacturer = Build.MANUFACTURER ?: ""
        if (brands.any { manufacturer.contains(it, true) }) return true
        val brand = Build.BRAND ?: ""
        brands.any { brand.contains(it, true) }
    }

    fun isAtLeastAndroid8() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isAtLeastAndroid10() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    fun isAtLeastAndroid11() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    fun isAtLeastAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    fun isAtLeastAndroid13() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    fun isAtLeastAndroid14() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    fun hasNotificationPermission() = let {
        if (isAtLeastAndroid13()) {
            ContextCompat.checkSelfPermission(app, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(app).areNotificationsEnabled()
        }
    }


    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(): Boolean {
        val appOps = app.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        return try {
            val mode = if (isAtLeastAndroid10()) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), app.packageName)
            } else {
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), app.packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getApplicationLabelString(packageName: String) =
        runCatching { app.packageManager.getApplicationLabel(app.packageManager.getApplicationInfo(packageName, 0)).toString() }.getOrNull() ?: ""

    fun getApplicationIconDrawable(packageName: String) = runCatching { app.packageManager.getApplicationIcon(packageName) }.getOrNull()

    fun isEnableStop(packageName: String): Boolean {
        if (isGoogleApplication(packageName) || isSystemApplication(packageName)) return false
        return isApplicationCanStop(packageName)
    }

    fun isApplicationCanStop(packageName: String): Boolean {
        return runCatching {
            val appInfo = app.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_STOPPED) == 0
        }.getOrNull() ?: false
    }

    fun isSystemApplication(packageName: String): Boolean {
        return runCatching {
            val appInfo = app.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrNull() ?: false
    }

    fun isGoogleApplication(packageName: String): Boolean {
        return packageName == "com.android.vending" || packageName.contains("google")
    }

    fun getDateRangeNameByIndex(index: Int): String {
        val calendar = Calendar.getInstance()
        val currentFormattedTime = System.currentTimeMillis().formatTime()
        return when (index) {
            3 -> app.getString(R.string.string_last7days)
            2 -> {
                calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                "${app.getString(R.string.string_yesterday)} (${calendar.timeInMillis.formatTime()})"
            }

            1 -> "${app.getString(R.string.string_today)} ($currentFormattedTime)"
            else -> app.getString(R.string.string_last60min)
        }
    }

    fun getDateRangePairByIndex(index: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()
        return when (index) {
            3 -> {
                calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis to currentTime
            }

            2 -> {
                calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                startOfDay to calendar.timeInMillis
            }

            1 -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis to currentTime
            }

            else -> {
                (currentTime - 60 * 60000L) to currentTime
            }
        }
    }


//    fun isSystemLauncher(packageName: String): Boolean {
//        val intent = Intent(Intent.ACTION_MAIN).apply {
//            addCategory(Intent.CATEGORY_HOME)
//        }
//
//        val resolveInfoList = app.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//        return resolveInfoList.any { it.activityInfo.packageName == packageName }
//    }


}