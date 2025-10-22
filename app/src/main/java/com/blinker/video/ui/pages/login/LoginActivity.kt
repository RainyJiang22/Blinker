package com.blinker.video.ui.pages.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blinker.video.databinding.ActivityLayoutLoginBinding
import com.blinker.video.ui.utils.invokeViewBinding
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2025/1/10
 * 登录
 */
class LoginActivity : AppCompatActivity() {

    private val viewBinding: ActivityLayoutLoginBinding by invokeViewBinding()
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.actionClose.setOnClickListener { finish() }
        viewModel.initTencent(applicationContext)

        viewBinding.actionLogin.setOnClickListener {
            viewModel.login(this)
        }

        collectUiState()
    }

    private fun collectUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LoginState.Idle -> Unit
                    is LoginState.Loading -> {
                    }
                    is LoginState.Success -> {
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
