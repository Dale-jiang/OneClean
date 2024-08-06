package com.kk.newcleanx

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.AppLifecycleHelper
import com.kk.newcleanx.utils.tba.TbaHelper
import com.tencent.mmkv.MMKV

class MyAPP : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(AppLifecycleHelper())
        Firebase.initialize(this)
        TbaHelper.getAllUserInfo()
    }

}