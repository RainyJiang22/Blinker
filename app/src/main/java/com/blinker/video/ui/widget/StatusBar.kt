package com.blinker.video.ui.widget

import android.content.Context

/**
 * @author jiangshiyu
 * @date 2025/8/18
 */

private var result = -1
fun Context.getStatusBarHeight(): Int {
    if (result == -1) {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
    }
    return result
}

private var nav_result = 0
fun Context.getNavigationBarHeight(): Int {
    val rid: Int =
        resources.getIdentifier("config_showNavigationBar", "bool", "android")
    nav_result = if (rid != 0) {
        val resourceId: Int =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
    return nav_result
}
