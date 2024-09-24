@file:Suppress("DEPRECATION")

package com.kk.newcleanx.gam

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VersionInfo
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.mediation.Adapter
import com.google.android.gms.ads.mediation.InitializationCompleteCallback
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationConfiguration
import com.google.android.gms.ads.mediation.MediationInterstitialAd
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration

class CustomEventInterstitialAd : Adapter(), MediationInterstitialAd {

    private var gamInterstitial: AdManagerInterstitialAd? = null
    private var mCallback: MediationInterstitialAdCallback? = null

    override fun getSDKVersionInfo(): VersionInfo {
        val sdkVersionInfo = MobileAds.getVersion()
        return com.google.android.gms.ads.mediation.VersionInfo(sdkVersionInfo.majorVersion, sdkVersionInfo.minorVersion, sdkVersionInfo.microVersion)
    }

    override fun getVersionInfo(): VersionInfo {
        return com.google.android.gms.ads.mediation.VersionInfo(1, 0, 0)
    }

    override fun initialize(context: Context, initializationCompleteCallback: InitializationCompleteCallback, list: MutableList<MediationConfiguration>) {
        runCatching {
            initializationCompleteCallback.onInitializationSucceeded()
        }
    }

    override fun loadInterstitialAd(
        adConfiguration: MediationInterstitialAdConfiguration,
        mediationAdLoadCallback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
    ) {
        runCatching {
            val adUnit = adConfiguration.serverParameters.getString("parameter")
            val adRequest = AdManagerAdRequest.Builder().build()

            AdManagerInterstitialAd.load(adConfiguration.context, adUnit!!, adRequest, object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    this@CustomEventInterstitialAd.gamInterstitial = null
                    mediationAdLoadCallback.onFailure(loadAdError)
                }

                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    this@CustomEventInterstitialAd.gamInterstitial = interstitialAd
                    mCallback = mediationAdLoadCallback.onSuccess(this@CustomEventInterstitialAd)
                }
            })
        }
    }

    override fun showAd(context: Context) {
        runCatching {
            if (this.gamInterstitial == null) {
                mCallback?.onAdFailedToShow(AdError(1004, "IllegalState", "customWithGam"))
            } else {
                this.gamInterstitial!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                    }

                    override fun onAdDismissedFullScreenContent() {
                        mCallback?.onAdClosed()
                        this@CustomEventInterstitialAd.gamInterstitial = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        mCallback?.onAdFailedToShow(adError)
                        this@CustomEventInterstitialAd.gamInterstitial = null
                    }

                    override fun onAdImpression() {
                        mCallback?.reportAdImpression()
                    }

                    override fun onAdShowedFullScreenContent() {
                        mCallback?.onAdOpened()
                    }
                }
                this.gamInterstitial?.show(context as Activity)
            }
        }
    }

}