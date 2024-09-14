package com.kk.newcleanx.ui.functions.recentapp.fragment

import android.content.Intent
import android.graphics.DashPathEffect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.LongSparseArray
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.ScreenTimeInfo
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.FrScreenTimeInfoBinding
import com.kk.newcleanx.ui.base.BaseFragment
import com.kk.newcleanx.ui.common.widget.TransitionFadeForRecycler
import com.kk.newcleanx.ui.functions.recentapp.adapter.ScreenTimeListAdapter
import com.kk.newcleanx.ui.functions.recentapp.vm.ScreenTimeViewModel
import com.kk.newcleanx.utils.CommonUtils.getDateRangeNameByIndex
import com.kk.newcleanx.utils.formatDuration
import com.kk.newcleanx.utils.formatTime
import com.kk.newcleanx.utils.removeStartPrefix
import com.kk.newcleanx.utils.showDateRangeSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScreenTimeInfoFragment : BaseFragment<FrScreenTimeInfoBinding>() {

    private val viewModel by viewModels<ScreenTimeViewModel>()
    private var selectorDateRangeIndex = 1
    private lateinit var screenListAdapter: ScreenTimeListAdapter
    private val settingsDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {
            dateRange.text = getDateRangeNameByIndex(selectorDateRangeIndex)
            initAdapter()
            barChart.setNoDataText("")
            progressbar.isVisible = true
            setupDateRangeClickListener()

            // Fetch initial data
            viewModel.getRangeTotalByIndex(selectorDateRangeIndex)
            viewModel.fetchListData(selectorDateRangeIndex)

            initObserver()
        }

    }

    private fun setupDateRangeClickListener() {
        binding.dateRange.setOnClickListener {
            ctx.showDateRangeSelector { value, text ->
                selectorDateRangeIndex = value
                binding.dateRange.text = text
                binding.progressbar.isVisible = true
                viewModel.getRangeTotalByIndex(value)
                viewModel.fetchListData(value)
            }
        }
    }

    private fun initObserver() {
        viewModel.chartLiveData.observe(viewLifecycleOwner) {
            binding.progressbar.isVisible = false
            initChartData(it)
        }
        viewModel.listLiveData.observe(viewLifecycleOwner) { list ->
            updateRecyclerView(list)
        }
    }

    private fun updateRecyclerView(list: List<ScreenTimeInfo>) {
        lifecycleScope.launch(Dispatchers.Main) {
            screenListAdapter.setNewData(emptyList())
            delay(150L)
            binding.recyclerView.let {
                TransitionManager.beginDelayedTransition(it, TransitionFadeForRecycler())
            }

            val sortedList = list.sortedByDescending { it.duration }
            binding.tvEmpty.isVisible = sortedList.isEmpty()

            screenListAdapter.max = sortedList.maxOfOrNull { it.duration } ?: 0L
            screenListAdapter.setNewData(sortedList)
        }
    }

    private fun initAdapter() {
        screenListAdapter = ScreenTimeListAdapter(ctx) { screenTimeInfo ->
            isToSettings = true
            settingsDetailsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${screenTimeInfo.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            })
        }
        binding.recyclerView.apply {
            itemAnimator = null
            adapter = screenListAdapter
        }
    }

    private fun initChartData(array: LongSparseArray<Long>) = runCatching {
        binding.barChart.apply {
            if (array.isNotEmpty()) {
                val entries = mutableListOf<BarEntry>()
                val timeList = mutableListOf<Long>()
                for (index in 0 until array.size()) {
                    timeList.add(array.keyAt(index))
                    entries.add(BarEntry(index.toFloat(), array.valueAt(index).toFloat()))
                }

                val dataset = createBarDataSet(entries)

                setScaleEnabled(false)
                setTouchEnabled(false)
                isDoubleTapToZoomEnabled = false
                setDrawGridBackground(false)
                setNoDataText("")
                setPadding(0, 0, 0, 0)
                setFitBars(true)
                description.isEnabled = false
                legend.isEnabled = false

                xAxis.setupXAxis(timeList)

                axisLeft.axisMinimum = 0f
                axisLeft.isEnabled = false

                axisRight.setupRightAxis()

                this.data = BarData(dataset).apply { barWidth = 0.8f }
                invalidate()
            } else {
                data = null
                invalidate()
            }
        }
    }

    private fun createBarDataSet(entries: List<BarEntry>) = BarDataSet(entries, "AppScreenTimeChart").apply {
        setDrawValues(false)
        color = ctx.getColor(R.color.color_21b772)
    }


    private fun XAxis.setupXAxis(timeList: List<Long>) = runCatching {
        setDrawGridLines(false)
        position = XAxis.XAxisPosition.BOTTOM
        textColor = ctx.getColor(R.color.main_text_color)
        axisLineColor = ctx.getColor(R.color.color_bcbcbc)
        granularity = 1.0f
        mLabelHeight = 2
        isGranularityEnabled = true
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < timeList.size) {
                    when (selectorDateRangeIndex) {
                        0, 1, 2 -> timeList.getOrNull(value.toInt())?.formatTime("HH:mm") ?: ""
                        3 -> timeList.getOrNull(value.toInt())?.formatTime("MM.dd")?.removeStartPrefix() ?: ""
                        else -> ""
                    }
                } else ""
            }
        }
    }

    private fun YAxis.setupRightAxis() = runCatching {
        setDrawZeroLine(false)
      //  setDrawLabels(false)
        axisLineColor = ctx.getColor(R.color.transparent)
        zeroLineColor = ctx.getColor(R.color.transparent)
        textColor = ctx.getColor(R.color.main_text_color)
        gridColor = ctx.getColor(R.color.color_bcbcbc)
        gridLineWidth = 0f
        axisMinimum = 0f
        setLabelCount(6, false)
        setGridDashedLine(DashPathEffect(floatArrayOf(5f, 5f), 0F))
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toLong().formatDuration()
        }
    }

}