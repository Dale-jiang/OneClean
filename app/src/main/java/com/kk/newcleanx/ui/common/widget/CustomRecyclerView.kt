package com.kk.newcleanx.ui.common.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R

class CustomRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RecyclerView(context, attrs, defStyleAttr) {

    private var maxHeight: Int = -1

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView)
        maxHeight = arr.getLayoutDimension(R.styleable.CustomRecyclerView_maxHeight, maxHeight)
        arr.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpecNew = heightMeasureSpec
        if (maxHeight > 0) {
            heightMeasureSpecNew = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpecNew)
    }


}