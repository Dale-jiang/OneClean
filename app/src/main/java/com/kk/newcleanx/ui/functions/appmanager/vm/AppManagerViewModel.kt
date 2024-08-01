package com.kk.newcleanx.ui.functions.appmanager.vm

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.BuildConfig
import com.kk.newcleanx.data.local.AppInfo
import com.kk.newcleanx.data.local.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppManagerViewModel : ViewModel() {


    private val mPackageManager: PackageManager by lazy { app.packageManager }
    val appInfoListObserver = MutableLiveData<MutableList<AppInfo>>()

    @SuppressLint("QueryPermissionsNeeded")
    fun getAppInfoList() {

        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            runCatching {

                val allAppInfoList = mutableListOf<AppInfo>()

                val installedApps = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA).filter { it.packageName != BuildConfig.APPLICATION_ID }
                    .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

                installedApps.forEach { packageInfo ->

                    if (packageInfo.applicationInfo.flags and PackageManager.GET_META_DATA == 0) {

                        val appName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString()
                        val packageName = packageInfo.packageName
                        val appIcon = packageInfo.applicationInfo.loadIcon(mPackageManager)

                        val data = AppInfo()
                        data.appIcon = appIcon
                        data.appPackageName = packageName
                        data.appName = appName

                        allAppInfoList.add(data)
                    }
                }

                withContext(Dispatchers.Main) {
                    appInfoListObserver.postValue(allAppInfoList)
                }

            }.onFailure {
                appInfoListObserver.postValue(mutableListOf())
            }
        }

    }


}