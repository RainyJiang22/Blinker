package com.blinker.video.ui.utils

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.palette.graphics.Palette
import com.blinker.video.R
import com.blinker.video.model.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.drawable.toDrawable

/**
 * @author jiangshiyu
 * @date 2025/8/26
 */
fun ImageView.bindFeedImage(lifecycleOwner: LifecycleOwner, feed: Feed,maxHeight: Int) {
    if (TextUtils.isEmpty(feed.cover)) {
        visibility = View.GONE
        return
    }
    visibility = View.VISIBLE
    load(feed.cover!!) {
        if (feed.width <= 0 && feed.height <= 0) {
            setFeedImageSize(it.width, it.height, maxHeight)
        }
        if (feed.backgroundColor == 0) {
            lifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO) {
                val defaultColor = context.getColor(R.color.color_theme_10)
                val color = Palette.Builder(it).generate().getMutedColor(defaultColor)
                feed.backgroundColor = color
                withContext(lifecycleOwner.lifecycle.coroutineScope.coroutineContext) {
                    background = feed.backgroundColor.toDrawable()
                }
            }
        } else {
            background = feed.backgroundColor.toDrawable()
        }
    }

    if (feed.width > 0 && feed.height > 0) {
        setFeedImageSize(feed.width, feed.height, maxHeight)
    }
}

fun ImageView.setFeedImageSize(width: Int, height: Int, maxHeight: Int) {
    val finalWidth: Int = PixUtil.getScreenWidth();
    val finalHeight: Int = if (width > height) {
        (height / (width * 1.0f / finalWidth)).toInt()
    } else {
        maxHeight
    }
    val params = layoutParams as LinearLayout.LayoutParams
    params.width = finalWidth
    params.height = finalHeight
    params.gravity = Gravity.CENTER
    scaleType = ImageView.ScaleType.FIT_CENTER
    layoutParams = params
}