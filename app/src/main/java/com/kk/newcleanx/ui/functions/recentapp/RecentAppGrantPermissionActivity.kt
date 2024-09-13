package com.kk.newcleanx.ui.functions.recentapp

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.INTENT_KEY
import com.kk.newcleanx.data.local.INTENT_KEY_1
import com.kk.newcleanx.data.local.isToSettings
import com.kk.newcleanx.databinding.AcRecentAppPermissionBinding
import com.kk.newcleanx.ui.base.BaseActivity
import com.kk.newcleanx.ui.common.PermissionSettingDialogActivity
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.CommonUtils.hasUsageStatsPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecentAppGrantPermissionActivity : BaseActivity<AcRecentAppPermissionBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, RecentAppGrantPermissionActivity::class.java))
        }
    }

    override fun topView(): View {
        return binding.toolbar.root
    }

    private val perResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasUsageStatsPermission()) {
               RecentAppUsedInfoActivity.start(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.tvTitle.text = getString(R.string.grant_permission)
            toolbar.ivBack.setColorFilter(ContextCompat.getColor(this@RecentAppGrantPermissionActivity, R.color.main_text_color), PorterDuff.Mode.SRC_IN)
            toolbar.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnOk.setOnClickListener {

                runCatching {
                    isToSettings = true

                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    if (CommonUtils.isAtLeastAndroid10()) {
                        intent.data = Uri.fromParts("package", packageName, null)
                    }
                    perResult.launch(intent.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    })

                    lifecycleScope.launch {
                        delay(350)
                        startActivity(Intent(this@RecentAppGrantPermissionActivity, PermissionSettingDialogActivity::class.java).apply {
                            putExtra(INTENT_KEY, getString(R.string.how_to_do))
                            putExtra(INTENT_KEY_1, getString(R.string.make_sure_this_option_is_turned_on))
                        })
                    }
                }

            }
        }
    }

}