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

    private var adItemList: AdItemList? = null
    private val gson: Gson by lazy { Gson() }

    private var displayMax = 0
    private var clickMax = 0


    val ocLaunchLoader = FullScreenAdLoader("oc_launch")
    val ocScanIntLoader = FullScreenAdLoader("oc_scan_int")
    val ocCleanIntLoader = FullScreenAdLoader("oc_clean_int")
    val ocScanNatLoader = NativeAdLoader("oc_scan_nat")
    val ocCleanNatLoader = NativeAdLoader("oc_clean_nat")
    val ocMainNatLoader = NativeAdLoader("oc_main_nat")

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
                ocLaunchLoader.initData(this.ocLaunch)
                ocScanIntLoader.initData(this.ocScanInt)
                ocCleanIntLoader.initData(this.ocCleanInt)
                ocScanNatLoader.initData(this.ocScanNat)
                ocCleanNatLoader.initData(this.ocCleanNat)
                ocMainNatLoader.initData(this.ocMainNat)
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

    fun isBlocked(): Boolean {

        //todo
        return false
    }


}