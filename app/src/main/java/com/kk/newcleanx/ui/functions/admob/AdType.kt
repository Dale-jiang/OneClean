package com.kk.newcleanx.ui.functions.admob

import com.kk.newcleanx.data.local.AdItemList

sealed class AdType {

    abstract val dataList: MutableList<AdItemList.AdItem>?

    abstract fun loadAd()
}