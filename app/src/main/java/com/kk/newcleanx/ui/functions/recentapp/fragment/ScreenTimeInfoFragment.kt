package com.kk.newcleanx.ui.functions.recentapp.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.kk.newcleanx.databinding.FrRecentAppLaunchesBinding
import com.kk.newcleanx.databinding.FrScreenTimeInfoBinding
import com.kk.newcleanx.ui.base.BaseFragment
import com.kk.newcleanx.ui.functions.recentapp.adapter.ScreenTimeListAdapter
import com.kk.newcleanx.ui.functions.recentapp.vm.ScreenTimeViewModel

class ScreenTimeInfoFragment : BaseFragment<FrScreenTimeInfoBinding>() {


    private val viewModel by viewModels<ScreenTimeViewModel>()
    private var selectorDateRangeIndex = 1
    private lateinit var screenListAdapter: ScreenTimeListAdapter
    private val settingsDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

}