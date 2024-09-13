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
        val foregroundEvents = hashMapOf<String, MutableList<UsageEvents.Event>>()
        val eventGroups = LinkedList<LinkedList<UsageEvents.Event>>()
        val launchesHashMap = hashMapOf<String, Int>()
        var lastEvent: UsageEvents.Event? = null

        val queryEvents = statsManager.queryEvents(start, end)
        while (queryEvents?.hasNextEvent() == true) {
            val event = UsageEvents.Event()
            if (queryEvents.getNextEvent(event)) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_STOPPED || event.eventType == UsageEvents.Event.STANDBY_BUCKET_CHANGED) continue

                // Group events by package name
                if (eventGroups.isNotEmpty() && eventGroups.last.last.packageName == event.packageName) {
                    eventGroups.last.add(event)
                } else {
                    eventGroups.add(LinkedList<UsageEvents.Event>().apply { add(event) })
                }

                // Record foreground events
                if (lastEvent == null || (lastEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED)) {
                    foregroundEvents.getOrPut(event.packageName) { mutableListOf() }.add(event)
                }

                lastEvent = event
            }
        }

        // Count launches for each package
        eventGroups.forEach { eventList ->
            if (eventList.size > 1) {
                val packageName = eventList.first.packageName
                launchesHashMap[packageName] = launchesHashMap.getOrDefault(packageName, 0) + 1
            }
        }

        // Filter system apps early
        val systemApps = setOf(app.packageName, "com.android.mms", "com.android.contacts", "com.android.settings")
        val resultList = mutableListOf<LaunchesItem>()

        foregroundEvents.forEach { (packageName, events) ->
            if (CommonUtils.isPackageInstalled(packageName) && !systemApps.contains(packageName)) {
                val totalLaunches = launchesHashMap.getOrDefault(packageName, events.size)
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

        return resultList
    }

}