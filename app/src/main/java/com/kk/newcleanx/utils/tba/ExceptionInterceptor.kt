package com.kk.newcleanx.utils.tba

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ExceptionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (t: Throwable) {
            throw IOException("ExceptionInterceptor when requesting ${chain.request().url}", t)
        }
    }
}