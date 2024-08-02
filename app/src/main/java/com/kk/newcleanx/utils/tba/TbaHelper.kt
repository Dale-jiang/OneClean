package com.kk.newcleanx.utils.tba

import android.util.Log
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.cloakResult
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


    fun getAllUserInfo() {
        getCloakInfo()
    }

    override fun getCloakInfo() {
        if (cloakResult.isNotBlank()) return
        getCloakInfoJob?.cancel()
        getCloakInfoJob = CoroutineHelper.launchIO {
            while (cloakResult.isBlank()) {
                delay(1000L)
                requestInfo()
                delay(10000L)
            }
        }
    }


    private fun requestInfo() {
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
            Log.e("request==> ", "${e.message}==Exception")
        }
    }


    override fun getReferrerInfo() {

    }


}