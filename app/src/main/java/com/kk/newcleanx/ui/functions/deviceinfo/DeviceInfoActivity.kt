package com.kk.newcleanx.ui.functions.deviceinfo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.databinding.AcDeviceInfoBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.admob.ADManager
import com.kk.newcleanx.ui.functions.admob.AdType
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.tba.TbaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class DeviceInfoActivity : AllFilePermissionActivity<AcDeviceInfoBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DeviceInfoActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val mSensorManager by lazy { app.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val mWindowManager by lazy { app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            toolbar.tvTitle.text = getString(R.string.device_status)

            showNatAd()

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    showFullAd {
                        clLoading.isVisible = false
                        viewLottie.cancelAnimation()
                    }
                }
            }

            btnClean.setOnClickListener {
                requestAllFilePermission {
                    if (it) JunkScanningActivity.start(this@DeviceInfoActivity)
                }
            }

            ivScanBack.setOnClickListener {
                finish()
            }

            toolbar.ivBack.setOnClickListener {
                if (clLoading.isVisible) return@setOnClickListener
                finish()
            }
        }

        setViews()
    }

    private fun setViews() {
        setPhoneInfo()
        setMemoryInfo()
        setScreenInfo()
        setBatteryInfo()
        setSensorInf()
        isCompleted = true
    }


    @SuppressLint("SetTextI18n")
    private fun setPhoneInfo() {
        binding.tvPhoneModel.text = Build.BRAND + " " + Build.MODEL
        binding.tvSystemVersion.text = "Android " + Build.VERSION.RELEASE
    }

    private fun setMemoryInfo() {
        val total = CommonUtils.getTotalStorageByManager()
        val use = CommonUtils.getUsedStorageByManager()
        binding.tvMemory.text = getString(R.string.string_storage_size, use.formatStorageSize(), total.formatStorageSize())
        binding.memoryProgress.progress = ((use / total.toFloat()) * 100).toInt()

        val ramInfoPair = CommonUtils.getTotalRamMemory()
        binding.tvRam.text = getString(R.string.string_memory_size, ramInfoPair.second.formatStorageSize(), ramInfoPair.first.formatStorageSize())
        binding.ramProgress.progress = ((ramInfoPair.second / ramInfoPair.first.toFloat()) * 100).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun setScreenInfo() {
        runCatching {
            val display = mWindowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)

            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val densityDpi = metrics.densityDpi

            val supportsMultiTouch = packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
            binding.tvResolution.text = "${width}x${height}"
            binding.tvScreenAccuracy.text = "$densityDpi DPI"
            binding.tvMultiTouch.text = if (supportsMultiTouch) getString(R.string.string_support) else getString(R.string.string_no_support)
        }
    }

    private fun setBatteryInfo() {
        val batteryStatusReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                runCatching {
                    intent?.let {
                        val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
                        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                        val temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                        val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
                        val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                        val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

                        val currentCapacity = level / scale.toFloat()

                        val healthStatus = when (health) {
                            BatteryManager.BATTERY_HEALTH_GOOD -> getString(R.string.string_good)
                            BatteryManager.BATTERY_HEALTH_OVERHEAT -> getString(R.string.string_overheat)
                            BatteryManager.BATTERY_HEALTH_DEAD -> getString(R.string.string_dead)
                            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> getString(R.string.over_voltage)
                            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> getString(R.string.unspecified_failure)
                            else -> getString(R.string.string_unknown)
                        }


                        val batteryStatus = when (status) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.str_charging)
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.string_discharging)
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getString(R.string.not_charging)
                            BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.string_full)
                            else -> getString(R.string.string_unknown)
                        }
                        val chargePlug = when (plugged) {
                            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                            BatteryManager.BATTERY_PLUGGED_WIRELESS -> getString(R.string.string_wireless)
                            else -> getString(R.string.not_plugged)
                        }

                        binding.apply {
                            tvHealthStatus.text = healthStatus
                            val totalCapacity = CommonUtils.getTotalCapacity(this@DeviceInfoActivity)
                            tvCurrentCapacity.text = "${(currentCapacity * totalCapacity).toInt()} mAh"
                            tvTotalCapacity.text = "${totalCapacity.toInt()} mAh"
                            tvVoltage.text = "${String.format("%.1f", (voltage.toDouble() / 1000))} V"
                            tvTemperature.text = "${temperature / 10.0} °C"
                            tvBatteryStatus.text = batteryStatus
                            tvCharging.text = chargePlug
                            tvBatteryTechnology.text = technology

                        }
                    }
                }
            }
        }
        registerReceiver(batteryStatusReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun setSensorInf() {
        binding.apply {
            runCatching {
                val accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                val magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
                val orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null
                val gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
                val light = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null
                val proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
                val temperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null

                tvAccelerationSensor.text = if (accelerometer) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvMagneticFieldSensor.text = if (magneticField) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvOrientationSensor.text = if (orientation) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvGyroSensor.text = if (gyroscope) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvLightSensor.text = if (light) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvDistanceSensor.text = if (proximity) getString(R.string.string_support) else getString(R.string.string_no_support)
                tvTemperatureSensor.text = if (temperature) getString(R.string.string_support) else getString(R.string.string_no_support)

            }
        }
    }


    private fun showFullAd(b: () -> Unit) {

        if (ADManager.isOverAdMax()|| ADManager.isBlocked()) {
            b.invoke()
            return
        }

        // log : oc_scan_int
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_int"))

        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
            if (ADManager.ocScanIntLoader.canShow(this@DeviceInfoActivity)) {
                ADManager.ocScanIntLoader.showFullScreenAd(this@DeviceInfoActivity, "oc_scan_int") {
                    b.invoke()
                }
            } else {
                ADManager.ocScanIntLoader.loadAd(this@DeviceInfoActivity)
                b.invoke()
            }
        }
    }

    private var ad: AdType? = null
    private fun showNatAd() {

        if (ADManager.isOverAdMax()) {
            binding.adFr.isVisible = false
            return
        }
        TbaHelper.eventPost("oc_ad_chance", hashMapOf("ad_pos_id" to "oc_scan_nat"))
        ADManager.ocScanNatLoader.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(200L)
                if (ADManager.ocScanNatLoader.canShow(this@DeviceInfoActivity)) {
                    ad?.destroy()
                    binding.adFr.isVisible = true
                    ADManager.ocScanNatLoader.showNativeAd(this@DeviceInfoActivity, binding.adFr, "oc_scan_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
    }

}