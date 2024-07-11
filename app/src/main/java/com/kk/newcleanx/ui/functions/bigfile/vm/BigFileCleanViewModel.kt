package com.kk.newcleanx.ui.functions.bigfile.vm

import androidx.lifecycle.ViewModel
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BigFileFilter

class BigFileCleanViewModel : ViewModel() {

    val typeList = mutableListOf<BigFileFilter>()
    val sizeList = mutableListOf<BigFileFilter>()
    val timeList = mutableListOf<BigFileFilter>()

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