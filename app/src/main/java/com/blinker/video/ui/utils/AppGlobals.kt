package com.blinker.video.ui.utils

import android.app.Application

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */


private var sApplication: Application? = null


object AppGlobals {

    fun getApplication(): Application {
        if (sApplication == null) {
            kotlin.runCatching {
                sApplication =
                    Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                        .invoke(null, *emptyArray()) as Application
            }.onFailure {
                it.printStackTrace()
            }
        }
        return sApplication!!
    }
}