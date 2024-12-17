package com.blinker.video.ui.utils

import android.view.View

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
fun View.setVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}