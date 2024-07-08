package com.kk.newcleanx

import android.app.Application
import com.kk.newcleanx.data.local.app
import com.tencent.mmkv.MMKV

class MyAPP:Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        MMKV.initialize(this)
    }

}