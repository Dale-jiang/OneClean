package com.kk.newcleanx.utils.tba

import android.content.Context
import android.telephony.TelephonyManager
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.utils.CommonUtils
import org.json.JSONObject

abstract class TbaBase {
    protected val cloakUrl = "https://atoll.optimiclean.com/melinda/dance/pantheon"
    protected val tbaUrl =
        if (BuildConfig.DEBUG) "https://test-anheuser.optimiclean.com/goddess/force/coterie" else "https://anheuser.optimiclean.com/cache/screw"

    protected val netOperator by lazy { (app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.networkOperator ?: "" }
    protected val distinctId by lazy { CommonUtils.createDistinctId() }
    protected val androidId by lazy { CommonUtils.createAndroidId() }
    protected val httpClient by lazy { CommonUtils.createHttpClient() }
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(app) }
    val facebookLogger by lazy { AppEventsLogger.newLogger(app) }
    protected abstract fun getGoogleInfo()
    protected abstract fun getCloakInfo()
    protected abstract fun getReferrerInfo()

    protected abstract fun buildCommonParams(): JSONObject
    protected abstract fun postEvent(event: String, params: HashMap<String, Any?> = hashMapOf())
    protected abstract fun postInstall(referrerInfo: ReferrerDetails?)
    protected abstract fun postSession()
    protected abstract fun postAdImpression(obj: JSONObject)
    protected abstract suspend fun runRequest(bodyString: String, requestTag: String)
}