package com.kk.newcleanx.ui.functions.notice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.kk.newcleanx.ui.functions.notice.FrontNoticeManager.NOTIFICATION_ID

class FrontNoticeService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
//        startFrontNotice()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startFrontNotice()
        return START_STICKY
    }

    @SuppressLint("InlinedApi")
    private fun startFrontNotice() {
        runCatching {
            ServiceCompat.startForeground(
                this@FrontNoticeService,
                NOTIFICATION_ID,
                FrontNoticeManager.showNotice("foreground_notice", "" to ""),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        }.onFailure {
            ServiceCompat.startForeground(
                this@FrontNoticeService,
                NOTIFICATION_ID,
                FrontNoticeManager.showNotice("foreground_notice", "" to ""),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        }
    }

}