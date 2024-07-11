package com.kk.newcleanx.data.local

import com.kk.newcleanx.MyAPP
import com.kk.newcleanx.R

lateinit var app: MyAPP
var isToSettings = false

val main_list_data = arrayListOf(
    MainFunction(R.string.free_up_space, -1, ""),
    MainFunction(R.string.big_file_clean, R.drawable.big_file_clean, BIG_FILE_CLEAN),
    MainFunction(R.string.app_manager, R.drawable.app_manager, APP_MANAGER),
    MainFunction(R.string.device_status, R.drawable.device_status, DEVICE_STATUS),
    MainFunction(R.string.empty_folder, R.drawable.empty_folder, EMPTY_FOLDER)
)

enum class JunkType() {
    LOG_FILES(), TEMP_FILES(), APP_CACHE(), AD_JUNK(), APK_FILES()
}

enum class CleanType() {
    JunkType(), EmptyFolderType(), BigFileType()
}

val junkDataList = mutableListOf<JunkDetailsType>()
val emptyFoldersDataList = mutableListOf<String>()

