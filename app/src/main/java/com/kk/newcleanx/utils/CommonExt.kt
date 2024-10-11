package com.kk.newcleanx.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.JunkDetailsType
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.hasShowAntivirusTips
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.DialogAntivirusNoticeBinding
import com.kk.newcleanx.databinding.DialogDateRangeChoseBinding
import com.kk.newcleanx.databinding.DialogVirusDeleteBinding
import com.kk.newcleanx.databinding.DialogVirusScanErrorBinding
import com.kk.newcleanx.ui.common.WebViewActivity
import com.kk.newcleanx.ui.functions.notice.FrontNoticeManager
import com.kk.newcleanx.ui.functions.notice.FrontNoticeService
import com.kk.newcleanx.utils.CommonUtils.getDateRangeNameByIndex
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun Context.startFrontNoticeService() = run {
    if (CommonUtils.isAtLeastAndroid14() || (CommonUtils.isAtLeastAndroid12() && this is Application)) {
        runCatching {
            FrontNoticeManager.showNotice("normal_notice")
            TbaHelper.eventPost("normal_notice")
        }
    } else {
        runCatching {
            ContextCompat.startForegroundService(this, Intent(this, FrontNoticeService::class.java))
            TbaHelper.eventPost("foreground_notice")
        }
    }
}

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
    return String.format(Locale.getDefault(), "%.1f %s", this / 1000.0.pow(digitGroups.toDouble()), units[digitGroups])

}

fun Long.formatTime(format: String = "yyyy/MM/dd"): String = let { SimpleDateFormat(format, Locale.getDefault()).format(Date(this)) }

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

fun HashMap<String, Any?>.toBundle(): Bundle? = let {
    if (this.isNotEmpty()) {
        val bundle = Bundle()
        this.forEach { (t, u) ->
            when (u) {
                is String -> bundle.putString(t, u)
                is Int -> bundle.putInt(t, u)
                is Long -> bundle.putLong(t, u)
                is Boolean -> bundle.putBoolean(t, u)
                is Double -> bundle.putDouble(t, u)
                is Float -> bundle.putFloat(t, u)
            }
        }
        bundle
    } else null
}

fun Context.opFiles(path: String) = runCatching {
    val file = File(path)
    val uri = FileProvider.getUriForFile(app, "${BuildConfig.APPLICATION_ID}.provider", file)
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.toString())
    val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    if ("application/vnd.android.package-archive" == mimetype || file.name.endsWith(".apk")) {
        Toast.makeText(this, getString(R.string.install_error_tip), Toast.LENGTH_SHORT).show()
    } else {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, mimetype)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        isToSettings = true
        this.startActivity(intent)
    }
}.onFailure {
    Toast.makeText(this, getString(R.string.open_failed), Toast.LENGTH_SHORT).show()
}


fun Activity.showDateRangeSelector(callback: (value: Int, text: String) -> Unit) {
    val binding = DialogDateRangeChoseBinding.inflate(LayoutInflater.from(this), window.decorView as ViewGroup, false)
    val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomAlertDialog).apply {
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        create()
    }
    val listData = listOf(getDateRangeNameByIndex(0), getDateRangeNameByIndex(1), getDateRangeNameByIndex(2), getDateRangeNameByIndex(3))

    binding.apply {
        last60min.text = listData[0]
        today.text = listData[1]
        yesterday.text = listData[2]
        last7days.text = listData[3]

        last60min.setOnClickListener {
            callback(0, getDateRangeNameByIndex(0))
            bottomSheetDialog.dismiss()
        }
        today.setOnClickListener {
            callback(1, getDateRangeNameByIndex(1))
            bottomSheetDialog.dismiss()
        }
        yesterday.setOnClickListener {
            callback(2, getDateRangeNameByIndex(2))
            bottomSheetDialog.dismiss()
        }
        last7days.setOnClickListener {
            callback(3, getDateRangeNameByIndex(3))
            bottomSheetDialog.dismiss()
        }

    }

    bottomSheetDialog.show()
}


fun Double.format(): String {
    if (this.isNaN()) return "0"
    return String.format(Locale.US, "%.1f", this)
}

fun String.removeEndSuffix(suffix: String = ".0") = if (this.endsWith(suffix)) this.dropLast(suffix.length) else this
fun String.removeStartPrefix(prefix: String = "0") = if (this.startsWith(prefix)) this.drop(prefix.length) else this
fun Long.formatDuration(): String {
    val seconds = this / 1000.0
    val hours = seconds / 3600
    val minutes = seconds / 60

    return when {
        hours >= 1.0 -> "${hours.format().removeEndSuffix()} h"
        minutes >= 1.0 -> "${minutes.format().removeEndSuffix()} m"
        seconds >= 1.0 -> "${seconds.format().removeEndSuffix()} s"
        else -> "${this % 1000} ms"
    }
}


fun CoroutineScope.launchTicker(
    first: Long,
    interval: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    functionNext: () -> Unit,
) = launch(dispatcher) {
    flow {
        delay(first)
        while (true) {
            emit(Unit)
            delay(interval)
        }
    }.flowOn(Dispatchers.IO).collect { functionNext() }
}