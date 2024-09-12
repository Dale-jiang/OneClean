package com.kk.newcleanx.ui.functions.bigfile.vm

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BigFile
import com.kk.newcleanx.data.local.BigFileFilter
import com.kk.newcleanx.data.local.allBigFiles
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.audioTypeList
import com.kk.newcleanx.data.local.docsTypeList
import com.kk.newcleanx.data.local.imageTypeList
import com.kk.newcleanx.data.local.videoTypeList
import com.kk.newcleanx.data.local.zipTypeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class BigFileCleanViewModel : ViewModel() {

    val typeList = mutableListOf<BigFileFilter>()
    val sizeList = mutableListOf<BigFileFilter>()
    val timeList = mutableListOf<BigFileFilter>()

    val completeObserver = MutableLiveData<MutableList<BigFile>>()

    fun getAllBigFiles() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            allBigFiles.clear()
            queryFiles()
            filterBigFiles()
        }
    }

    fun filterBigFiles() {
        val resultList = mutableListOf<BigFile>()
        allBigFiles.forEach {
            it.select = false
            if (getFileTypeByPosition(getFileType(it.name)) && getFileSizeByPosition(it) && getFileTimeByPosition(it)) {
                resultList.add(it)
            }
        }
        completeObserver.postValue(resultList)
    }

    private fun getFileType(fileName: String): String {
        return if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            fileName.substring(fileName.lastIndexOf(".") + 1).lowercase(Locale.getDefault())
        } else {
            ""
        }
    }

    private fun getFileTypeByPosition(fileType: String): Boolean {
        return when (typeList.indexOfFirst { it.select }) {
            0 -> true
            1 -> imageTypeList.contains(fileType)
            2 -> videoTypeList.contains(fileType)
            3 -> audioTypeList.contains(fileType)
            4 -> docsTypeList.contains(fileType)
            5 -> zipTypeList.contains(fileType)
            6 -> {
                "apk".equals(fileType, true) || "aab".equals(fileType, true)
            }

            7 -> {
                !imageTypeList.contains(fileType) && !videoTypeList.contains(fileType) && !audioTypeList.contains(fileType) && !docsTypeList.contains(fileType) && !zipTypeList.contains(
                    fileType
                ) && !"apk".equals(fileType, true) && !"aab".equals(fileType, true)
            }

            else -> false
        }
    }

    private fun getFileSizeByPosition(bigFile: BigFile): Boolean {
        return when (sizeList.indexOfFirst { it.select }) {
            0 -> bigFile.size > 10L * 1000 * 1000
            1 -> bigFile.size > 50L * 1000 * 1000
            2 -> bigFile.size > 100L * 1000 * 1000
            3 -> bigFile.size > 500L * 1000 * 1000
            4 -> bigFile.size > 1000L * 1000 * 1000
            else -> false
        }
    }

    private fun getFileTimeByPosition(bigFile: BigFile): Boolean {
        return when (timeList.indexOfFirst { it.select }) {
            0 -> true
            1 -> System.currentTimeMillis() - bigFile.date > 604800000L
            2 -> System.currentTimeMillis() - bigFile.date > 1814400000
            3 -> System.currentTimeMillis() - bigFile.date > 7257600000L
            4 -> System.currentTimeMillis() - bigFile.date > 14515200000L
            5 -> System.currentTimeMillis() - bigFile.date > 31449600000L
            else -> false
        }
    }


    private suspend fun queryFiles() = withContext(Dispatchers.IO) {

        val resolver: ContentResolver = app.contentResolver
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = "${MediaStore.Images.Media.SIZE} >= ?"
        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"
        val selectionArgs = arrayOf((10 * 1000 * 1000).toString())
        val cursor: Cursor? = resolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use {

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (it.moveToNext()) {
                val id = cursor.getLongOrNull(idColumn) ?: 0
                val path = cursor.getStringOrNull(pathColumn) ?: ""
                val size = cursor.getLongOrNull(sizeColumn) ?: 0
                val date = cursor.getLongOrNull(dateColumn) ?: 0
                val mimeType = cursor.getStringOrNull(mimeTypeColumn) ?: "*/*"

                if (File(path).exists()) {
                    val name = path.substring(path.lastIndexOf("/") + 1, path.length)
                    allBigFiles.add(BigFile(id, name, path, size, date * 1000, mimeType))
                }

            }
        }
    }


    fun createFilterList() {
        typeList.clear()
        sizeList.clear()
        timeList.clear()

        typeList.add(BigFileFilter(R.string.big_file_all, true))
        typeList.add(BigFileFilter(R.string.big_file_photo, false))
        typeList.add(BigFileFilter(R.string.big_file_video, false))
        typeList.add(BigFileFilter(R.string.big_file_audio, false))
        typeList.add(BigFileFilter(R.string.big_file_docs, false))
        typeList.add(BigFileFilter(R.string.big_file_zip, false))
        typeList.add(BigFileFilter(R.string.big_file_apk, false))
        typeList.add(BigFileFilter(R.string.big_file_others, false))


        sizeList.add(BigFileFilter(R.string.big_file_10, true))
        sizeList.add(BigFileFilter(R.string.big_file_50, false))
        sizeList.add(BigFileFilter(R.string.big_file_100, false))
        sizeList.add(BigFileFilter(R.string.big_file_500, false))
        sizeList.add(BigFileFilter(R.string.big_file_1000, false))

        timeList.add(BigFileFilter(R.string.big_file_all_time, true))
        timeList.add(BigFileFilter(R.string.big_file_one_week, false))
        timeList.add(BigFileFilter(R.string.big_file_one_month, false))
        timeList.add(BigFileFilter(R.string.big_file_three_month, false))
        timeList.add(BigFileFilter(R.string.big_file_six_month, false))
        timeList.add(BigFileFilter(R.string.big_file_one_year, false))

    }


}