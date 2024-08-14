package com.kk.newcleanx.ui.functions.antivirus.vm

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.data.local.VirusBean
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.data.local.virusRiskList
import com.kk.newcleanx.utils.CommonUtils
import com.trustlook.sdk.cloudscan.CloudScanClient
import com.trustlook.sdk.cloudscan.CloudScanListener
import com.trustlook.sdk.data.AppInfo
import com.trustlook.sdk.data.Region
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AntivirusScanningViewModel : ViewModel() {

    val runningTask = MutableLiveData<Int>()
    val progress = MutableLiveData<Int>()
    val complete = MutableLiveData<Int>()
    val virusNumObserver = MutableLiveData<Int>()
    val malWareNumObserver = MutableLiveData<Int>()
    val pathObserver = MutableLiveData<String>()

    private var progressAnimator: Animator? = null
    private var timeoutJob: Job? = null
    private var scanClient: CloudScanClient? = null

    private val firstStep by lazy {
        Random.nextInt(10, 16)
    }

    private var virusNum = 0
    private var malWareNum = 0

    private val scanListener = object : CloudScanListener() {
        override fun onScanStarted() {}

        override fun onScanProgress(cur: Int, total: Int, result: AppInfo?) {
            if (result == null) return
            timeoutJob?.cancel()
            if (result.score in 6..7) {
                virusNumObserver.postValue(++virusNum)
            } else if (result.score in 8..10) {
                malWareNumObserver.postValue(++malWareNum)
            }

            val progressValue = (firstStep + cur.toFloat() / total * 70).toInt()
            if (progressValue == 50) runningTask.postValue(1)
            pathObserver.postValue(result.apkPath)
            progress.postValue(progressValue)
        }

        override fun onScanError(errorCode: Int, msg: String?) {
            complete.postValue(errorCode)
        }

        override fun onScanCanceled() = Unit
        override fun onScanInterrupt() {
            complete.postValue(1001)
        }

        override fun onScanFinished(results: MutableList<AppInfo>?) {
            runCatching {
                doResult(results)
            }
        }
    }


    private fun doResult(results: MutableList<AppInfo>?) {
        results?.forEach { result ->

            if (result.score > 5) {

                val levelName = if (result.score in 6..7) "(PUA)" else "(malware)"
                val securityEntity: VirusBean

                if (result.packageName.isNullOrBlank().not() && CommonUtils.isPackageInstalled(result.packageName)) {
                    if (result.isSystemApp.not()) {
                        securityEntity = VirusBean(
                            path = result.apkPath ?: "",
                            packageName = result.packageName ?: "",
                            appName = result.appName ?: "",
                            icon = app.packageManager.getApplicationIcon(result.packageName),
                            levelName = levelName,
                            category = if (result.category != null && result.category.size > 1) result.category[1] else "",
                            virusName = result.virusName,
                            score = result.score
                        )
                        virusRiskList.add(securityEntity)
                    }
                } else {
                    securityEntity = VirusBean(
                        path = result.apkPath ?: "",
                        packageName = result.packageName ?: "",
                        appName = result.appName ?: "",
                        icon = null,
                        levelName = levelName,
                        category = if (result.category != null && result.category.size > 1) result.category[1] else "",
                        virusName = result.virusName,
                        score = result.score
                    )
                    virusRiskList.add(securityEntity)
                }
            }
        }

        startProgressAnim(start = 80, end = 100, time = 2000L, onValueUpdate = {
            progress.postValue(it)
        }, onCompleted = {
            complete.postValue(888)
        })
    }


    private fun startProgressAnim(start: Int = 0, end: Int = 100, time: Long = 10000L, onValueUpdate: (value: Int) -> Unit = {}, onCompleted: () -> Unit = {}) {
        cancelAnim()
        progressAnimator = ValueAnimator.ofInt(start, end).apply {
            duration = time
            addUpdateListener {
                (it.animatedValue as? Int)?.let { value ->
                    onValueUpdate.invoke(value)
                    if (end == value) onCompleted.invoke()
                }
            }
        }
        progressAnimator?.start()
    }

    fun cancel() {
        runCatching {
            scanClient?.cancelScan()
            cancelAnim()
        }
    }

    private fun cancelAnim() {
        runCatching {
            progressAnimator?.cancel()
            progressAnimator = null
        }
    }

    fun startScan() {
        runCatching {

            virusRiskList.clear()
            runningTask.postValue(0)

            startTimeOutCount()

            scanClient = CloudScanClient.Builder(app).setRegion(Region.INTL).setConnectionTimeout(30000).setSocketTimeout(30000).build()

            startProgressAnim(0, firstStep, 2500L, onValueUpdate = {
                progress.postValue(it)
            }, onCompleted = {
                scanClient?.startComprehensiveScan(scanListener)
            })
        }
    }


    private fun startTimeOutCount() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch(Dispatchers.IO) {
            delay(35 * 1000L)
            withContext(Dispatchers.Main) {
                if (progress.value == firstStep) {
                    complete.postValue(9)
                }
            }
        }
    }

}