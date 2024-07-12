package com.kk.newcleanx.data.local

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class MainFunction(var title: Int = -1, var iconId: Int = -1, var type: String = "")

@Keep
abstract class JunkDetailsType {
    abstract val junkType: JunkType
    abstract var select: Boolean
    abstract val fileSize: Long
}

@Keep
data class JunkDetails(
    val fileName: String, val filePath: String, override val fileSize: Long, override val junkType: JunkType, override var select: Boolean
) : JunkDetailsType()

@Keep
data class JunkDetailsParent(
    val subItems: MutableList<JunkDetails>, var isOpen: Boolean, override val fileSize: Long, override val junkType: JunkType, override var select: Boolean
) : JunkDetailsType()

@Keep
data class BigFileFilter(val nameId: Int, var select: Boolean = false)

@Keep
data class BigFile(val id: Long, val name: String, val path: String, val size: Long, val date: Long, val mimeType: String, var select: Boolean = false)


@Keep
data class AdItemList(
    @SerializedName("suhbs") val displayMax: Int = 0,

    @SerializedName("uzols") val clickMax: Int = 0,

    @SerializedName("fm_launch") val fmLaunch: MutableList<AdItem>?,

    @SerializedName("fm_done_clean_int") val fmDoneCleanInt: MutableList<AdItem>?,

    @SerializedName("fm_main_nat") val fmMainNat: MutableList<AdItem>?,

    @SerializedName("fm_done_clean_nat") val fmDoneCleanNat: MutableList<AdItem>?,

    @SerializedName("fm_back_scan_int") val fmBackScanInt: MutableList<AdItem>?
) {
    @Keep
    @Parcelize
    data class AdItem(
        @SerializedName("aoos") val adId: String,
        @SerializedName("aubs") val adPlatform: String,
        @SerializedName("appij") val adType: String,
        @SerializedName("ysbbk") val adExpireTime: Int,
        @SerializedName("ijskj") val adWeight: Int
    ) : Parcelable

}




















