package com.kk.newcleanx.ui.functions.recentapp.vm

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.launch
import java.util.Calendar

class ScreenTimeViewModel : ViewModel() {

    private val statsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    private val systemAppLst by lazy { listOf("com.google.android.apps.nexuslauncher", app.packageName, "com.android.settings") }

    val chartLiveData = MutableLiveData<LongSparseArray<Long>>()
    val listLiveData = MutableLiveData<MutableList<ScreenTimeInfo>>()


    fun fetchListData(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val timePair = fetchRangeTime(index)
                getRangeTimeInfoList(timePair.first, timePair.second)
            }.onFailure { e ->
                Log.e("ScreenTimeVM", "Error fetching list data", e)
            }
        }
    }

    private fun fetchRangeTime(index: Int): Pair<Long, Long> {
        try {
            val calendar = Calendar.getInstance()
            return when (index) {
                1 -> {
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    calendar.timeInMillis to System.currentTimeMillis()
                }

                2 -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    calendar.timeInMillis to calendar.timeInMillis + 86400000
                }

                3 -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -6)
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    calendar.timeInMillis to System.currentTimeMillis()
                }

                else -> {
                    val current = System.currentTimeMillis()
                    current - 3600000 to current
                }
            }
        } catch (e: Throwable) {
            return 0L to 0L
        }

    }

    fun getRangeTotalByIndex(index: Int) = runCatching {
        viewModelScope.launch(Dispatchers.IO) {
            val endList = generateEndList(index)
            val longSparseArray = LongSparseArray<Long>()

            for (i in endList.indices) {
                val startTime = endList[i]
                val endTime = if (i < endList.size - 1) endList[i + 1] else System.currentTimeMillis()
                longSparseArray.append(startTime, getRangeTimeTotal(startTime, endTime))
            }
            chartLiveData.postValue(longSparseArray)
        }
    }

    private fun generateEndList(index: Int): List<Long> {
        val calendar = Calendar.getInstance().apply {
            when (index) {
                0 -> set(Calendar.SECOND, 0)
                1, 2 -> {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                }

                3 -> set(Calendar.HOUR_OF_DAY, 0)
            }
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val timeInMillis = calendar.timeInMillis
        val interval = when (index) {
            0 -> 60000L // 1 minute
            1, 2 -> 3600000L // 1 hour
            3 -> 86400000L // 1 day
            else -> 3600000L
        }

        val size = when (index) {
            0 -> 60
            1, 2 -> 24
            3 -> 7
            else -> 1
        }

        return List(size) { timeInMillis - it * interval }
    }

    private fun getRangeTimeTotal(start: Long, end: Long): Long {
        return runCatching {
            if (end - start > 259200000) { // Interval greater than 3 days
                statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                    .filter { it.totalTimeInForeground > 0L && it.lastTimeUsed > start }
                    .sumOf { if (isValidPackage(it.packageName)) it.totalTimeInForeground else 0L }
            } else {
                queryUsageEvents(start, end)
            }
        }.getOrElse { 0L }
    }


    private fun queryUsageEvents(start: Long, end: Long): Long {
        val usageEvents = statsManager.queryEvents(start, end)
        var total = 0L
        var previousEvent: UsageEvents.Event? = null

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                previousEvent = event
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && previousEvent != null) {
                total += (event.timeStamp - previousEvent.timeStamp).takeIf { it > 0 } ?: 0L
                previousEvent = null
            }
        }
        return total
    }

    private fun getRangeTimeInfoList(start: Long, end: Long) {
        if (end - start > 259200000) {
            handleUsageStatsQuery(start, end)
        } else {
            handleUsageEventsQuery(start, end)
        }
    }

    private fun handleUsageStatsQuery(start: Long, end: Long) {
        val usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val resultList = usageStatsList.filter { it.totalTimeInForeground > 0L && it.lastTimeUsed > start }
            .mapNotNull { usageStats ->
                if (isValidPackage(usageStats.packageName)) {
                    ScreenTimeInfo(
                        getApplicationLabelString(usageStats.packageName),
                        usageStats.packageName,
                        getApplicationIconDrawable(usageStats.packageName),
                        usageStats.totalTimeInForeground
                    )
                } else null
            }

        listLiveData.postValue(resultList.toMutableList())
    }

    private fun handleUsageEventsQuery(start: Long, end: Long) {
        val usageEvents = statsManager.queryEvents(start, end)
        val resultMap = mutableMapOf<String, Long>()
        var previousEvent: UsageEvents.Event? = null

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                previousEvent = event
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && previousEvent != null) {
                val duration = event.timeStamp - previousEvent.timeStamp
                if (duration > 0L) {
                    resultMap[event.packageName] = resultMap.getOrDefault(event.packageName, 0L) + duration
                }
                previousEvent = null
            }
        }

        val resultList = resultMap.mapNotNull { (packageName, duration) ->
            if (isValidPackage(packageName)) {
                ScreenTimeInfo(
                    getApplicationLabelString(packageName),
                    packageName,
                    getApplicationIconDrawable(packageName),
                    duration
                )
            } else null
        }

        listLiveData.postValue(resultList.toMutableList())
    }

    private fun isValidPackage(packageName: String) = CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it.contains(packageName) }

}