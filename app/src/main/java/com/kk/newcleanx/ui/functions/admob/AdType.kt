package com.kk.newcleanx.ui.functions.admob

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.ui.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class AdType {

    var where: String? = null
    var adItem: AdItemList.AdItem? = null
    abstract fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit)
    abstract fun showAd(activity: BaseActivity<*>, onClose: () -> Unit = {})
    abstract fun destroy()
    abstract fun isAdExpire(): Boolean
    fun onPaidEventListener(adValue: AdValue, responseInfo: ResponseInfo?) {
        Log.e("onPaidEventListener", "------>>>>onPaidEventListener")
    }

    data class FullScreenAd(val adLoadTime: Long = System.currentTimeMillis()) : AdType() {

        private var mAd: Any? = null
        private val mAdRequest = AdRequest.Builder().build()

        @Suppress("DEPRECATION")
        private fun adOpen(onLoaded: (success: Boolean, msg: String?) -> Unit) {
            adItem?.apply {
                Log.e("FullScreenAd==>", "$where $adType - $adId starting load ad")
                AppOpenAd.load(app, adId, mAdRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdFailedToLoad(e: LoadAdError) = onLoaded.invoke(false, e.message)
                    override fun onAdLoaded(openAd: AppOpenAd) = kotlin.run {
                        mAd = openAd
                        openAd.setOnPaidEventListener { onPaidEventListener(it, openAd.responseInfo) }
                        onLoaded.invoke(true, "")
                    }
                })
            }
        }

        private fun adInterstitial(onLoaded: (success: Boolean, msg: String?) -> Unit) {

            adItem?.apply {
                Log.e("FullScreenAd==>", "$where $adType - $adId starting load ad")
                InterstitialAd.load(app, adId, mAdRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(e: LoadAdError) = onLoaded.invoke(false, e.message)
                    override fun onAdLoaded(interstitialAd: InterstitialAd) = kotlin.run {
                        mAd = interstitialAd
                        interstitialAd.setOnPaidEventListener {
                            onPaidEventListener(it, interstitialAd.responseInfo)
                        }
                        onLoaded.invoke(true, "")
                    }
                })
            }

        }

        override fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit) {
            when (adItem?.adType) {
                "int" -> adInterstitial(onLoaded)
                "op" -> adOpen(onLoaded)
                else -> onLoaded.invoke(false, "something is wrong with adType ")
            }
        }

        private fun onAdClose(activity: BaseActivity<*>?, close: () -> Unit = {}) {
            activity?.apply {
                activity.lifecycleScope.launch {
                    while (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(100L)
                    close.invoke()
                }
            } ?: close.invoke()
        }


        override fun showAd(activity: BaseActivity<*>, onClose: () -> Unit) {

            val admobCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClose(activity, onClose)
                }

                override fun onAdFailedToShowFullScreenContent(e: AdError) {
                    Log.e("FullScreenAd==>", "$where ${adItem?.adType} - ${adItem?.adId} show ad failed:${e.message}")
                    onAdClose(activity, onClose)
                }

                override fun onAdClicked() {
                    ADManager.addClick()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.e("FullScreenAd==>", "$where ${adItem?.adType} - ${adItem?.adId} show ad success")
                    ADManager.addDisplay()
                }
            }

            when (val admobAd = mAd) {
                is InterstitialAd -> {
                    admobAd.run {
                        fullScreenContentCallback = admobCallback
                        show(activity)
                    }
                }

                is AppOpenAd -> {
                    admobAd.run {
                        fullScreenContentCallback = admobCallback
                        show(activity)
                    }
                }

                else -> onAdClose(activity, onClose)
            }
        }

        override fun destroy() {
        }

        override fun isAdExpire(): Boolean {
            if (adItem == null) return false
            return System.currentTimeMillis() - adLoadTime >= adItem!!.adExpireTime * 1000L
        }

    }

}