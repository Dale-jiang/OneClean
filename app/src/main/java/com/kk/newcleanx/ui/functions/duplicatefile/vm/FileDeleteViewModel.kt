package com.kk.newcleanx.ui.functions.duplicatefile.vm

import android.media.MediaScannerConnection
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kk.newcleanx.data.local.JunkDetails
import com.kk.newcleanx.data.local.allBigFiles
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.duplicateFiles
import com.kk.newcleanx.data.local.emptyFoldersDataList
import com.kk.newcleanx.data.local.junkDataList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class FileDeleteViewModel : ViewModel() {

    val completeObserver = MutableLiveData<Boolean>()

    fun cleanDuplicateFiles() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            duplicateFiles.forEach {
                if (it.select) {
                    File(it.path).deleteRecursively()
                    MediaScannerConnection.scanFile(app, arrayOf(it.path), null) { _, _ -> }
                }
            }
            completeObserver.postValue(true)
            duplicateFiles.removeIf { it.select }
        }
    }


}