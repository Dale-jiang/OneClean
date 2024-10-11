package com.kk.newcleanx.ui.functions.admob

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.databinding.LayoutNativeAdBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Currency

sealed class AdType {

    var where: String? = null
    var adItem: AdItemList.AdItem? = null
    abstract fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit)
    abstract fun showAd(activity: BaseActivity<*>, parent: ViewGroup?, onClose: () -> Unit = {})
    abstract fun destroy()
    abstract fun isAdExpire(): Boolean
    fun onPaidEventListener(adValue: AdValue, responseInfo: ResponseInfo?) {

        Log.e("onPaidEventListener", "------>>>>onPaidEventListener")

        val revenue: Double = adValue.valueMicros / 1000000.toDouble()
        runCatching {
            if (!CommonUtils.isTestMode()) {
                TbaHelper.firebaseAnalytics.logEvent("ad_impression_revenue", Bundle().apply {
                    putDouble(FirebaseAnalytics.Param.VALUE, revenue)
                    putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                })
                TbaHelper.facebookLogger.logPurchase(revenue.toBigDecimal(), Currency.getInstance("USD"))
            }
        }

        TbaHelper.postAdImpressionEvent(adValue, responseInfo, this)
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


        override fun showAd(activity: BaseActivity<*>, parent: ViewGroup?, onClose: () -> Unit) {

            val admobCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClose(activity, onClose)
                }

                override fun onAdFailedToShowFullScreenContent(e: AdError) {
                    Log.e("FullScreenAd==>", "$where ${adItem?.adType} - ${adItem?.adId} show ad failed:${e.message}")
                    onAdClose(activity, onClose)
                }

                override fun onAdClicked() {
                    ADManager.updateUnusualAdMetrics(isFullAd = true, isClick = true)
                    ADManager.addClick()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.e("FullScreenAd==>", "$where ${adItem?.adType} - ${adItem?.adId} show ad success")
                    ADManager.updateUnusualAdMetrics(isFullAd = true, isClick = false)
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

        override fun destroy() = Unit

        override fun isAdExpire(): Boolean {
            if (adItem == null) return false
            return System.currentTimeMillis() - adLoadTime >= adItem!!.adExpireTime * 1000L
        }

    }


    data class MyNativeAd(val adLoadTime: Long = System.currentTimeMillis()) : AdType() {

        private var mAdNative: NativeAd? = null

        private val mAdRequest = AdRequest.Builder().build()


        override fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit) {

            Log.e("MyNativeAd==>", "${where} ${adItem?.adType} - ${adItem?.adId} start load ad")
            adItem?.apply {
                AdLoader.Builder(app, adId).apply {
                    forNativeAd { ad ->
                        mAdNative = ad
                        ad.setOnPaidEventListener { onPaidEventListener(it, ad.responseInfo) }
                        onLoaded.invoke(true, "")
                    }
                    withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(e: LoadAdError) = onLoaded.invoke(false, e.message)
                        override fun onAdClicked() {
                            ADManager.addClick()
                            ADManager.updateUnusualAdMetrics(isFullAd = false, isClick = true)
                        }

                    })
                    withNativeAdOptions(NativeAdOptions.Builder().apply {
                        setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                    }.build())

                }.build().loadAd(mAdRequest)
            }
        }

        override fun showAd(activity: BaseActivity<*>, parent: ViewGroup?, onClose: () -> Unit) {
            runCatching {
                mAdNative?.run {
                    val binding = LayoutNativeAdBinding.inflate(LayoutInflater.from(activity), parent, false)
                    binding.nativeAdView.let { adView ->
                        adView.iconView = binding.icon.apply { setImageDrawable(this@run.icon?.drawable) }
                        adView.mediaView = binding.media.apply { mediaContent = this@run.mediaContent }
                        adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        adView.headlineView = binding.title.apply { text = this@run.headline ?: "" }
                        adView.bodyView = binding.des.apply { text = this@run.body ?: "" }
                        adView.callToActionView = binding.actionBtn.apply { text = this@run.callToAction ?: "" }
                        adView.setNativeAd(this)
                    }
                    parent?.removeAllViews()
                    parent?.addView(binding.root)
                    ADManager.addDisplay()
                    ADManager.updateUnusualAdMetrics(isFullAd = false, isClick = false)
                    Log.e("MyNativeAd==>", "${where} ${adItem?.adType} - ${adItem?.adId} show success")
                }
            }
        }

        override fun destroy() {
            mAdNative?.destroy()
        }

        override fun isAdExpire(): Boolean {
            if (adItem == null) return false
            return System.currentTimeMillis() - adLoadTime >= adItem!!.adExpireTime * 1000L
        }
    }

}