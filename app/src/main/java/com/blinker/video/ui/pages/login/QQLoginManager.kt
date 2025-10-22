package com.blinker.video.ui.pages.login

import android.app.Activity
import android.content.Context
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * @author jiangshiyu
 * @date 2025/10/22
 */
class QQLoginManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: QQLoginManager? = null

        /**
         * 获取单例实例
         */
        fun getInstance(): QQLoginManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QQLoginManager().also { INSTANCE = it }
            }
        }
    }

    private var tencent: Tencent? = null

    // QQ 登录结果状态流
    private val _loginResult = MutableStateFlow<QQLoginResult?>(null)
    val loginResult: StateFlow<QQLoginResult?> = _loginResult.asStateFlow()

    /**
     * 初始化 QQ SDK
     * @param context 应用上下文
     * @param appId QQ 应用 ID
     */
    fun init(context: Context, appId: String) {
        // 设置权限授权状态
        tencent = Tencent.createInstance(appId, context.applicationContext)
    }

    /**
     * 启动 QQ 登录
     * @param activity 当前 Activity
     */
    fun startQQLogin(activity: Activity) {
        if (tencent == null) {
            _loginResult.value = QQLoginResult.Error("QQ SDK 未初始化")
            return
        }
        tencent?.login(activity, "all", qqLoginListener)
    }


    val qqLoginListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            try {
                val jsonObject = response as JSONObject
                val accessToken = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN)
                val openId = jsonObject.getString(Constants.PARAM_OPEN_ID)
                val expiresInTime = tencent?.expiresIn
                val nickname = jsonObject.optString("nickname")
                val avatar = jsonObject.optString("figureurl_2")
                // 发送登录成功结果
                _loginResult.value = QQLoginResult.Success(
                    accessToken, openId, nickname, avatar,
                    expiresInTime
                )
            } catch (e: Exception) {
                _loginResult.value = QQLoginResult.Error("解析登录结果失败: ${e.message}")
            }
        }

        override fun onError(error: UiError?) {
            _loginResult.value = QQLoginResult.Error("登录失败: ${error?.errorMessage}")
        }

        override fun onCancel() {
            _loginResult.value = QQLoginResult.Cancel
        }

        override fun onWarning(p0: Int) {
        }

    }

    /**
     * 清除登录结果状态
     */
    fun clearLoginResult() {
        _loginResult.value = null
    }

}

/**
 * QQ 登录结果密封类
 */
sealed class QQLoginResult {
    /**
     * 登录成功
     * @param accessToken 访问令牌
     * @param openId 用户唯一标识
     * @param nickName 昵称
     * @param avatar 用户头像
     * @param expiresIn 有效期
     */
    data class Success(
        val accessToken: String,
        val openId: String,
        val nickName: String,
        val avatar: String,
        val expiresIn: Long?,
    ) : QQLoginResult()

    /**
     * 登录失败
     * @param message 错误信息
     */
    data class Error(val message: String) : QQLoginResult()

    /**
     * 用户取消登录
     */
    object Cancel : QQLoginResult()
}