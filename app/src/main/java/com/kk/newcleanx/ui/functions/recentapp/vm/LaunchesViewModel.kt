package com.kk.newcleanx.ui.functions.recentapp.vm

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.LaunchesItem
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList

class LaunchesViewModel : ViewModel() {

    private val statsManager by lazy { app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    val launchesListLiveData = MutableLiveData<MutableList<LaunchesItem>>()

    fun queryAppLaunches(start: Long, end: Long) = runCatching {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { getAppLaunches(start, end) }.getOrNull() ?: mutableListOf()
            launchesListLiveData.postValue(result)
        }
    }

    private fun getAppLaunches(start: Long, end: Long): MutableList<LaunchesItem> {
        try {
            val foregroundEventsMap = mutableMapOf<String, MutableList<UsageEvents.Event>>()
            val eventGroups = LinkedList<LinkedList<UsageEvents.Event>>()
            var lastEvent: UsageEvents.Event? = null

            val queryEvents = statsManager.queryEvents(start, end)
            while (queryEvents?.hasNextEvent() == true) {
                val event = UsageEvents.Event()
                if (queryEvents.getNextEvent(event) &&
                    event.eventType != UsageEvents.Event.ACTIVITY_STOPPED &&
                    event.eventType != UsageEvents.Event.STANDBY_BUCKET_CHANGED
                ) {
                    if (eventGroups.isNotEmpty() && eventGroups.last.last.packageName == event.packageName) {
                        eventGroups.last.addLast(event)
                    } else {
                        eventGroups.add(LinkedList<UsageEvents.Event>().apply { add(event) })
                    }

                    if (lastEvent?.packageName != event.packageName &&
                        (lastEvent == null || lastEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) &&
                        event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                    ) {
                        foregroundEventsMap.getOrPut(event.packageName) { mutableListOf() }.add(event)
                    }
                    lastEvent = event
                }
            }

            val launchCountMap = eventGroups
                .filter { it.size > 1 }
                .groupingBy { it.first.packageName }
                .eachCount()

            val resultList = mutableListOf<LaunchesItem>()
            foregroundEventsMap.forEach { (packageName, events) ->
                if (CommonUtils.isPackageInstalled(packageName)) {
                    val totalLaunches = maxOf(launchCountMap.getOrDefault(packageName, 0), events.size)
                    resultList.add(
                        LaunchesItem(
                            CommonUtils.getApplicationLabelString(packageName),
                            packageName,
                            CommonUtils.getApplicationIconDrawable(packageName),
                            totalLaunches,
                            events.size,
                            totalLaunches - events.size
                        )
                    )
                }
            }

            val systemAppList = setOf(app.packageName, "com.android.mms", "com.android.contacts", "com.android.settings")
            return resultList.filterNot { systemAppList.contains(it.packageName) }.toMutableList()
        } catch (e: Throwable) {
            return mutableListOf()
        }
    }

}