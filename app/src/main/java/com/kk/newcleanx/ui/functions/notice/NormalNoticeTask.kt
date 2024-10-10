package com.kk.newcleanx.ui.functions.notice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CoroutineHelper
import com.kk.newcleanx.utils.CoroutineHelper.taskCheckScope
import com.kk.newcleanx.utils.CoroutineHelper.timerTaskCheckScope
import com.kk.newcleanx.utils.launchTicker
import com.kk.newcleanx.utils.startFrontNoticeService
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object NormalNoticeTask {

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

    fun initTask(context: Context) {
        runCatching {
            context.registerReceiver(unlockReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
            })
        }
        timerNoticeCheck()
        postSessionBack()
        TbaHelper.eventPost("start_timer_task")
    }

    private fun timerNoticeCheck() {
        timerTaskCheckScope.launchTicker(60000L, 60000L) {
            runCatching {
                app.startFrontNoticeService()
            }
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