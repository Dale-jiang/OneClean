package com.kk.newcleanx.ui.functions.recentapp.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.INTENT_KEY_1
import com.kk.newcleanx.data.local.LaunchesItem
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.FrRecentAppLaunchesBinding
import com.kk.newcleanx.ui.base.BaseFragment
import com.kk.newcleanx.ui.common.PermissionSettingDialogActivity
import com.kk.newcleanx.ui.common.widget.TransitionFadeForRecycler
import com.kk.newcleanx.ui.functions.recentapp.adapter.AppLaunchesListAdapter
import com.kk.newcleanx.ui.functions.recentapp.vm.LaunchesViewModel
import com.kk.newcleanx.utils.CommonUtils.getDateRangeNameByIndex
import com.kk.newcleanx.utils.CommonUtils.getDateRangePairByIndex
import com.kk.newcleanx.utils.showDateRangeSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppLaunchesFragment : BaseFragment<FrRecentAppLaunchesBinding>() {

    private val viewModel by viewModels<LaunchesViewModel>()
    private var selectorIndex = 0
    private var selectorDateRangeIndex = 1
    private lateinit var launchesListAdapter: AppLaunchesListAdapter

    private val settingsDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshLaunchData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        onSelectorIndexChanged()
        binding.dateRange.text = getDateRangeNameByIndex(selectorDateRangeIndex)
        initAdapter()
        refreshLaunchData()

        binding.dateRange.setOnClickListener {
            ctx.showDateRangeSelector { value, text ->
                selectorDateRangeIndex = value
                binding.dateRange.text = text
                refreshLaunchData()
            }
        }

        setupSelectorListeners()
        initObserver()
    }

    private fun setupSelectorListeners() {
        binding.layoutTotal.setOnClickListener { updateSelector(0) }
        binding.layoutForeground.setOnClickListener { updateSelector(1) }
        binding.layoutBackground.setOnClickListener { updateSelector(2) }
    }

    private fun updateSelector(index: Int) {
        selectorIndex = index
        onSelectorIndexChanged()
        viewModel.launchesListLiveData.postValue(viewModel.launchesListLiveData.value)
    }

    private fun refreshLaunchData() {
        getDateRangePairByIndex(selectorDateRangeIndex).let {
            binding.progressbar.isVisible = true
            viewModel.queryAppLaunches(it.first, it.second)
        }
    }

    private fun initObserver() {
        viewModel.launchesListLiveData.observe(viewLifecycleOwner) { list ->
            binding.progressbar.isVisible = false
            updateLaunchCounts(list)

            val filteredList = getFilteredLaunchList(list)
            updateRecyclerView(filteredList)
        }
    }

    private fun updateLaunchCounts(list: List<LaunchesItem>) {
        binding.totalCounts.text = "${list.sumOf { it.totalCount }}"
        binding.foregroundCounts.text = "${list.sumOf { it.foreground }}"
        binding.backgroundCounts.text = "${list.sumOf { it.background }}"
    }

    private fun getFilteredLaunchList(list: List<LaunchesItem>): MutableList<LaunchesItem> {
        return when (selectorIndex) {
            2 -> list.filter { it.background != 0 }.sortedByDescending { it.background }.toMutableList()
            1 -> list.filter { it.foreground != 0 }.sortedByDescending { it.foreground }.toMutableList()
            else -> list.filter { it.totalCount != 0 }.sortedByDescending { it.totalCount }.toMutableList()
        }
    }

    private fun updateRecyclerView(listData: MutableList<LaunchesItem>) {
        runCatching {
            lifecycleScope.launch(Dispatchers.Main) {
                launchesListAdapter.initData(mutableListOf(), selectorIndex)
                delay(150L)
                binding.recyclerView.apply {
                    TransitionManager.beginDelayedTransition(this, TransitionFadeForRecycler())
                }
                binding.tvEmpty.isVisible = listData.isEmpty()
                launchesListAdapter.initData(listData, selectorIndex)
            }
        }
    }

    private fun onSelectorIndexChanged() = runCatching {
        binding.layoutTotal.isSelected = selectorIndex == 0
        binding.layoutForeground.isSelected = selectorIndex == 1
        binding.layoutBackground.isSelected = selectorIndex == 2
    }

    private fun initAdapter() {
        launchesListAdapter = AppLaunchesListAdapter(ctx) { openAppSettings(it.packageName) }
        binding.recyclerView.apply {
            itemAnimator = null
            adapter = launchesListAdapter
        }
    }

    private fun openAppSettings(packageName: String) = runCatching {
        lifecycleScope.launch(Dispatchers.Main) {
            isToSettings = true
            settingsDetailsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }.apply { addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) })
            delay(350L)
            startActivity(Intent(ctx, PermissionSettingDialogActivity::class.java).apply {
                putExtra(INTENT_KEY, getString(R.string.how_to_do))
                putExtra(INTENT_KEY_1, getString(R.string.tap_force_stop_to_quit_the_running_app_completely))
            })
        }
    }
}