package com.blinker.video.ui.utils

import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner

/**
 * @author jiangshiyu
 * @date 2025/8/26
 */
interface IViewBinding {

    fun getLayoutInflater(): LayoutInflater

    fun getLifecycleOwner(): LifecycleOwner
}