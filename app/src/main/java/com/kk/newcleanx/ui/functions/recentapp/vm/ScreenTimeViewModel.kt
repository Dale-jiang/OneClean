package com.kk.newcleanx.ui.functions.recentapp.vm

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.util.LongSparseArray
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.LinkedList

class ScreenTimeViewModel : RecentTimeBaseViewModel() {


    fun fetchListData(index: Int) = runCatching {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val timePair = fetchRangeTime(index)
            getRangeTimeInfoList(timePair.first, timePair.second)
        }
    }

    private fun fetchRangeTime(index: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        val currentTime = System.currentTimeMillis()
        val startTime: Long

        when (index) {
            1 -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }

            2 -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
                return Pair(startTime, startTime + 86400000)
            }

            3 -> {
                calendar.add(Calendar.DAY_OF_MONTH, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }

            else -> {
                return Pair(currentTime - 3600000, currentTime)
            }
        }

        return Pair(startTime, currentTime)
    }

    fun getRangeTotalByIndex(index: Int) = run {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            when (index) {
                0 -> {
                    kotlin.runCatching {
                        val timeInMillis = Calendar.getInstance().apply {
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val endList = mutableListOf<Long>()
                        for (i in 0..59) {
                            endList.add(timeInMillis - i * 60000)
                        }
                        val longSparseArray = LongSparseArray<Long>()
                        var current = endList.size
                        while (--current >= 0) {
                            val startTime = endList[current]
                            val endTime = if (current > 0) endList[current - 1] else System.currentTimeMillis()
                            longSparseArray.append(startTime, getRangeTimeTotal(startTime, endTime))
                        }
                        chartLiveData.postValue(longSparseArray)
                    }
                }

                1 -> {
                    kotlin.runCatching {
                        val timeInMillis = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val endList = mutableListOf<Long>()
                        for (i in 0..24) {
                            endList.add(timeInMillis - i * 3600000)
                        }
                        val longSparseArray = LongSparseArray<Long>()
                        var current = endList.size
                        while (--current > 0) {
                            val startTime = endList[current]
                            val endTime = endList[current - 1]
                            longSparseArray.append(startTime, getRangeTimeTotal(startTime, endTime))
                        }
                        chartLiveData.postValue(longSparseArray)
                    }

                }

                2 -> {
                    kotlin.runCatching {
                        val timeInMillis = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val endList = mutableListOf<Long>()
                        for (i in 0..24) {
                            endList.add(timeInMillis - i * 3600000)
                        }
                        val longSparseArray = LongSparseArray<Long>()
                        var current = endList.size
                        while (--current > 0) {
                            val startTime = endList[current]
                            val endTime = endList[current - 1]
                            longSparseArray.append(startTime, getRangeTimeTotal(startTime, endTime))
                        }
                        chartLiveData.postValue(longSparseArray)
                    }

                }

                3 -> {
                    kotlin.runCatching {
                        val timeInMillis = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val endList = mutableListOf<Long>()
                        for (i in 0..6) {
                            endList.add(timeInMillis - i * 86400000)
                        }
                        val longSparseArray = LongSparseArray<Long>()
                        var current = endList.size
                        while (--current >= 0) {
                            val startTime = endList[current]
                            val endTime = if (current > 0) endList[current - 1] else System.currentTimeMillis()
                            longSparseArray.append(startTime, getRangeTimeTotal(startTime, endTime))
                        }
                        chartLiveData.postValue(longSparseArray)
                    }
                }
            }
        }
    }


    private fun getRangeTimeTotal(start: Long, end: Long): Long = runCatching {
        if (end - start > 259200000) {
            calculateTotalForegroundTime(start, end)
        } else {
            calculateTotalEventTime(start, end)
        }
    }.getOrNull() ?: 0L

    private fun calculateTotalForegroundTime(start: Long, end: Long): Long {
        val resultMap = hashMapOf<String, Long>()
        val usageStatsList = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)

        usageStatsList.forEach { usageStats ->
            if (usageStats.totalTimeInForeground > 0L && usageStats.lastTimeUsed > start) {
                resultMap[usageStats.packageName] = resultMap.getOrDefault(usageStats.packageName, 0L) + usageStats.totalTimeInForeground
            }
        }

        return calculateTotalFromMap(resultMap)
    }

    private fun calculateTotalEventTime(start: Long, end: Long): Long {
        val usageEvents = statsManager.queryEvents(start, end)
        val allEventLinkedList = LinkedList<UsageEvents.Event>()

        while (usageEvents != null && usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            if (usageEvents.getNextEvent(event) && event.eventType in listOf(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED)) {
                allEventLinkedList.add(event)
            }
        }

        val resultMap = hashMapOf<String, Long>()
        var previousEvent: UsageEvents.Event? = null

        allEventLinkedList.forEach { event ->
            val packageName = event.packageName
            if ( CommonUtils.isPackageInstalled(packageName) && !systemAppLst.any { it == packageName }) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    previousEvent = event
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && previousEvent != null) {
                    val duration = event.timeStamp - (previousEvent?.timeStamp ?: 0L)
                    if (duration > 0L) {
                        resultMap[packageName] = resultMap.getOrDefault(packageName, 0L) + duration
                    }
                    previousEvent = null
                }
            }
        }

        return calculateTotalFromMap(resultMap)
    }

    private fun calculateTotalFromMap(resultMap: HashMap<String, Long>): Long {
        var total = 0L
        resultMap.forEach { (packageName, duration) ->
            if ( CommonUtils.isPackageInstalled(packageName) && !systemAppLst.contains(packageName)) {
                total += duration
            }
        }
        return total
    }


}
