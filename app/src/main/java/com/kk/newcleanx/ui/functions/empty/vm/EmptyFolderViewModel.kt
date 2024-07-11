package com.kk.newcleanx.ui.functions.empty.vm

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.emptyFoldersDataList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class EmptyFolderViewModel : ViewModel() {

    private val skipFolders = listOf("obb", "data")
    private val extraPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "Android"
    val scanningCompletedObserver = MutableLiveData<Boolean>()
    fun getEmptyFoldersList() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            scanEmptyFolders()
            scanningCompletedObserver.postValue(true)
        }
    }

    private fun scanEmptyFolders(file: File = Environment.getExternalStorageDirectory()) {

        emptyFoldersDataList.clear()

        fun isEmptyFolders(folder: File): Boolean {
            if (folder.canWrite()
                    .not() || folder.absolutePath.equals(extraPath) || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && skipFolders.any {
                    folder.absolutePath.contains(it, true)
                })
            ) return false

            if (!folder.isDirectory) {
                return false
            }

            var isEmpty = true
            val files = folder.listFiles()
            files?.forEach { file ->
                isEmpty = if (file.isDirectory) {
                    isEmpty and isEmptyFolders(file)
                } else false
            }

            if (isEmpty) {
                emptyFoldersDataList.add(folder.absolutePath)
            }
            return isEmpty
        }

        isEmptyFolders(file)
    }


}