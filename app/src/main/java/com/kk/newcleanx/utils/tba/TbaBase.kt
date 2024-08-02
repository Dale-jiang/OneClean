package com.kk.newcleanx.utils.tba

import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.utils.CommonUtils

abstract class TbaBase {
    val cloakUrl = "https://atoll.optimiclean.com/melinda/dance/pantheon"
    val tbaUrl = if (BuildConfig.DEBUG) "https://test-anheuser.optimiclean.com/goddess/force/coterie" else "https://anheuser.optimiclean.com/cache/screw"

    val distinctId by lazy { CommonUtils.createDistinctId() }
    val androidId by lazy { CommonUtils.createAndroidId() }
    val httpClient by lazy { CommonUtils.createHttpClient() }

    //    val mFirebase by lazy { FirebaseAnalytics.getInstance(app) }
    //    val mFacebookLog by lazy { AppEventsLogger.newLogger(app) }

    abstract fun getCloakInfo()
    abstract fun getReferrerInfo()

}