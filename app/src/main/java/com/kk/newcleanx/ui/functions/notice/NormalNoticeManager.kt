package com.kk.newcleanx.ui.functions.notice

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.KEY_NOTICE_FUNCTION
import com.kk.newcleanx.data.local.LOCAL_NOTICE_CONFIG_JSON
import com.kk.newcleanx.data.local.LOCAL_NOTICE_TEXT_JSON
import com.kk.newcleanx.data.local.NormalNoticeConfig
import com.kk.newcleanx.data.local.NormalNoticeConfigItem
import com.kk.newcleanx.data.local.NormalNoticeTextItem
import com.kk.newcleanx.data.local.NoticeType
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.timerNoticeLastShowTime
import com.kk.newcleanx.data.local.unlockNoticeLastShowTime
import com.kk.newcleanx.ui.common.OpenActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.activityReferences
import kotlin.random.Random

object NormalNoticeManager {

    const val NOTIFICATION_TIMER_ID = 18849
    const val NOTIFICATION_UNLOCK_ID = 18850

    private const val CHANNEL_ID = "NORMAL_NOTICE"
    var noticeConf: NormalNoticeConfig? = null
    var noticeTextList: MutableList<NormalNoticeTextItem>? = null

    fun initNoticeConfig(json: String = LOCAL_NOTICE_CONFIG_JSON) {
        runCatching {
            Log.e("initNoticeConfig==>", "initNoticeConfig---normal")
            noticeConf = Gson().fromJson(json, NormalNoticeConfig::class.java)
        }.onFailure {
            runCatching {
                Log.e("initNoticeConfig==>", "initNoticeConfig---onFailure")
                noticeConf = Gson().fromJson(LOCAL_NOTICE_CONFIG_JSON, NormalNoticeConfig::class.java)
            }
        }
    }

    fun initNoticeText(json: String = LOCAL_NOTICE_TEXT_JSON) {
        runCatching {
            Log.e("initNoticeText==>", "initNoticeText---normal")
            val type = object : TypeToken<MutableList<NormalNoticeTextItem>>() {}.type
            val list = Gson().fromJson<MutableList<NormalNoticeTextItem>?>(json, type)
            Log.e("initNoticeText==>", "initNoticeText---normal--$list")
            Log.e("initNoticeText==>", "initNoticeText---normal--${list[0].functions}")
            noticeTextList = list.map {
                Log.e("initNoticeText==>", "initNoticeText---${it}")
                when (it.functions) {
                    "clean" -> {
                        it.smallIconId = R.drawable.svg_notice_clean
                        it.largeIconId = R.mipmap.ic_notice_clean
                    }

                    "big" -> {
                        it.smallIconId = R.drawable.svg_notice_big_files
                        it.largeIconId = R.mipmap.ic_notice_big_file
                    }

                    "empty" -> {
                        it.smallIconId = R.drawable.svg_notice_empty_folders
                        it.largeIconId = R.mipmap.ic_notoce_empty_folder
                    }

                    "virus" -> {
                        it.smallIconId = R.drawable.svg_notice_antivirus
                        it.largeIconId = R.mipmap.ic_notice_antivirus
                    }

                    else -> {
                        it.smallIconId = R.drawable.svg_notice_clean
                        it.largeIconId = R.mipmap.ic_notice_clean
                    }
                }
                it
            }.toMutableList()

        }.onFailure {
            runCatching {
                Log.e("initNoticeText==>", "initNoticeText---onFailure${it.message}", it)
                val type = object : TypeToken<MutableList<NormalNoticeTextItem>>() {}.type
                val list = Gson().fromJson<MutableList<NormalNoticeTextItem>?>(json, type)
                noticeTextList = list.map {
                    when (it.functions) {
                        "clean" -> {
                            it.smallIconId = R.drawable.svg_notice_clean
                            it.largeIconId = R.mipmap.ic_notice_clean
                        }

                        "big" -> {
                            it.smallIconId = R.drawable.svg_notice_big_files
                            it.largeIconId = R.mipmap.ic_notice_big_file
                        }

                        "empty" -> {
                            it.smallIconId = R.drawable.svg_notice_empty_folders
                            it.largeIconId = R.mipmap.ic_notoce_empty_folder
                        }

                        "virus" -> {
                            it.smallIconId = R.drawable.svg_notice_antivirus
                            it.largeIconId = R.mipmap.ic_notice_antivirus
                        }

                        else -> {
                            it.smallIconId = R.drawable.svg_notice_clean
                            it.largeIconId = R.mipmap.ic_notice_clean
                        }
                    }
                    it
                }.toMutableList()
            }
        }
    }

    fun canShowNotice(type: String, open: Int, item: NormalNoticeConfigItem?): Boolean {

        Log.e("canShowNotice==>", "notice type $type")

        if (!CommonUtils.hasNotificationPermission()) {
            Log.e("canShowNotice==>", "has no notification permission")
            return false
        }

        if (open == 0) {
            Log.e("canShowNotice==>", "open is closed")
            return false
        }

        if (activityReferences > 0) {
            Log.e("canShowNotice==>", "current is foreground ,has $activityReferences actives ")
            return false
        }

        if (item == null) {
            Log.e("canShowNotice==>", "no notice data get")
            return false
        }

        if (!canShowFirst(item)) {
            Log.e("canShowNotice==>", "can not show first ${item.first}")
            return false
        }

        if (!isNotInCd(type, item)) {
            Log.e("canShowNotice==>", "now is in cd: ${item.repeat}")
            return false
        }
        return true
    }


    @SuppressLint("MissingPermission")
    fun showNormalNotice(type: String, noticeTextItem: NormalNoticeTextItem) {
        buildChannel()
        val builder = NotificationCompat.Builder(app, CHANNEL_ID)
                .setSmallIcon(R.drawable.notice_small_icon)
                .setAutoCancel(true)
                .setGroupSummary(false)
                .setGroup("NORMAL")
                .setVibrate(null)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val largeViews = buildRemoteViews(true, type, noticeTextItem)
        val tinyViews = buildRemoteViews(false, type, noticeTextItem)

        builder.setCustomContentView(tinyViews).setCustomHeadsUpContentView(tinyViews).setCustomBigContentView(largeViews)
        runCatching {
            NotificationManagerCompat.from(app).notify(if ("timer" == type) NOTIFICATION_TIMER_ID else NOTIFICATION_UNLOCK_ID, builder.build())
            if ("timer" == type) timerNoticeLastShowTime = System.currentTimeMillis() else unlockNoticeLastShowTime = System.currentTimeMillis()
        }

    }

    private fun buildChannel() = run {
        NotificationManagerCompat.from(app).createNotificationChannel(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                        .setSound(null, null)
                        .setLightsEnabled(false)
                        .setVibrationEnabled(false)
                        .setShowBadge(true)
                        .setName(CHANNEL_ID)
                        .build()
        )
    }


    private fun buildClickPendingIntent(noticeType: NoticeType) = let {
        val intent = Intent(app, OpenActivity::class.java).apply {
            Log.e("buildClickPendingIntent==>", "buildClickPendingIntent---${noticeType}")
            putExtra(KEY_NOTICE_FUNCTION, noticeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        PendingIntent.getActivity(app, Random.nextInt(1200, 8000), intent, PendingIntent.FLAG_IMMUTABLE)
    }


    private fun buildRemoteViews(isLarge: Boolean, type: String, item: NormalNoticeTextItem): RemoteViews = let {

        RemoteViews(app.packageName, if (isLarge) R.layout.notice_front_large else R.layout.notice_normal_tiny).apply {

            val des = item.noticeDes.random()
            val page = formatJumpStr(item.functions)
            val noticeType = NoticeType(page, "normal_notice", type, des)
            Log.e("buildClickPendingIntent==>", "buildClickPendingIntent---${page}")

            setTextViewText(R.id.tv_des, des)
            setTextViewText(R.id.tv_btn, item.btnStr)
            setImageViewResource(R.id.iv_logo, if (isLarge) item.largeIconId else item.smallIconId)
            setOnClickPendingIntent(R.id.root, buildClickPendingIntent(noticeType))
        }

    }

    private fun formatJumpStr(str: String): String {
        return when (str) {
            "clean" -> "clean"
            "big" -> "big_file"
            "empty" -> "empty_folder"
            "virus" -> "antivirus"
            else -> ""
        }
    }

    private fun canShowFirst(item: NormalNoticeConfigItem): Boolean {
        if (item.first == 0) return true
        return (System.currentTimeMillis() - CommonUtils.getFirInstallTime()) >= item.first * 60000L
    }

    private fun isNotInCd(type: String, item: NormalNoticeConfigItem): Boolean {
        if (0 == item.repeat) return true
        val time = if ("timer" == type) timerNoticeLastShowTime else unlockNoticeLastShowTime
        return (System.currentTimeMillis() - time) > item.repeat * 60000L
    }


}