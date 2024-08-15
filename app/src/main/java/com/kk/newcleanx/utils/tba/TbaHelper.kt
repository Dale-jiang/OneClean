package com.kk.newcleanx.utils.tba

import android.os.Build
import android.util.Log
import android.webkit.WebSettings
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.cloakResult
import com.kk.newcleanx.data.local.enableLimitAdTracker
import com.kk.newcleanx.data.local.gaidString
import com.kk.newcleanx.data.local.installReferrerStr
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.getAdTypeName
import com.kk.newcleanx.utils.CommonUtils.getAdapterClassName
import com.kk.newcleanx.utils.CommonUtils.getFirInstallTime
import com.kk.newcleanx.utils.CommonUtils.getLastUpdateTime
import com.kk.newcleanx.utils.CoroutineHelper
import com.kk.newcleanx.utils.toBundle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object TbaHelper : TbaBase() {

    private var getCloakInfoJob: Job? = null
    private var getReferrerJob: Job? = null
    private var googleInfoJob: Job? = null

    fun getAllUserInfo() {
        getGoogleInfo()
        getCloakInfo()
        getReferrerInfo()
    }

    fun postSessionEvent() {
        postSession()
    }

    fun postAdImpressionEvent(adValue: AdValue, responseInfo: ResponseInfo?, ad: AdType) {
        runCatching {
            val jsonObj = buildCommonParams()
            val obj = JSONObject().apply {
                put("polite", adValue.valueMicros)
                put("odysseus", adValue.currencyCode)
                put("laterite", getAdapterClassName(responseInfo))
                put("michele", ad.adItem?.adPlatform ?: "admob")
                put("diane", ad.adItem?.adId ?: "")
                put("ginn", ad.where)
                put("sheathe", getAdTypeName(ad.adItem?.adType ?: ""))
            }
            jsonObj.put("cacao", obj)
            postAdImpression(jsonObj)
        }
    }

    fun eventPost(event: String, params: HashMap<String, Any?>) {
        runCatching {
            Log.e("eventPost==>", if (params.isNotEmpty()) "EventName: $event, Params: $params" else "EventName: $event")
            if (!CommonUtils.isTestMode()) firebaseAnalytics.logEvent(event, params.toBundle())
            postEvent(event, params)
        }

    }

    override fun postEvent(event: String, params: HashMap<String, Any?>) {

        CoroutineHelper.launchIO {
            val jsonObj = buildCommonParams()
            jsonObj.put("upon", event)
            params.forEach { (t, u) -> jsonObj.put("${t}^zeiss", u) }
            runRequest(jsonObj.toString(), "postEvent")
        }

    }

    override fun buildCommonParams(): JSONObject {

        return JSONObject().apply {

            put("shinbone", JSONObject().apply {
                put("autopsy", Locale.getDefault().toString())
                put("put", app.packageName)
                put("naacp", UUID.randomUUID().toString())
            })

            put("returnee", JSONObject().apply {
                put("jaime", androidId)
                put("white", netOperator)
                put("acapulco", System.currentTimeMillis())
                put("succumb", Build.MANUFACTURER ?: "")
            })

            put("nourish", JSONObject().apply {
                put("past", TimeZone.getDefault().rawOffset / 3600000)
                put("galactic", BuildConfig.VERSION_NAME)
                put("bronze", distinctId)
                put("rod", "backstop")
                put("physic", Build.VERSION.RELEASE ?: "")
                put("bandy", Build.MODEL ?: "")
            })

            put("jacket", JSONObject().apply {
                put("moore", Locale.getDefault().country.toString())
            })

        }


    }

    override fun postInstall(referrerInfo: ReferrerDetails?) {
        CoroutineHelper.launchIO {
            val parameters = buildCommonParams()
            parameters.apply {
                put("prague", "build/${Build.ID}")
                put("chow", referrerInfo?.installReferrer ?: "")
                put("snatch", referrerInfo?.installVersion ?: "")
                put("blocky", WebSettings.getDefaultUserAgent(app) ?: "")
                put("suzuki", if (enableLimitAdTracker) "toodle" else "justice")
                put("palpate", referrerInfo?.referrerClickTimestampSeconds ?: 0L)
                put("formal", referrerInfo?.installBeginTimestampSeconds ?: 0L)
                put("mabel", referrerInfo?.referrerClickTimestampServerSeconds ?: 0L)
                put("glut", referrerInfo?.installBeginTimestampServerSeconds ?: 0L)
                put("pancreas", getFirInstallTime())
                put("toshiba", getLastUpdateTime())
                put("upon", "moghul")

            }
            runRequest(parameters.toString(), "postInstall")
        }
    }

    override fun postSession() {
        CoroutineHelper.launchIO {
            val jsonObj = buildCommonParams()
            jsonObj.put("baggage", JSONObject())
            runRequest(jsonObj.toString(), "postSession")
        }
    }

    override fun postAdImpression(obj: JSONObject) {
        CoroutineHelper.launchIO {
            runRequest(obj.toString(), "postAdImpression")
        }
    }

    override fun runRequest(bodyString: String, requestTag: String) {

        Log.e("runRequest==>", bodyString)
        val body = bodyString.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().post(body).addHeader("physic", Build.VERSION.RELEASE ?: "").url(tbaUrl).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("runRequest==>", "$requestTag: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("runRequest==>", "$requestTag: ${response.code}, ${response.body?.string() ?: ""}")
            }
        })

    }

    override fun getGoogleInfo() {
        if (gaidString.isNotBlank()) return
        googleInfoJob = CoroutineHelper.launchIO {
            while (gaidString.isBlank()) {
                delay(500L)
                getGoogleInfos()
                delay(30000L)
            }
        }
    }

    private fun getGoogleInfos() {
        runCatching {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(app)
            gaidString = adInfo.id ?: ""
            Log.e("request==>", gaidString)
            enableLimitAdTracker = adInfo.isLimitAdTrackingEnabled
            if (gaidString.isNotBlank()) {
                Log.e("request==>", "getGoogleInfos start get cancel")
                googleInfoJob?.cancel()
            }
        }
    }

    override fun getCloakInfo() {
        if (cloakResult.isNotBlank()) return
        getCloakInfoJob = CoroutineHelper.launchIO {
            Log.e("request==> ", "requestCloakInfo000")
            while (cloakResult.isBlank()) {
                delay(1000L)
                Log.e("request==> ", "requestCloakInfo777")
                requestCloakInfo()
                delay(10000L)
            }
        }
    }


    private fun requestCloakInfo() {
        try {

            val obj = JSONObject().apply { // put("put", app.packageName)
                put("put", "com.optimi.clean.up.oneclean")
                put("rod", "backstop")
                put("galactic", BuildConfig.VERSION_NAME)
            }

            val body = obj.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder().post(body).url(cloakUrl).build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("request==> ", "${e.message}==$obj")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("request==> ", "success==$obj")

                    if (response.code == 200) {
                        val bodyStr = response.body?.string()
                        Log.e("getCloakInfo==>", "Cloak Request Result ==${bodyStr}")
                        if (!bodyStr.isNullOrBlank()) cloakResult = bodyStr
                        getCloakInfoJob?.cancel()
                    }
                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun getReferrerInfo() {
        if (installReferrerStr.isNotBlank()) return
        getCloakInfoJob = CoroutineHelper.launchIO {
            while (installReferrerStr.isBlank()) {
                delay(1000L)
                requestReferrer()
                delay(20000L)
            }
        }
    }

    private fun requestReferrer() {
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(app).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    runCatching {
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                val referrerDetails = referrerClient.installReferrer
                                val referrer = referrerDetails?.installReferrer ?: ""
                                if (referrer.isNotBlank()) {
                                    installReferrerStr = referrer
                                    getReferrerJob?.cancel()
                                }
                                postInstall(referrerDetails)
                            }

                            else -> Unit
                        }
                        referrerClient.endConnection()
                    }
                }

                override fun onInstallReferrerServiceDisconnected() = Unit
            })
        }
    }


}