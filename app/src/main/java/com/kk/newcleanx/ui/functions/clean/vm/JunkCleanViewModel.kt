package com.kk.newcleanx.ui.functions.clean.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kk.newcleanx.data.local.JunkDetails
import com.kk.newcleanx.data.local.emptyFoldersDataList
import com.kk.newcleanx.data.local.junkDataList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class JunkCleanViewModel : ViewModel() {

    //val progressObserver = MutableLiveData<Int>()
    val completeObserver = MutableLiveData<Boolean>()
    fun cleanJunk() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            junkDataList.filterIsInstance<JunkDetails>().forEachIndexed { index, t ->
                if (t.select) File(t.filePath).deleteRecursively() // progressObserver.postValue(index + 1)
            }
            completeObserver.postValue(true)
            junkDataList.clear()
        }
    }

    fun cleanEmptyFolders() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            emptyFoldersDataList.forEach {
                File(it).deleteRecursively()
            }
            completeObserver.postValue(true)
            emptyFoldersDataList.clear()
        }
    }


}