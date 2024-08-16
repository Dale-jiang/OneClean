package com.kk.newcleanx.data.local

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

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
data class AppInfo(var appIcon: Drawable? = null, var appName: String = "", var appPackageName: String = "") : Serializable

@Keep
data class AdItemList(
        @SerializedName("uzek") val displayMax: Int = 0,

        @SerializedName("xihe") val clickMax: Int = 0,

        @SerializedName("oc_launch") val ocLaunch: MutableList<AdItem>?,

        @SerializedName("oc_scan_int") val ocScanInt: MutableList<AdItem>?,

        @SerializedName("oc_clean_int") val ocCleanInt: MutableList<AdItem>?,

        @SerializedName("oc_scan_nat") val ocScanNat: MutableList<AdItem>?,

        @SerializedName("oc_clean_nat") val ocCleanNat: MutableList<AdItem>?,

        @SerializedName("oc_main_nat") val ocMainNat: MutableList<AdItem>?
) {
    @Keep
    @Parcelize
    data class AdItem(
            @SerializedName("kdhi") val adId: String,
            @SerializedName("iegw") val adPlatform: String,
            @SerializedName("zirhh") val adType: String,
            @SerializedName("zpejh") val adExpireTime: Int,
            @SerializedName("lasha") val adWeight: Int
    ) : Parcelable

}


@Keep
data class VirusBean(
        val path: String,
        val packageName: String,
        val appName: String,
        val icon: Drawable?,
        val levelName: String,
        val virusName: String,
        val category: String,
        val score: Int
)

@Keep
@Parcelize
data class NoticeType(val toPage: String, val scene: String, val sceneSecond: String = "", val des: String="") : Parcelable

@Keep
data class NormalNoticeConfig(
        @SerializedName("ocon") val open: Int,
        @SerializedName("oct") val timer: NormalNoticeConfigItem?,
        @SerializedName("ocu") val unlock: NormalNoticeConfigItem?
)

@Keep
data class NormalNoticeConfigItem(
        @SerializedName("ocrepeat") val repeat: Int,
        @SerializedName("ocfirst") val first: Int
)

@Keep
data class NormalNoticeTextItem(
        @SerializedName("octtt") val functions: String = "",
        @SerializedName("occcc") val noticeDes: MutableList<String> = mutableListOf(),
        @SerializedName("ocbbb") val btnStr: String = "",
        @Transient var largeIconId: Int = 0,
        @Transient var smallIconId: Int = 0,
)


















