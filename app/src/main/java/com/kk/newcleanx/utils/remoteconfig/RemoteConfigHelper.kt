package com.kk.newcleanx.utils.remoteconfig

import com.google.firebase.remoteconfig.get
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.LOCAL_AD_JSON
import com.kk.newcleanx.ui.functions.admob.ADManager

object RemoteConfigHelper : RemoteConfigBase() {

    override fun init() {
        if (BuildConfig.DEBUG) {
            ADManager.initData()
            return
        }
        getAllConfigs()
        remoteConfig.fetchAndActivate().addOnSuccessListener { getAllConfigs() }
    }

    private fun getAllConfigs() {
        getAdConfig()
    }

    private fun getAdConfig() {
        runCatching {
            val json = remoteConfig["oc_ad_config"].asString()
            ADManager.initData(json.ifBlank { LOCAL_AD_JSON })
        }
    }

}