package com.kk.newcleanx.ui.functions.admob

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.data.local.AdItemList
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.ui.base.BaseActivity
import kotlinx.coroutines.launch

class FullScreenAdLoader(iWhere: String) : BaseLoader(iWhere) {

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

    fun showFullScreenAd(
        activity: BaseActivity<*>, posId: String = where, onClose: () -> Unit = {}
    ) {
        if (adLoadList.isEmpty()) {
            onClose.invoke()
            return
        }
        runCatching {
            activity.lifecycleScope.launch {
                val ad = adLoadList.removeFirstOrNull()
                if (ad is AdType.FullScreenAd) {
                    ad.showAd(activity, onClose)
                } else {
                    onClose.invoke()
                    return@launch
                }
                onLoaded = {}
                loadAd(activity)
            }
        }
    }


}