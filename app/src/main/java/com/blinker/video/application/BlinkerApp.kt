package com.blinker.video.application

import android.app.Application
import com.blinker.video.ui.utils.AliyunOssUtil

/**
 * @author jiangshiyu
 * @date 2025/4/10
 */
class BlinkerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AliyunOssUtil.init(this)
    }
}