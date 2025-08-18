package com.blinker.video.ui.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets

class FakeStatusView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = context.getStatusBarHeight()
        setMeasuredDimension(widthMeasureSpec,height)
    }
}


class FakeNavigationBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = if (hasNavigationBar()) context.getNavigationBarHeight() else 0
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    private fun hasNavigationBar(): Boolean {
        var hasNavBar = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootWindowInsets?.let { windowInsets ->
                val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
                hasNavBar = insets.bottom > 0
            }
        } else {
            val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
            if (id > 0) {
                hasNavBar = context.resources.getBoolean(id)
            }
        }
        return hasNavBar
    }
}