package com.kk.newcleanx.ui.functions.admob

import com.google.gson.Gson
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.LOCAL_AD_JSON
import com.kk.newcleanx.data.local.adClickCount
import com.kk.newcleanx.data.local.adClickTime
import com.kk.newcleanx.data.local.adDisplayCount
import com.kk.newcleanx.data.local.adDisplayTime
import com.kk.newcleanx.utils.CommonUtils

object ADManager {

    var adItemList: AdItemList? = null
    private val gson: Gson by lazy { Gson() }

    private var displayMax = 0
    private var clickMax = 0


    val fm_launch = FullScreenAdLoader("fm_launch")

    fun initData(json: String = LOCAL_AD_JSON) {
        runCatching {
            adItemList = gson.fromJson(json, AdItemList::class.java)
        }.onFailure {
            adItemList = null
        }
        dispatcherData()
    }

    private fun dispatcherData() {
        runCatching {
            adItemList?.apply {
                fm_launch.initData(this.fmLaunch)
            }
        }
    }


    fun addClick() = run {
        runCatching {
            if (!CommonUtils.isSameDay(adClickTime)) {
                adClickTime = System.currentTimeMillis()
                adClickCount = 1
            } else adClickCount++
        }
    }

    fun addDisplay() = run {
        runCatching {
            if (!CommonUtils.isSameDay(adDisplayTime)) {
                adDisplayTime = System.currentTimeMillis()
                adDisplayCount = 1
            } else adDisplayCount++
        }
    }

    fun isOverAdMax(): Boolean = let {
        if (0 == displayMax) return false
        val overDisplay = if (CommonUtils.isSameDay(adDisplayTime)) adDisplayCount >= displayMax else false
        val overClick = if (CommonUtils.isSameDay(adClickTime)) adClickCount >= clickMax else false
        (overDisplay || overClick)
    }

    fun notBlocked(): Boolean {

        //todo
        return true
    }


}