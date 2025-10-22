package com.blinker.video.ui.pages.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blinker.video.http.ApiService
import com.blinker.video.ui.utils.AppConfig
import com.blinker.video.ui.utils.AppGlobals
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * @author jiangshiyu
 * @date 2025/10/22
 */
class LoginViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private var tencent: Tencent? = null

    fun initTencent(context: Context) {
        if (tencent == null) {
            tencent = Tencent.createInstance(AppConfig.QQ_APP_ID, context.applicationContext)
        }
    }

    fun login(activity: Activity) {
        _uiState.value = LoginState.Loading
        tencent?.login(activity, "all", loginListener)
    }

    private val loginListener = object : IUiListener {
        override fun onComplete(response: Any?) {
            val json = response as JSONObject
            val openid = json.getString(Constants.PARAM_OPEN_ID)
            val accessToken = json.getString(Constants.PARAM_ACCESS_TOKEN)
            val expiresIn = json.getLong(Constants.PARAM_EXPIRES_IN)

            tencent?.openId = openid
            tencent?.setAccessToken(accessToken, expiresIn.toString())

            getUserInfo()
        }

        override fun onError(err: UiError) {
            _uiState.value = LoginState.Error("登录失败: ${err.errorMessage}")
        }

        override fun onCancel() {
            _uiState.value = LoginState.Error("登录已取消")
        }

        override fun onWarning(p0: Int) {}
    }

    private fun getUserInfo() {
        val userInfo = UserInfo(AppGlobals.getApplication(), tencent!!.qqToken)
        userInfo.getUserInfo(object : IUiListener {
            override fun onComplete(any: Any) {
                val response = any as JSONObject
                val nickname = response.optString("nickname")
                val avatar = response.optString("figureurl_2")
                save(nickname, avatar)
            }

            override fun onError(err: UiError) {
                _uiState.value = LoginState.Error("获取用户信息失败")
            }

            override fun onCancel() {
                _uiState.value = LoginState.Error("取消获取用户信息")
            }

            override fun onWarning(p0: Int) {}
        })
    }

    private fun save(nickname: String, avatar: String) {
        viewModelScope.launch {
            try {
                val apiResult = ApiService.getService().saveUser(
                    nickname,
                    avatar,
                    tencent!!.openId,
                    tencent!!.expiresIn
                )
                if (apiResult.success && apiResult.body != null) {
                    UserManager.save(apiResult.body!!)
                    _uiState.value = LoginState.Success
                } else {
                    _uiState.value = LoginState.Error("登录失败")
                }
            } catch (e: Exception) {
                _uiState.value = LoginState.Error("网络错误: ${e.message}")
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener)
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
