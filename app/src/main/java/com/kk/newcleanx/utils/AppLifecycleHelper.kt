package com.kk.newcleanx.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.ui.common.MainActivity
import com.kk.newcleanx.ui.common.OpenActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.LinkedList


private val any = Any()
private val activityStack = LinkedList<Activity>()
private var activityJob: Job? = null
private var isHotStart = false
private var activityReferences = 0

class AppLifecycleHelper : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        synchronized(any) {
            activityStack.add(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        activityReferences++
        activityJob?.cancel()
        if (isHotStart) {
            isHotStart = false
            if (isToSettings) {
                isToSettings = false
                return
            }
            activity.startActivity(Intent(activity, OpenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        } else isToSettings = false

    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        activityReferences--
        activityJob?.cancel()
        if (activityReferences <= 0) {
            activityJob = CoroutineHelper.launchIO {
                delay(3000L)
                isHotStart = true
                activityStack.forEach {
                    if (it !is MainActivity) it.finish()
                }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}