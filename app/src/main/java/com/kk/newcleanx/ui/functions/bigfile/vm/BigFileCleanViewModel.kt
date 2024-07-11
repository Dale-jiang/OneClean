package com.kk.newcleanx.ui.functions.bigfile.vm

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BigFile
import com.kk.newcleanx.data.local.BigFileFilter
import com.kk.newcleanx.data.local.allBigFiles
import com.kk.newcleanx.data.local.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BigFileCleanViewModel : ViewModel() {

    val typeList = mutableListOf<BigFileFilter>()
    val sizeList = mutableListOf<BigFileFilter>()
    val timeList = mutableListOf<BigFileFilter>()

    val completeObserver = MutableLiveData<Boolean>()

    fun getAllBigFiles() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            allBigFiles.clear()
            queryFiles()
            completeObserver.postValue(true)
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

        val selection = "${MediaStore.Images.Media.SIZE} >= ?" // val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"
        val selectionArgs = arrayOf((10 * 10000 * 10000).toString())
        val cursor: Cursor? = resolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (it.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val size = cursor.getLong(sizeColumn)
                val date = cursor.getLong(dateColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                if (File(path).exists()) {
                    val name = path.substring(path.lastIndexOf("/") + 1, path.length)
                    allBigFiles.add(BigFile(id, name, path, size, date, mimeType))
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