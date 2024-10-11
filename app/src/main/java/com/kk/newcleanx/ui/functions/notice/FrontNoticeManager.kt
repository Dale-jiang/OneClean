package com.kk.newcleanx.ui.functions.notice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.KEY_NOTICE_FUNCTION
import com.kk.newcleanx.data.local.NoticeType
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.ui.common.OpenActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlin.random.Random

object FrontNoticeManager {

    const val NOTIFICATION_ID = 18848
    private const val CHANNEL_ID = "NOTICE_TOOLS"

    private fun buildChannel() = run {
        NotificationManagerCompat.from(app).createNotificationChannel(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT).setSound(null, null).setLightsEnabled(false)
                        .setVibrationEnabled(false).setShowBadge(false).setName(CHANNEL_ID).build()
        )
    }

    private fun buildRemoteViews(isLarge: Boolean): RemoteViews = let {
        RemoteViews(app.packageName, if (isLarge) R.layout.notice_front_large else R.layout.notice_front_tiny).apply {

            if (isLarge) {
                setTextViewText(R.id.tv_clean, app.getString(R.string.string_clean))
                setTextViewText(R.id.tv_antivirus, app.getString(R.string.string_antivirus))
                setTextViewText(R.id.tv_big_files, app.getString(R.string.big_files))
                setTextViewText(R.id.tv_empty_folder, app.getString(R.string.empty_folders))
            }

            setOnClickPendingIntent(R.id.clean, buildClickPendingIntent(NoticeType("clean", "front_notice")))
            setOnClickPendingIntent(R.id.antivirus, buildClickPendingIntent(NoticeType("antivirus", "front_notice")))
            setOnClickPendingIntent(R.id.big_files, buildClickPendingIntent(NoticeType("big_file", "front_notice")))
            setOnClickPendingIntent(R.id.empty_folder, buildClickPendingIntent(NoticeType("empty_folder", "front_notice")))
        }
    }

    private fun buildClickPendingIntent(noticeType: NoticeType) = let {
        val intent = Intent(app, OpenActivity::class.java).apply {
            putExtra(KEY_NOTICE_FUNCTION, noticeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        PendingIntent.getActivity(app, Random.nextInt(1200, 8000), intent, PendingIntent.FLAG_IMMUTABLE)
    }


    @SuppressLint("MissingPermission")
    fun showNotice(type:String="normal_notice"): Notification = run {
        buildChannel()
        val largeViews = buildRemoteViews(true)
        val tinyViews = buildRemoteViews(false)
        val builder = NotificationCompat.Builder(app, CHANNEL_ID).setSmallIcon(R.drawable.notice_small_icon).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE).setOnlyAlertOnce(true).setGroupSummary(false).setGroup("FRONT").setSound(null).setOngoing(true)
        if (CommonUtils.isMiUI() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setCustomContentView(tinyViews).setCustomBigContentView(largeViews).setCustomHeadsUpContentView(largeViews)
        } else {
            builder.setCustomContentView(largeViews).setCustomBigContentView(largeViews).setCustomHeadsUpContentView(largeViews)
        }
        val notification = builder.build()
        runCatching {
            NotificationManagerCompat.from(app).notify(NOTIFICATION_ID, notification)
            TbaHelper.eventPost("front_notice_type", hashMapOf("nt_type" to type))
        }
        return notification
    }

}