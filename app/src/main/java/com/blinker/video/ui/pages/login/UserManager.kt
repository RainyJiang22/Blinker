package com.blinker.video.ui.pages.login

import android.content.Intent
import com.blinker.video.ui.utils.AppGlobals

/**
 * @author jiangshiyu
 * @date 2025/1/17
 */
object UserManager {

    fun startLogin() {
        val intent = Intent(AppGlobals.getApplication(),LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        AppGlobals.getApplication().startActivity(intent)
    }

}