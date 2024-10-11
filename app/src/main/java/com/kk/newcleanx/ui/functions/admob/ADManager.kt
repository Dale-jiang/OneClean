package com.kk.newcleanx.ui.functions.admob

import android.util.Log
import com.google.gson.Gson
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.LOCAL_AD_JSON
import com.kk.newcleanx.data.local.abnormalAdConfig
import com.kk.newcleanx.data.local.adClickCount
import com.kk.newcleanx.data.local.adClickTime
import com.kk.newcleanx.data.local.adDisplayCount
import com.kk.newcleanx.data.local.adDisplayTime
import com.kk.newcleanx.data.local.appInstallTime
import com.kk.newcleanx.data.local.buyUserTags
import com.kk.newcleanx.data.local.installReferrerStr
import com.kk.newcleanx.data.local.isUnusualUser
import com.kk.newcleanx.data.local.unusualAdClickCount
import com.kk.newcleanx.data.local.unusualAdShowCount
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.tba.TbaHelper
import java.util.concurrent.TimeUnit

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
                this@ADManager.displayMax = displayMax
                this@ADManager.clickMax = clickMax
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
        if (hasReachedUnusualAdLimit()) return@let true
        if (0 == displayMax) return@let false
        val overDisplay = if (CommonUtils.isSameDay(adDisplayTime)) adDisplayCount >= displayMax else false
        val overClick = if (CommonUtils.isSameDay(adClickTime)) adClickCount >= clickMax else false
        return@let (overDisplay || overClick)
    }

    fun updateUnusualAdMetrics(isFullAd: Boolean, isClick: Boolean) = runCatching {
        if (abnormalAdConfig.switch == 0) return@runCatching

        val isTypeMatching = (abnormalAdConfig.type == 1 && isFullAd) || abnormalAdConfig.type == 0
        if (!isTypeMatching) return@runCatching // Early return if type condition doesn't match

        val currentTime = System.currentTimeMillis()
        val hoursSinceInstall = TimeUnit.MILLISECONDS.toHours(currentTime - appInstallTime)

        if (hoursSinceInstall >= abnormalAdConfig.timeInterval) {
            // Reset counters if time interval has passed
            appInstallTime = currentTime
            unusualAdShowCount = if (isClick) 0 else 1
            unusualAdClickCount = if (isClick) 1 else 0
        } else {
            // Update counters based on whether it is a click or a show
            if (isClick) {
                unusualAdClickCount++
                Log.e("updateUnusualAdMetrics", "unusualAdClickCount == $unusualAdClickCount")
            } else {
                unusualAdShowCount++
                Log.e("updateUnusualAdMetrics", "unusualAdShowCount == $unusualAdShowCount")
            }

            // Check if unusual ad limit is reached
            hasReachedUnusualAdLimit()
        }
    }


    private fun hasReachedUnusualAdLimit(): Boolean {

        if (isUnusualUser) return true // Early return if already marked as unusual user

        // Check if the user has exceeded the maximum allowed counts
        val hasReachedLimit = unusualAdClickCount >= abnormalAdConfig.maxClickCount || unusualAdShowCount >= abnormalAdConfig.maxShowCount
        if (hasReachedLimit) {
            TbaHelper.eventPost("ad_abnormal_user")
            isUnusualUser = true
            return true
        }
        return false
    }

    fun isBlocked(): Boolean {
        if (CommonUtils.isBlackUser()) return true
        return !buyUserTags.any { installReferrerStr.contains(it, true) }
    }

}