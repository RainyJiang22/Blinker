package com.blinker.video.application

import android.app.Application
import com.blinker.video.ui.pages.login.QQLoginManager
import com.blinker.video.ui.utils.AliyunOssUtil
import com.blinker.video.ui.utils.AppConfig
import timber.log.Timber

/**
 * @author jiangshiyu
 * @date 2025/10/21
 */

class QQTask(val appContext: Application) : Task {
    override val name: String
        get() = "QQTask"

    override val isAsync: Boolean
        get() = true

    override suspend fun execute() {
        QQLoginManager.getInstance().init(appContext, AppConfig.QQ_APP_ID)
    }
}

class OssTask(val appContext: Application) : Task {

    override val name: String
        get() = "AliyunOssTask"

    override val isAsync: Boolean
        get() = true

    override suspend fun execute() {
        AliyunOssUtil.init(appContext)
    }
}


class TimberTask() : Task {
    override val name: String
        get() = "TimerTask"

    override val isAsync: Boolean
        get() = true

    override suspend fun execute() {
        Timber.plant(Timber.DebugTree())
    }
}