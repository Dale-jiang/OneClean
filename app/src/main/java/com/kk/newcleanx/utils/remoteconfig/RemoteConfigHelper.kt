package com.kk.newcleanx.utils.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.get
import com.google.gson.Gson
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.AbnormalAdConfig
import com.kk.newcleanx.data.local.LOCAL_AD_JSON
import com.kk.newcleanx.data.local.LOCAL_NOTICE_CONFIG_JSON
import com.kk.newcleanx.data.local.LOCAL_NOTICE_TEXT_JSON
import com.kk.newcleanx.data.local.abnormalAdConfig
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.notice.NormalNoticeManager

object RemoteConfigHelper : RemoteConfigBase() {

    override fun init() {
        if (BuildConfig.DEBUG) {
            ADManager.initData()
            NormalNoticeManager.initNoticeConfig()
            NormalNoticeManager.initNoticeText()
            return
        }
        getAllConfigs()
        remoteConfig.fetchAndActivate().addOnSuccessListener { getAllConfigs() }
    }

    private fun getAllConfigs() {
        getAdConfig()
        getNoticeConfigs()
        getNoticeText()
        getAbnormalAdConfig()
    }

    private fun getAdConfig() {
        runCatching {
            val json = remoteConfig["oc_ad_config"].asString()
            ADManager.initData(json.ifBlank { LOCAL_AD_JSON })
        }
    }

    private fun getNoticeConfigs() {
        runCatching {
            val json = remoteConfig["ocpop"].asString()
            NormalNoticeManager.initNoticeConfig(json.ifBlank { LOCAL_NOTICE_CONFIG_JSON })
        }
    }

    private fun getNoticeText() {
        runCatching {
            val json = remoteConfig["octext"].asString()
            NormalNoticeManager.initNoticeText(json.ifBlank { LOCAL_NOTICE_TEXT_JSON })
        }
    }

    private fun getAbnormalAdConfig() {
        runCatching {
            val json = remoteConfig["ad_toomuch"].asString()
            if (json.isEmpty()) return
            runCatching {
                abnormalAdConfig = Gson().fromJson(json, AbnormalAdConfig::class.java)
            }.onFailure {
                abnormalAdConfig = AbnormalAdConfig()
            }
        }.onFailure { exception ->
            Log.e("getAbnormalAdConfig", "Error: ${exception.message}", exception)
        }
    }

}