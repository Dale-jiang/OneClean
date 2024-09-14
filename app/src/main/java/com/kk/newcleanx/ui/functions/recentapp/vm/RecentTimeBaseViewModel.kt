package com.kk.newcleanx.ui.functions.recentapp.vm

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.LongSparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kk.newcleanx.data.local.ScreenTimeInfo
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.getApplicationIconDrawable
import com.kk.newcleanx.utils.CommonUtils.getApplicationLabelString
import java.util.LinkedList

abstract class RecentTimeBaseViewModel : ViewModel() {

    val chartLiveData = MutableLiveData<LongSparseArray<Long>>()
    val listLiveData = MutableLiveData<MutableList<ScreenTimeInfo>>()

    protected val statsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    protected val systemAppLst by lazy {
        listOf(
            app.packageName,
            "com.android.settings",
            "com.android.launcher",           //  Launcher
            "com.google.android.apps.nexuslauncher",  // Google Pixel Launcher
            "com.samsung.android.launcher",    // Samsung Launcher
            "com.miui.home",                   // MIUI Launcher
            "com.huawei.android.launcher",     // Huawei Launcher
            "com.oppo.launcher",               // OPPO Launcher
            "com.vivo.launcher",             // Vivo Launcher
        )
    }

    protected fun getRangeTimeInfoList(start: Long, end: Long) = runCatching {
        if (end - start > 259200000) {
            val resultMap = hashMapOf<String, Long>()
            val usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            usageStatsList.forEach { usageStats ->
                if (usageStats.totalTimeInForeground > 0L && usageStats.lastTimeUsed > start) {
                    val duration = resultMap.getOrDefault(usageStats.packageName, 0L) + usageStats.totalTimeInForeground
                    resultMap[usageStats.packageName] = duration
                }
            }
            val resultList = mutableListOf<ScreenTimeInfo>()
            resultMap.forEach { (packageName, duration) ->
                if (CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) }) {
                    resultList.add(ScreenTimeInfo(getApplicationLabelString(packageName), packageName, getApplicationIconDrawable(packageName), duration))
                }
            }
            listLiveData.postValue(resultList)
        } else {
            val usageEvents = statsManager.queryEvents(start, end)
            val allEventLinkedList: LinkedList<LinkedList<UsageEvents.Event>> = LinkedList()
            while (usageEvents != null && usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                if (usageEvents.getNextEvent(event) && event.eventType != UsageEvents.Event.ACTIVITY_STOPPED && event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                    if (allEventLinkedList.isNotEmpty() && allEventLinkedList.last.last.packageName == event.packageName) {
                        allEventLinkedList.last.addLast(event)
                    } else {
                        val pkgEventList: LinkedList<UsageEvents.Event> = LinkedList()
                        pkgEventList.addLast(event)
                        allEventLinkedList.addLast(pkgEventList)
                    }
                }
            }
            val resultMap = hashMapOf<String, Long>()
            allEventLinkedList.forEach { eventList ->
                val packageName = eventList.first.packageName
                if (CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) }) {
                    var previousEvent: UsageEvents.Event? = null
                    var totalTime = 0L
                    eventList.forEach { event ->
                        if (UsageEvents.Event.ACTIVITY_RESUMED == event.eventType && null == previousEvent) {
                            previousEvent = event
                        } else if (UsageEvents.Event.ACTIVITY_PAUSED == event.eventType && null != previousEvent) {
                            val duration = event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                            if (duration > 0L) totalTime += duration
                            previousEvent = null
                        }
                    }
                    if (totalTime > 0L) {
                        if (resultMap.containsKey(packageName)) {
                            resultMap[packageName] = resultMap.getOrDefault(packageName, 0L) + totalTime
                        } else resultMap[packageName] = totalTime
                    }
                }
            }

            val resultList = mutableListOf<ScreenTimeInfo>()
            resultMap.forEach { (packageName, duration) ->
                resultList.add(ScreenTimeInfo(getApplicationLabelString(packageName), packageName, getApplicationIconDrawable(packageName), duration))
            }
            listLiveData.postValue(resultList)
        }
    }

}