package com.kk.newcleanx.ui.clean.vm

import android.os.Environment
import android.util.Log
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
import java.util.Locale

class JunkScanningViewModel : ViewModel() {

    private val cacheFolders = arrayOf("Cache", "leakcanary", ".DS_Store", "cache")
    private val cacheFiles = arrayOf("Analytics", "LOST.DIR", "desktop.ini", ".Trash", ".trash")
    private val cacheFilters = mutableListOf<String>()

    private val logFolders = arrayOf("Logs", "logs", "Bugreport", "bugreports")
    private val logFiles = arrayOf(".log")
    private val logFilters = mutableListOf<String>()

    private val tempFolders = arrayOf("temp", "Temporary", "temporary", "thumbnails?", "albumthumbs?")
    private val tempFiles = arrayOf(".tmp", "thumbs?.db", ".thumb[0-9]", ".thumb")
    private val tempFilters = mutableListOf<String>()

    private val aDJunkFolders = arrayOf("supersonicads", "mobvista", "UnityAdsVideoCache", "splashad", ".spotlight-V100", "fseventsd")
    private val aDJunkFiles = arrayOf(".exo", "splashad")
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
                pathChaneObserver.postValue(singleFile.path)

                if (singleFile.isDirectory) {

                    if (cacheFilters.any { singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.APP_CACHE,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (logFilters.any { singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.LOG_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (tempFilters.any {
                            singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex())
                        }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.TEMP_FILES,
                            select = true
                        )

                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)

                    } else if (aDJunkFilters.any {
                            singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex())
                        }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.AD_JUNK,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else scanningJunk(singleFile)

                } else {

                    if (cacheFilters.any { singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex()) }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.APP_CACHE,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (logFilters.any {
                            singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex())
                        }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.LOG_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (tempFilters.any {
                            singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex())
                        }) {

                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.TEMP_FILES,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (aDJunkFilters.any {
                            singleFile.absolutePath.lowercase(Locale.getDefault()).matches(it.lowercase(Locale.getDefault()).toRegex())
                        }) {
                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
                            fileSize = CommonUtils.getFileSize(singleFile),
                            junkType = JunkType.AD_JUNK,
                            select = true
                        )
                        junkDetailsList.add(item)
                        itemChaneObserver.postValue(item)
                    } else if (singleFile.absolutePath.endsWith(".apk", true) || singleFile.absolutePath.endsWith(".aab", true)) {
                        val item = JunkDetails(
                            fileName = singleFile.name,
                            filePath = singleFile.absolutePath,
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
        cacheFiles.forEach { cacheFilters.add(CommonUtils.getFileJunkRegex(it)) }
        cacheFolders.forEach { cacheFilters.add(CommonUtils.getFolderJunkRegex(it)) }
        Log.e("---->>",cacheFilters.toString())
    }

    private suspend fun createLogJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        logFiles.forEach { logFilters.add(CommonUtils.getFileJunkRegex(it)) }
        logFolders.forEach { logFilters.add(CommonUtils.getFolderJunkRegex(it)) }
        Log.e("---->>",logFilters.toString())
    }

    private suspend fun createTempJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        tempFiles.forEach { tempFilters.add(CommonUtils.getFileJunkRegex(it)) }
        tempFolders.forEach { tempFilters.add(CommonUtils.getFolderJunkRegex(it)) }
        Log.e("---->>",tempFilters.toString())
    }

    private suspend fun createADJunkFilter() = withContext(Dispatchers.IO + SupervisorJob()) {
        aDJunkFiles.forEach { aDJunkFilters.add(CommonUtils.getFileJunkRegex(it)) }
        aDJunkFolders.forEach { aDJunkFilters.add(CommonUtils.getFolderJunkRegex(it)) }
        Log.e("---->>",aDJunkFilters.toString())
    }

}