package com.kk.newcleanx.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

object CoroutineHelper {

    private val availableProcessors = Runtime.getRuntime().availableProcessors()
    private val poolSize = availableProcessors * 2
    private val customIODispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(poolSize).asCoroutineDispatcher()

    private val iOScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun launchIO(block: suspend () -> Unit): Job {
        return iOScope.launch(customIODispatcher) {
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