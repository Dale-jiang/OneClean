package com.kk.newcleanx.data.local

import androidx.annotation.Keep

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