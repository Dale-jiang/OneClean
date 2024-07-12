package com.kk.newcleanx.ui.functions.admob

import com.google.gson.Gson
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.LOCAL_AD_JSON

object ADManager {

    private var adItemList: AdItemList? = null
    private val gson: Gson by lazy { Gson() }
    fun initData(json: String = LOCAL_AD_JSON) {
        runCatching {
            adItemList = gson.fromJson(json, AdItemList::class.java)
        }.onFailure {
            adItemList = null
        }
        dispatcherData()
    }

    private fun dispatcherData() {

    }


}