package com.kk.newcleanx.ui.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST", "DEPRECATION")
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private lateinit var _binding: VB
    protected val binding get() = _binding
    protected open val isLight = true
    protected open val isBottomPadding = true
    open fun topView(): View? = null

    override fun onAttachedToWindow() {
        setImmersiveBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDensity()
        _binding = inflateBinding()
        setContentView(_binding.root)
    }

    private fun inflateBinding(): VB {
        val type = (javaClass.genericSuperclass as java.lang.reflect.ParameterizedType).actualTypeArguments[0] as Class<VB>
        val method: Method = type.getMethod("inflate", android.view.LayoutInflater::class.java)
        return method.invoke(null, layoutInflater) as VB
    }

    private fun setDensity() {
        resources.displayMetrics.runCatching {
            density = heightPixels / 765f
            densityDpi = (density * 160).toInt()
            scaledDensity = density
        }
    }

    private fun setImmersiveBar() {
        window.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.WHITE
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = true
            }
            ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insetsCompat ->
                val systemBarsInsets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
                topView()?.setPadding(0, systemBarsInsets.top, 0, 0)
                decorView.setPadding(0, 0, 0, if (isBottomPadding) systemBarsInsets.bottom else 0)
                insetsCompat
            }
        }
    }
}