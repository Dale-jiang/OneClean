package com.kk.newcleanx.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
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