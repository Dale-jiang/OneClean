package com.kk.newcleanx.data.local

import com.kk.newcleanx.MyAPP
import com.kk.newcleanx.R

lateinit var app: MyAPP
var isToSettings = false

val main_list_data = arrayListOf(
    MainFunction(R.string.free_up_space, -1, ""),
    MainFunction(R.string.string_antivirus, R.drawable.scan_antivirus, SCAN_ANTIVIRUS),
    MainFunction(R.string.big_file_clean, R.drawable.big_file_clean, BIG_FILE_CLEAN),
    MainFunction(R.string.app_manager, R.drawable.app_manager, APP_MANAGER),
    MainFunction(R.string.device_status, R.drawable.device_status, DEVICE_STATUS),
    MainFunction(R.string.empty_folder, R.drawable.empty_folder, EMPTY_FOLDER)
)

val imageTypeList = arrayOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "heif", "raw", "tiff", "svg", "rwa")
val videoTypeList = arrayOf("3gp", "mkv", "avi", "flv", "mp4", "wmv", "mov", "rmvb", "webm", "mpeg", "hevc")
val audioTypeList = arrayOf("mp3", "ogg", "flac", "mid", "wav", "m4a", "aac", "wma", "aiff", "midi")
val docsTypeList = arrayOf("doc", "docx", "xlsx", "pptx", "pdf", "txt", "rtf", "html", "xml", "csv", "xls", "ods", "ppt", "odp", "pages")
val zipTypeList = arrayOf("zip", "rar", "7z", "xz", "gz", "bz2", "tar", "iso", "cab")


val buyUserTags by lazy { setOf("adjust", "not%20set", "youtubeads", "bytedance", "%7B%22", "fb4a", "facebook", "gclid") }


enum class JunkType {
    LOG_FILES, TEMP_FILES, APP_CACHE, AD_JUNK, APK_FILES
}

enum class CleanType {
    JunkType, EmptyFolderType, BigFileType
}

val junkDataList = mutableListOf<JunkDetailsType>()
val emptyFoldersDataList = mutableListOf<String>()
val allBigFiles = mutableListOf<BigFile>()
val virusRiskList = mutableListOf<VirusBean>()

