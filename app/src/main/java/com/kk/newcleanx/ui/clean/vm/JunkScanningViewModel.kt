package com.kk.newcleanx.ui.clean.vm

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.JunkDetails
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class JunkScanningViewModel : ViewModel() {

    private val cacheFolders = arrayOf("cache", "Analytics", "LOST.DIR", ".Trash", "desktop.ini", "leakcanary", ".DS_Store", "fseventsd", ".cache")
    private val cacheFilters = mutableListOf<String>()

    private val logFolders = arrayOf("logs", "Bugreport", "bugreports", "debug_log", "MiPushLog")
    private val logFilters = mutableListOf<String>()

    private val tempFolders = arrayOf("temp", "temporary", "thumbnails?", ".thumbnails")
    private val tempFiles = arrayOf("thumbs?.db", ".thumb[0-9]")
    private val tempFilters = mutableListOf<String>()

    private val aDJunkFolders = arrayOf("supersonicads", "UnityAdsVideoCache", ".spotlight-V100", "splashad")
    private val aDJunkFiles = arrayOf("splashad", ".exo")
    private val aDJunkFilters = mutableListOf<String>()

    var scanningJob: Job? = null
    val junkDetailsList = mutableListOf<JunkDetails>()
    val pathChaneObserver = MutableLiveData<String>()
    val scanningCompletedObserver = MutableLiveData<Boolean>()
    val itemChaneObserver = MutableLiveData<JunkDetails>()

    fun getAllJunk() {
        scanningJob?.cancel()
        scanningJob = viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val task1 = async { createCacheJunkFilter() }
            val task2 = async { createLogJunkFilter() }
            val task3 = async { createTempJunkFilter() }
            val task4 = async { createADJunkFilter() }
            task1.await()
            task2.await()
            task3.await()
            task4.await()
            scanningJunk()
            scanningCompletedObserver.postValue(true)
        }
    }

    private fun scanningJunk(file: File = Environment.getExternalStorageDirectory()) {
        val files = file.listFiles()
        if (files != null) {
            for (singleFile in files) {

                if (singleFile.exists().not()) continue
                val filePath = singleFile.absolutePath
                pathChaneObserver.postValue(filePath)

                if (singleFile.isDirectory) {

                    if (cacheFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.APP_CACHE,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (logFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.LOG_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (tempFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.TEMP_FILES,
                            select = true
                        )

                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (aDJunkFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.AD_JUNK,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else scanningJunk(singleFile)

                } else {

                    if (cacheFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.APP_CACHE,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (filePath.endsWith(".log", true) || logFilters.any { filePath.matches(it.toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.LOG_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (filePath.contains(".thumb", true) || filePath.endsWith(
                            ".tmp", true
                        ) || tempFilters.any { filePath.matches(it.toRegex()) }
                    ) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.TEMP_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (aDJunkFilters.any { filePath.matches(it.toRegex()) }) {
                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.AD_JUNK,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (filePath.endsWith(".apk", true) || filePath.endsWith(".aab", true)) {
                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = filePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.APK_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    }
                }
            }
        }

    }


    private suspend fun createCacheJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        cacheFolders.forEach { cacheFilters.add(CommonUtils.getFolderJunkRegex(it)) }
    }

    private suspend fun createLogJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        logFolders.forEach { logFilters.add(CommonUtils.getFolderJunkRegex(it)) }
    }

    private suspend fun createTempJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        tempFiles.forEach { tempFilters.add(CommonUtils.getFileJunkRegex(it)) }
        tempFolders.forEach { tempFilters.add(CommonUtils.getFolderJunkRegex(it)) }
    }

    private suspend fun createADJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        aDJunkFiles.forEach { aDJunkFilters.add(CommonUtils.getFileJunkRegex(it)) }
        aDJunkFolders.forEach { aDJunkFilters.add(CommonUtils.getFolderJunkRegex(it)) }
    }

}