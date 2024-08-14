package com.kk.newcleanx.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.JunkDetailsType
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.hasShowAntivirusTips
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.DialogAntivirusNoticeBinding
import com.kk.newcleanx.databinding.DialogVirusDeleteBinding
import com.kk.newcleanx.databinding.DialogVirusScanErrorBinding
import com.kk.newcleanx.ui.common.WebViewActivity
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
        isToSettings = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}

fun Context.isPackageInstalled(packageName: String): Boolean = run {
    try {
        app.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Activity.showAntivirusNotice(done: (Boolean) -> Unit) {

    if (hasShowAntivirusTips) {
        done.invoke(true)
        return
    }
    val binding = DialogAntivirusNoticeBinding.inflate(layoutInflater, window.decorView as ViewGroup, false)
    val dialog = AlertDialog.Builder(this).setView(binding.root).setCancelable(false).create()
    dialog.setCanceledOnTouchOutside(false)

    val spanned = Html.fromHtml(getString(R.string.trust_look_privacy_tips), Html.FROM_HTML_MODE_COMPACT)
    val spannable = SpannableString(spanned)

    val spans = spannable.getSpans(0, spanned.length, ClickableSpan::class.java)
    for (span in spans) {
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        val flags = spannable.getSpanFlags(span)
        spannable.removeSpan(span)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebViewActivity.start(this@showAntivirusNotice, "https://www.trustlook.com/privacy-policy")
            }
        }, start, end, flags)
    }

    binding.tvContent.text = spannable
    binding.tvContent.movementMethod = LinkMovementMethod.getInstance()

    binding.ivClose.setOnClickListener {
        done.invoke(false)
        dialog.dismiss()
    }
    binding.agree.setOnClickListener {
        hasShowAntivirusTips = true
        dialog.dismiss()
        done.invoke(true)
    }

    dialog.window?.decorView?.background = null
    dialog.window?.setLayout(getScreenWidth() - 88.dp2px(), ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.show()

}

fun Activity.showAntivirusScanError(done: () -> Unit) {
    val binding = DialogVirusScanErrorBinding.inflate(layoutInflater, window.decorView as ViewGroup, false)
    val dialog = AlertDialog.Builder(this).setView(binding.root).setCancelable(false).create()
    dialog.setCanceledOnTouchOutside(false)
    binding.positiveButton.setOnClickListener {
        dialog.dismiss()
        done.invoke()
    }
    dialog.window?.decorView?.background = null
    dialog.window?.setLayout(getScreenWidth() - 46.dp2px(), ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.show()
}

fun Activity.showAntivirusDelete(done: () -> Unit) {
    val binding = DialogVirusDeleteBinding.inflate(layoutInflater, window.decorView as ViewGroup, false)
    val dialog = AlertDialog.Builder(this).setView(binding.root).setCancelable(true).create()
    dialog.setCanceledOnTouchOutside(true)

    binding.negativeButton.setOnClickListener {
        dialog.dismiss()
    }

    binding.positiveButton.setOnClickListener {
        dialog.dismiss()
        done.invoke()
    }
    dialog.window?.decorView?.background = null
    dialog.window?.setLayout(getScreenWidth() - 46.dp2px(), ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.show()
}
