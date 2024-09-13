package com.kk.newcleanx.ui.functions.recentapp

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.AcRecentAppUsedInfoBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.functions.recentapp.fragment.AppLaunchesFragment
import com.kk.newcleanx.ui.functions.recentapp.fragment.ScreenTimeInfoFragment

class RecentAppUsedInfoActivity : BaseActivity<AcRecentAppUsedInfoBinding>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, RecentAppUsedInfoActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            toolbar.tvTitle.text = getString(R.string.recent_app)
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@RecentAppUsedInfoActivity, R.color.main_text_color), PorterDuff.Mode.SRC_IN)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            launches.setOnClickListener {
                viewPager.currentItem = 0
                setBtn(0)
            }

            screenTime.setOnClickListener {
                viewPager.currentItem = 1
                setBtn(1)
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    setBtn(position)
                }
            })


            val fragmentList = mutableListOf<Fragment>(AppLaunchesFragment(), ScreenTimeInfoFragment())
            viewPager.isUserInputEnabled = true
            viewPager.adapter = ViewPageAdapter(this@RecentAppUsedInfoActivity, fragmentList)
            viewPager.offscreenPageLimit = 2
        }
    }


    private fun setBtn(position: Int) {

        binding.apply {
            if (position == 0) {
                launches.setBackgroundResource(R.drawable.shape_btn_gradient_bg_r4)
                launches.setTextColor(ContextCompat.getColor(this@RecentAppUsedInfoActivity, R.color.white))

                screenTime.setBackgroundResource(R.drawable.shape_dde2e9_r4)
                screenTime.setTextColor(ContextCompat.getColor(this@RecentAppUsedInfoActivity, R.color.color_b5bac1))
            } else {
                launches.setBackgroundResource(R.drawable.shape_dde2e9_r4)
                launches.setTextColor(ContextCompat.getColor(this@RecentAppUsedInfoActivity, R.color.color_b5bac1))

                screenTime.setBackgroundResource(R.drawable.shape_btn_gradient_bg_r4)
                screenTime.setTextColor(ContextCompat.getColor(this@RecentAppUsedInfoActivity, R.color.white))
            }

        }
    }

    inner class ViewPageAdapter(
        activity: FragmentActivity,
        private val list: MutableList<Fragment>
    ) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = list.size

        override fun createFragment(position: Int): Fragment = list[position]
    }

}