package com.kk.newcleanx.utils.tba

import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.cloakResult
import com.kk.newcleanx.data.local.installReferrerStr
import com.kk.newcleanx.utils.CoroutineHelper
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

object TbaHelper : TbaBase() {

    private var getCloakInfoJob: Job? = null
    private var getReferrerJob: Job? = null


    fun getAllUserInfo() {
        getCloakInfo()
        getReferrerInfo()
    }

    override fun getCloakInfo() {
        if (cloakResult.isNotBlank()) return
        getCloakInfoJob?.cancel()
        getCloakInfoJob = CoroutineHelper.launchIO {
            while (cloakResult.isBlank()) {
                delay(1000L)
                requestCloakInfo()
                delay(10000L)
            }
        }
    }


    private fun requestCloakInfo() {
        runCatching {

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

        }
    }


    override fun getReferrerInfo() {
        if (installReferrerStr.isNotBlank()) return
        getCloakInfoJob?.cancel()
        getCloakInfoJob = CoroutineHelper.launchIO {
            while (cloakResult.isBlank()) {
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