package com.kk.newcleanx.ui.functions.admob

import android.content.Context
import android.util.Log


fun Context.loaAd(i: Int, loader: BaseLoader) {

    fun realLoad(index: Int) {
        val item = loader.mAdItems.getOrNull(index)
        if (null == item) {
            loader.onLoaded?.invoke(false)
            loader.isLoading = false
            return
        }
        val baseAd = when (item.adPlatform) {
            "admob" -> {
                when (item.adType) {
                    "op", "int" -> {
                        AdType.FullScreenAd(System.currentTimeMillis()).also {
                            it.adItem = item
                            it.where = loader.where
                        }
                    }
                    // "nat" -> NativeAd(adImpl.adPosition, item)
                    else -> null
                }
            }

            else -> null
        }

        if (null == baseAd) {
            realLoad(index + 1)
            return
        }

        baseAd.loadAd(this) { success, msg ->
            if (success) {
                loader.run {
                    adLoadList.add(baseAd)
                    adLoadList.sortByDescending { it.adItem!!.adWeight }
                    onLoaded?.invoke(true)
                    isLoading = false
                }
                Log.e("AdLoadUtils==>", "${loader.where} ${item.adType} - ${item.adId} load success")
            } else {
                Log.e("AdLoadUtils==>", "${loader.where} ${item.adType} - ${item.adId} load failed:$msg")
                realLoad(index + 1)
            }
        }
    }

    realLoad(i)

}