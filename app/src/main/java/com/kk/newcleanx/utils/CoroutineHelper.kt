package com.kk.newcleanx.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object CoroutineHelper {

    private val iOScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    private val mainScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    fun launchIO(block: suspend () -> Unit): Job {
        return iOScope.launch {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun launchMain(block: suspend () -> Unit): Job {
        return mainScope.launch {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}