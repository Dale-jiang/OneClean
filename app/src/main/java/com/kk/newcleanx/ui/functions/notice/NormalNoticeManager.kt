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
import com.kk.newcleanx.utils.tba.TbaHelper
import org.json.JSONArray
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
                noticeConf =
                    Gson().fromJson(LOCAL_NOTICE_CONFIG_JSON, NormalNoticeConfig::class.java)
            }
        }
    }

    fun initNoticeText(json: String = LOCAL_NOTICE_TEXT_JSON) {
        runCatching {
            Log.e("initNoticeText==>", "initNoticeText---normal")

            val jsonArray = JSONArray(json)
            val list = mutableListOf<NormalNoticeTextItem>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val functions = jsonObject.getString("octtt")
                val noticeDes = jsonObject.getJSONArray("occcc")
                val desList = mutableListOf<String>()
                for (j in 0 until noticeDes.length()) {
                    desList.add(noticeDes.getString(j))
                }
                val btnStr = jsonObject.getString("ocbbb")
                val item = when (functions) {
                    "clean" -> {
                        NormalNoticeTextItem(
                            functions,
                            desList,
                            btnStr,
                            R.mipmap.ic_notice_clean,
                            R.drawable.svg_notice_clean
                        )
                    }

                    "big" -> {
                        NormalNoticeTextItem(
                            functions,
                            desList,
                            btnStr,
                            R.mipmap.ic_notice_big_file,
                            R.drawable.svg_notice_big_files
                        )
                    }

                    "empty" -> {
                        NormalNoticeTextItem(
                            functions,
                            desList,
                            btnStr,
                            R.mipmap.ic_notoce_empty_folder,
                            R.drawable.svg_notice_empty_folders
                        )
                    }

                    "virus" -> {
                        NormalNoticeTextItem(
                            functions,
                            desList,
                            btnStr,
                            R.mipmap.ic_notice_antivirus,
                            R.drawable.svg_notice_antivirus
                        )
                    }

                    else -> {
                        NormalNoticeTextItem(
                            functions,
                            desList,
                            btnStr,
                            R.mipmap.ic_notice_clean,
                            R.drawable.svg_notice_clean
                        )
                    }
                }

                list.add(item)
            }


            Log.e("initNoticeText==>", "initNoticeText---normal--$list")
            Log.e("initNoticeText==>", "initNoticeText---normal--${list[0].functions}")
            noticeTextList = list

        }.onFailure {
            runCatching {
                Log.e("initNoticeText==>", "initNoticeText---onFailure${it.message}", it)
                val jsonArray = JSONArray(json)
                val list = mutableListOf<NormalNoticeTextItem>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val functions = jsonObject.getString("octtt")
                    val noticeDes = jsonObject.getJSONArray("occcc")
                    val desList = mutableListOf<String>()
                    for (j in 0 until noticeDes.length()) {
                        desList.add(noticeDes.getString(j))
                    }
                    val btnStr = jsonObject.getString("ocbbb")
                    val item = when (functions) {
                        "clean" -> {
                            NormalNoticeTextItem(
                                functions,
                                desList,
                                btnStr,
                                R.mipmap.ic_notice_clean,
                                R.drawable.svg_notice_clean
                            )
                        }

                        "big" -> {
                            NormalNoticeTextItem(
                                functions,
                                desList,
                                btnStr,
                                R.mipmap.ic_notice_big_file,
                                R.drawable.svg_notice_big_files
                            )
                        }

                        "empty" -> {
                            NormalNoticeTextItem(
                                functions,
                                desList,
                                btnStr,
                                R.mipmap.ic_notoce_empty_folder,
                                R.drawable.svg_notice_empty_folders
                            )
                        }

                        "virus" -> {
                            NormalNoticeTextItem(
                                functions,
                                desList,
                                btnStr,
                                R.mipmap.ic_notice_antivirus,
                                R.drawable.svg_notice_antivirus
                            )
                        }

                        else -> {
                            NormalNoticeTextItem(
                                functions,
                                desList,
                                btnStr,
                                R.mipmap.ic_notice_clean,
                                R.drawable.svg_notice_clean
                            )
                        }
                    }

                    list.add(item)
                }
                noticeTextList = list
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


        val des = noticeTextItem.noticeDes.random()
        val page = formatJumpStr(noticeTextItem.functions)
        val noticeType = NoticeType(page, "normal_notice", type, des)
        val largeViews = buildRemoteViews(true, noticeTextItem, noticeType)
        val tinyViews = buildRemoteViews(false, noticeTextItem, noticeType)

        builder.setCustomContentView(tinyViews).setCustomHeadsUpContentView(tinyViews)
            .setCustomBigContentView(largeViews)
        runCatching {
            NotificationManagerCompat.from(app).notify(
                if ("timer" == type) NOTIFICATION_TIMER_ID else NOTIFICATION_UNLOCK_ID,
                builder.build()
            )
            if ("timer" == type) timerNoticeLastShowTime =
                System.currentTimeMillis() else unlockNoticeLastShowTime =
                System.currentTimeMillis()
        }

        runCatching {
            TbaHelper.eventPost(
                "pop_alltrigger",
                hashMapOf("func" to page, "text" to noticeType.des)
            )
            if ("timer" == type) {
                TbaHelper.eventPost(
                    "pop_trigger_timer",
                    hashMapOf("func" to page, "text" to noticeType.des)
                )
            } else {
                TbaHelper.eventPost(
                    "pop_trigger_unlock",
                    hashMapOf("func" to page, "text" to noticeType.des)
                )
            }
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
        PendingIntent.getActivity(
            app,
            Random.nextInt(1200, 8000),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun buildRemoteViews(
        isLarge: Boolean,
        item: NormalNoticeTextItem,
        noticeType: NoticeType
    ): RemoteViews = let {

        RemoteViews(
            app.packageName,
            if (isLarge) R.layout.notice_normal_large else R.layout.notice_normal_tiny
        ).apply {
            setTextViewText(R.id.tv_des, noticeType.des)
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