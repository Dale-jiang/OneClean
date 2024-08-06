package com.kk.newcleanx.utils.tba

import com.kk.newcleanx.utils.CommonUtils

abstract class TbaBase {
    val cloakUrl = "https://atoll.optimiclean.com/melinda/dance/pantheon"
    val httpClient by lazy { CommonUtils.createHttpClient() }
    abstract fun getCloakInfo()

}