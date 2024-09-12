package com.kk.newcleanx.ui.functions.duplicatefile.vm

import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.DuplicateFile
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.duplicateFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class DuplicateFileCleanViewModel : ViewModel() {

    val completeObserver = MutableLiveData<Boolean>()
    private var searchJob: Job? = null

    fun getDuplicateFileList() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            duplicateFiles.clear()
            val fileList = scanFiles()
            getDuplicateFile(fileList)
            Log.e("-------->>>", "-----completeObserver--->>>")
            completeObserver.postValue(true)
        }
    }

    private fun scanFiles(): MutableList<DuplicateFile> {
        val list = mutableListOf<DuplicateFile>()
        val fileUri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} ASC"
        val resolver = app.contentResolver

        resolver?.query(fileUri, projection, null, null, sortOrder)?.use { cursor ->
            if (cursor.moveToLast()) {

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                do {
                    val id = cursor.getLongOrNull(idColumn) ?: 0
                    val path = cursor.getStringOrNull(dataColumn) ?: ""
                    val size = cursor.getLongOrNull(sizeColumn) ?: 0
                    val date = cursor.getLongOrNull(dateColumn) ?: 0
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "*/*"

                    if (size > 0 && File(path).exists()) {
                        val name = path.substring(path.lastIndexOf("/") + 1, path.length)
                        list.add(DuplicateFile(id, name, path, size, date * 1000, mimeType))
                    }

                } while (cursor.moveToPrevious())
            }
        }

        return list
    }

    private suspend fun getDuplicateFile(fileList: MutableList<DuplicateFile>) = withContext(Dispatchers.IO + SupervisorJob()) {
        if (fileList.isEmpty()) return@withContext
        val originalList = fileList.groupBy { it.size }.filterValues { it.size > 1 }.values.flatten().filter {
            val file = File(it.path)
            file.exists() && file.isDirectory.not() && file.canWrite()
        }
        val filesMap = originalList.map { async { hashFile(it) } }.map { it.await() }.groupBy { it.fileHash }.filterValues { it.size > 1 }
        val sortedByFileSize = filesMap.entries.sortedByDescending { it.value.firstOrNull()?.size ?: 0L }.associate { it.toPair() }
        val finalList = sortedByFileSize.values.flatten()
        var hash = ""
        finalList.forEach { duplicate ->
            if (duplicate.fileHash != hash) {
                duplicateFiles.lastOrNull()?.itemType = 2
                duplicate.isNewest = true
                duplicate.select = false
                duplicate.itemType = 0
                hash = duplicate.fileHash
            } else {
                duplicate.itemType = 1
            }
            duplicateFiles.add(duplicate)
        }
        duplicateFiles.lastOrNull()?.itemType = 2

    }


    private suspend fun hashFile(duplicateFile: DuplicateFile) = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            val file = File(duplicateFile.path)
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                val buffer = ByteArray(1024)
                if (file.length() > 20 * 1000 * 1000) {
                    val bytesRead = fis.read(buffer)
                    if (bytesRead != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                } else {
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
            }

            val fileHash = "${file.length()}-${digest.digest().joinToString("") { "%02x".format(it) }}"
            duplicateFile.fileHash = fileHash
            duplicateFile
        } catch (e: Throwable) {
            e.printStackTrace()
            duplicateFile
        }
    }


    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

}