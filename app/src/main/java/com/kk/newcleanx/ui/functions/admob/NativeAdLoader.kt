package com.kk.newcleanx.ui.functions.admob

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.ui.base.BaseActivity

class NativeAdLoader(iWhere: String) : BaseLoader(iWhere) {

    fun initData(list: MutableList<AdItemList.AdItem>?) {
        mAdItems.apply {
            clear()
            addAll(list ?: emptyList())
            sortByDescending { it.adWeight }
        }
    }

    fun canShow(activity: BaseActivity<*>): Boolean {
        return adLoadList.isNotEmpty() && activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    private fun removeExpireAd() {
        adLoadList.firstOrNull()?.apply {
            if (this.isAdExpire()) adLoadList.remove(this)
        }
    }

    fun loadAd(context: Context = app) {
        if (mAdItems.isEmpty()) return
        if (ADManager.isOverAdMax()) return
        removeExpireAd()
        if (adLoadList.isNotEmpty()) return
        if (isLoading) return
        isLoading = true
        context.loaAd(0, this)
    }


    fun showNativeAd(
        activity: BaseActivity<*>, parent: ViewGroup, posId: String = where, callback: (AdType) -> Unit
    ) {
        if (adLoadList.isEmpty()) return
        val ad = adLoadList.removeFirstOrNull()

        if (ad is AdType.MyNativeAd) {
            ad.showAd(activity, parent)
            callback.invoke(ad)
        }

        onLoaded = {}
        loadAd(activity)
    }

    fun waitAdLoading(context: Context, onLoad: (Boolean) -> Unit = {}) {
        if (adLoadList.isNotEmpty()) onLoad.invoke(true)
        else {
            onLoaded = onLoad
            loadAd(context)
        }
    }

}