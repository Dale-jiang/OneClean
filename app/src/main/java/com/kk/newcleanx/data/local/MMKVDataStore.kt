package com.kk.newcleanx.data.local

import com.google.gson.Gson
import com.kk.newcleanx.utils.getFirInstallTime
import com.tencent.mmkv.MMKV
import kotlin.reflect.KProperty

class MMKVStorageDelegate<T>(private val defaultValue: T) {

    private val mmkv: MMKV by lazy { MMKV.defaultMMKV() }
    private val gson: Gson by lazy { Gson() }

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is String -> mmkv.decodeString(property.name, defaultValue) as T
            is Int -> mmkv.decodeInt(property.name, defaultValue) as T
            is Boolean -> mmkv.decodeBool(property.name, defaultValue) as T
            is Float -> mmkv.decodeFloat(property.name, defaultValue) as T
            is Long -> mmkv.decodeLong(property.name, defaultValue) as T
            is Double -> mmkv.decodeDouble(property.name, defaultValue) as T
            else -> {
                val json = mmkv.decodeString(property.name, null)
                if (json != null) gson.fromJson(json, defaultValue!!::class.java) else defaultValue
            }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        when (value) {
            is String -> mmkv.encode(property.name, value)
            is Int -> mmkv.encode(property.name, value)
            is Boolean -> mmkv.encode(property.name, value)
            is Float -> mmkv.encode(property.name, value)
            is Long -> mmkv.encode(property.name, value)
            is Double -> mmkv.encode(property.name, value)
            else -> mmkv.encode(property.name, gson.toJson(value))
        }
    }
}

var isFirstStartup by MMKVStorageDelegate(true)
var alreadyRequestStoragePermissions by MMKVStorageDelegate(false)
var junkCleanTimeTag by MMKVStorageDelegate(0L)
var adDisplayTime by MMKVStorageDelegate(0L)
var adDisplayCount by MMKVStorageDelegate(0)
var adClickTime by MMKVStorageDelegate(0L)
var adClickCount by MMKVStorageDelegate(0)
var distinctId by MMKVStorageDelegate("")
var cloakResult by MMKVStorageDelegate("")
var installReferrerStr by MMKVStorageDelegate("")
var hasShowAntivirusTips by MMKVStorageDelegate(false)
var gaidString by MMKVStorageDelegate("")
var enableLimitAdTracker by MMKVStorageDelegate(true)
var showNotificationPerDialogTime by MMKVStorageDelegate(0L)

var timerNoticeLastShowTime by MMKVStorageDelegate(0L)
var unlockNoticeLastShowTime by MMKVStorageDelegate(0L)

var unusualAdClickCount by MMKVStorageDelegate(0)
var unusualAdShowCount by MMKVStorageDelegate(0)
var appInstallTime by MMKVStorageDelegate(app.getFirInstallTime())
var isUnusualUser by MMKVStorageDelegate(false)

var antivirusRedPointLastShowTime by MMKVStorageDelegate(0L)
var duplicateFilesRedPointLastShowTime by MMKVStorageDelegate(0L)
