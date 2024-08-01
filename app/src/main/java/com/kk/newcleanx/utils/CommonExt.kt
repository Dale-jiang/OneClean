package com.kk.newcleanx.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.JunkDetailsType
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.isToSettingPage
import kotlin.math.log10
import kotlin.math.pow

fun Int.dp2px(): Int = let {
    val scale = Resources.getSystem().displayMetrics.density
    (this * scale + 0.5f).toInt()
}

@Suppress("DEPRECATION")
fun Context.getScreenWidth(): Int = let {
    val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getRealSize(point)
    point.x
}

fun Long.formatStorageSize(): String = let {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1000.0)).toInt()
    return String.format("%.1f %s", this / 1000.0.pow(digitGroups.toDouble()), units[digitGroups])

}

fun View.startRotateAnim() = run {
    runCatching {
        this.startAnimation(RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
            duration = 800L
            fillAfter = true
        })
    }
}

fun JunkDetailsType.imageIcon() = run {
    when (junkType) {
        JunkType.APP_CACHE -> R.drawable.junk_app_cache
        JunkType.LOG_FILES -> R.drawable.junk_log
        JunkType.TEMP_FILES -> R.drawable.junk_temp
        JunkType.AD_JUNK -> R.drawable.junk_ad
        JunkType.APK_FILES -> R.drawable.junk_apk
    }
}

fun JunkDetailsType.nameString() = run {
    when (junkType) {
        JunkType.APP_CACHE -> runCatching { app.getString(R.string.app_cache) }.getOrNull() ?: ""
        JunkType.LOG_FILES -> runCatching { app.getString(R.string.log_files) }.getOrNull() ?: ""
        JunkType.TEMP_FILES -> runCatching { app.getString(R.string.temp_files) }.getOrNull() ?: ""
        JunkType.AD_JUNK -> runCatching { app.getString(R.string.ad_junk) }.getOrNull() ?: ""
        JunkType.APK_FILES -> runCatching { app.getString(R.string.apk_files) }.getOrNull() ?: ""
    }
}

fun Context.openAppDetails(packageName: String) = run {
    runCatching {
        isToSettingPage = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}
