package com.kk.newcleanx.ui.functions.notice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.util.Log
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CommonUtils.hasNotificationPermission
import com.kk.newcleanx.utils.CommonUtils.shouldStartFrontendService
import com.kk.newcleanx.utils.CoroutineHelper
import com.kk.newcleanx.utils.CoroutineHelper.checkServiceScope
import com.kk.newcleanx.utils.CoroutineHelper.taskCheckScope
import com.kk.newcleanx.utils.CoroutineHelper.timerTaskCheckScope
import com.kk.newcleanx.utils.formatBytes
import com.kk.newcleanx.utils.launchTicker
import com.kk.newcleanx.utils.startFrontNoticeService
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NormalNoticeTask {

    private var previousRxBytes = 0L
    private var previousTxBytes = 0L

    private val unlockReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                CoroutineHelper.launchIO {
                    Log.e("unlockReceiver=>>", "unlockReceiver get ")
                    delay(600L)
                    NormalNoticeManager.noticeConf?.apply {
                        Log.e("unlockReceiver=>>", "noticeConf not null  ")
                        if (NormalNoticeManager.noticeTextList.isNullOrEmpty().not()) {
                            Log.e("unlockReceiver=>>", "noticeTextList not null  ")
                            if (NormalNoticeManager.canShowNotice("unlock", open, this.unlock)) {
                                Log.e("unlockReceiver=>>", "canShowNotice-----")
                                withContext(Dispatchers.Main) {
                                    NormalNoticeManager.showNormalNotice("unlock", NormalNoticeManager.noticeTextList!!.random())
                                }
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        runCatching {
                            app.startFrontNoticeService()
                        }
                    }

                }
            }
        }
    }

    fun initTask(context: Context) = runCatching {

        if (shouldStartFrontendService()) {
            runCatching {
                context.registerReceiver(unlockReceiver, IntentFilter().apply {
                    addAction(Intent.ACTION_USER_PRESENT)
                })
            }
            timerNoticeCheck()
        }

        postSessionBack()
        TbaHelper.eventPost("start_timer_task")
    }

    fun startServiceInterval() = runCatching {
        checkServiceScope.launch {
            while (true) {
                delay(5 * 60 * 1000L)
                withContext(Dispatchers.Main) {
                    app.startFrontNoticeService(false)
                }
            }
        }
    }

    fun startNetWorkTrafficMonitor() {

        if (!hasNotificationPermission()) return

        previousRxBytes = TrafficStats.getTotalRxBytes()
        previousTxBytes = TrafficStats.getTotalTxBytes()

        checkServiceScope.launch {
            while (true) {
                delay(4 * 1000L)
                kotlin.runCatching {
                    val currentRxBytes = TrafficStats.getTotalRxBytes()
                    val currentTxBytes = TrafficStats.getTotalTxBytes()

                    val rxSpeed = (currentRxBytes - previousRxBytes) / 4 // down
                    val txSpeed = (currentTxBytes - previousTxBytes) / 4 // up

                    previousRxBytes = currentRxBytes
                    previousTxBytes = currentTxBytes

                    val rxSpeedStr = rxSpeed.formatBytes()
                    val txSpeedStr = txSpeed.formatBytes()

                    withContext(Dispatchers.Main) {
                        FrontNoticeManager.showNotice("", txSpeedStr to rxSpeedStr)
                    }
                }
            }
        }
    }


    private fun timerNoticeCheck() {
        timerTaskCheckScope.launchTicker(60000L, 60000L, Dispatchers.Main) {
            runCatching {
                NormalNoticeManager.noticeConf?.apply {
                    if (NormalNoticeManager.noticeTextList.isNullOrEmpty().not()) {
                        if (NormalNoticeManager.canShowNotice("timer", open, this.timer)) {
                            NormalNoticeManager.showNormalNotice("timer", NormalNoticeManager.noticeTextList!!.random())
                        }
                    }
                }
            }
        }
    }

    private fun postSessionBack() {
        taskCheckScope.launchTicker(1000L, 5 * 60000L) {
            runCatching {
                TbaHelper.eventPost("sessionb")
            }
        }
    }

}