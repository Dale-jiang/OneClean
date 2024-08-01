package com.kk.newcleanx

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.utils.AppLifecycleHelper
import com.tencent.mmkv.MMKV

class MyAPP:Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(AppLifecycleHelper())
        MobileAds.initialize(this)
        ADManager.initData()
    }

}