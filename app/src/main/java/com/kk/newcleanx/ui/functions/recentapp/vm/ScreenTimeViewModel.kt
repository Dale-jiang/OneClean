package com.kk.newcleanx.ui.functions.recentapp.vm

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.LongSparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.ScreenTimeInfo
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.getApplicationIconDrawable
import com.kk.newcleanx.utils.CommonUtils.getApplicationLabelString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.LinkedList

class ScreenTimeViewModel : ViewModel() {

    private val statsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    private val systemAppLst by lazy { listOf("com.google.android.apps.nexuslauncher", app.packageName, "com.android.settings") }

    val chartLiveData = MutableLiveData<LongSparseArray<Long>>()
    val listLiveData = MutableLiveData<MutableList<ScreenTimeInfo>>()


    fun fetchListData(index: Int) = runCatching {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val timePair = fetchRangeTime(index)
            getRangeTimeInfoList(timePair.first, timePair.second)
        }
    }

    private fun fetchRangeTime(index: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            when (index) {
                2 -> add(Calendar.DAY_OF_MONTH, -1)
                3 -> add(Calendar.DAY_OF_MONTH, -6)
            }
            set(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = when (index) {
            1 -> System.currentTimeMillis()
            2 -> startTime + 86400000
            3 -> System.currentTimeMillis()
            else -> System.currentTimeMillis() - 3600000
        }
        return Pair(startTime, endTime)
    }

    fun getRangeTotalByIndex(index: Int) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val longSparseArray = LongSparseArray<Long>()
            val calendar = Calendar.getInstance()

            val (intervalMillis, size) = when (index) {
                0 -> 60000L to 60
                1 -> 3600000L to 24
                2 -> 86400000L to 7
                3 -> 86400000L to 7
                else -> 3600000L to 1
            }

            val endTime = System.currentTimeMillis()
            val startTime = when (index) {
                0 -> {
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis - intervalMillis * size
                }

                1 -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }

                2, 3 -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis - intervalMillis * size
                }

                else -> endTime - intervalMillis * size
            }

            for (i in 0 until size) {
                val rangeStart = startTime + i * intervalMillis
                val rangeEnd = if (i == size - 1) endTime else rangeStart + intervalMillis
                longSparseArray.append(rangeStart, getRangeTimeTotal(rangeStart, rangeEnd))
            }

            chartLiveData.postValue(longSparseArray)
        }
    }


    private fun getRangeTimeTotal(start: Long, end: Long): Long {
        return runCatching {
            if (end - start > 259200000) { // If interval is greater than 3 days
                getUsageStatsTotal(start, end)
            } else {
                getUsageEventsTotal(start, end)
            }
        }.getOrElse { 0L }
    }

    private fun getUsageStatsTotal(start: Long, end: Long): Long {
        val usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val resultMap = usageStatsList
            .filter { it.totalTimeInForeground > 0L && it.lastTimeUsed > start }
            .groupBy { it.packageName }
            .mapValues { entry -> entry.value.sumOf { it.totalTimeInForeground } }

        return filterSystemPackages(resultMap)
    }

    private fun getUsageEventsTotal(start: Long, end: Long): Long {
        val usageEvents = statsManager.queryEvents(start, end)
        val eventMap = mutableMapOf<String, LinkedList<UsageEvents.Event>>()

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (event.eventType != UsageEvents.Event.ACTIVITY_STOPPED && event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                eventMap.getOrPut(event.packageName) { LinkedList() }.add(event)
            }
        }

        val resultMap = eventMap.mapValues { (_, events) ->
            var totalTime = 0L
            var previousEvent: UsageEvents.Event? = null
            events.forEach { event ->
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> previousEvent = event
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (previousEvent != null) {
                            totalTime += event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                            previousEvent = null
                        }
                    }
                }
            }
            totalTime
        }

        return filterSystemPackages(resultMap)
    }

    private fun filterSystemPackages(resultMap: Map<String, Long>): Long {
        return resultMap.filterKeys { packageName ->
            CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) && !CommonUtils.isSystemLauncher(packageName) }
        }.values.sum()
    }

    private fun getRangeTimeInfoList(start: Long, end: Long) {
        val isLongInterval = end - start > 259200000
        val resultList = if (isLongInterval) {
            getUsageStatsInfoList(start, end)
        } else {
            getUsageEventsInfoList(start, end)
        }
        listLiveData.postValue(resultList)
    }

    private fun getUsageStatsInfoList(start: Long, end: Long): MutableList<ScreenTimeInfo> {
        val usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val resultMap = usageStatsList
            .filter { it.totalTimeInForeground > 0L && it.lastTimeUsed > start }
            .groupBy { it.packageName }
            .mapValues { entry -> entry.value.sumOf { it.totalTimeInForeground } }

        return resultMap.mapNotNull { (packageName, duration) ->
            if (CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) } && !CommonUtils.isSystemLauncher(packageName)) {
                ScreenTimeInfo(getApplicationLabelString(packageName), packageName, getApplicationIconDrawable(packageName), duration)
            } else null
        }.toMutableList()
    }

    private fun getUsageEventsInfoList(start: Long, end: Long): MutableList<ScreenTimeInfo> {
        val usageEvents = statsManager.queryEvents(start, end)
        val eventMap = mutableMapOf<String, LinkedList<UsageEvents.Event>>()

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (event.eventType != UsageEvents.Event.ACTIVITY_STOPPED && event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                eventMap.getOrPut(event.packageName) { LinkedList() }.add(event)
            }
        }

        val resultMap = eventMap.mapValues { (_, events) ->
            var totalTime = 0L
            var previousEvent: UsageEvents.Event? = null
            events.forEach { event ->
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> previousEvent = event
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (previousEvent != null) {
                            totalTime += event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                            previousEvent = null
                        }
                    }
                }
            }
            totalTime
        }
        return resultMap.mapNotNull { (packageName, duration) ->
            if (CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) } && !CommonUtils.isSystemLauncher(packageName)) {
                ScreenTimeInfo(getApplicationLabelString(packageName), packageName, getApplicationIconDrawable(packageName), duration)
            } else null
        }.toMutableList()
    }

}