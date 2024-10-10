package com.kk.newcleanx.ui.functions.notice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.kk.newcleanx.ui.functions.notice.FrontNoticeManager.NOTIFICATION_ID

class FrontNoticeService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startFrontNotice()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            startForeground(NOTIFICATION_ID, FrontNoticeManager.showNotice())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startFrontNotice() {
        runCatching {
            startForeground(NOTIFICATION_ID, FrontNoticeManager.showNotice())
        }
    }

}