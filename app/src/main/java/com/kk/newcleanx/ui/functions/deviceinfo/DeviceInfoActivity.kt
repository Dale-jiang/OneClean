package com.kk.newcleanx.ui.functions.deviceinfo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.app
import com.kk.newcleanx.databinding.AcDeviceInfoBinding
import com.kk.newcleanx.ui.base.AllFilePermissionActivity
import com.kk.newcleanx.ui.functions.clean.JunkScanningActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            toolbar.tvTitle.text = getString(R.string.device_status)

            startProgress(minWaitTime = 2000L) {
                if (it >= 100) {
                    clLoading.isVisible = false
                    viewLottie.cancelAnimation()
                }
            }

            btnClean.setOnClickListener {
                JunkScanningActivity.start(this@DeviceInfoActivity)
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

            val windowManager = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
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
                            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                            else -> "Unknown"
                        }


                        val batteryStatus = when (status) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.str_charging)
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                            BatteryManager.BATTERY_STATUS_FULL -> "Full"
                            else -> "Unknown"
                        }
                        val chargePlug = when (plugged) {
                            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                            else -> "Not Plugged"
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


}