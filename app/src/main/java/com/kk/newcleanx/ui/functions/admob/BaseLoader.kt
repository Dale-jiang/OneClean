package com.kk.newcleanx.ui.functions.admob

import com.kk.newcleanx.data.local.AdItemList

open class BaseLoader(val where: String) {
    var onLoaded: ((Boolean) -> Unit)? = null
    val mAdItems: MutableList<AdItemList.AdItem> = mutableListOf()
    val adLoadList: MutableList<AdType> = mutableListOf()
    var isLoading = false
}