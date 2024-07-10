package com.kk.newcleanx.data.local

import androidx.annotation.Keep

@Keep
data class MainFunction(var title: Int = -1, var iconId: Int = -1, var type: String = "")

abstract class JunkDetailsType {
    abstract val junkType: JunkType
    abstract val select: Boolean
    abstract val fileSize: Long
}

data class JunkDetails(
    val fileName: String, val filePath: String, override val fileSize: Long, override val junkType: JunkType, override val select: Boolean
) : JunkDetailsType()

data class JunkDetailsParent(
    val subItems: MutableList<JunkDetails>, var isOpen: Boolean, override val fileSize: Long, override val junkType: JunkType, override val select: Boolean
) : JunkDetailsType()