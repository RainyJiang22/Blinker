package com.blinker.video.ui.pages.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.AndroidViewModel
import com.blinker.video.base.BaseActivity
import com.blinker.video.databinding.ActivityNewLoginBinding
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

/**
 * @author jiangshiyu
 * @date 2025/10/11
 */
class NewLoginActivity : BaseActivity<ActivityNewLoginBinding, AndroidViewModel>() {

    private lateinit var bunn: Bunn

    override fun onBundle(bundle: Bundle) {

    }

    override fun init(savedInstanceState: Bundle?) {

        bunn = Bunn(binding?.animationView!!)

        setupBunnListeners()
    }
    private fun setupBunnListeners() {
        binding?.emailInputEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bunn.setPreTrackingState()
            }
        }

        binding?.passwordInputEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding?.passwordInputLayout?.isPasswordToggleChecked() == true)
                    bunn.setPeekState()
                else
                    bunn.setShyState()
            }
        }

        binding?.passwordInputLayout?.setPasswordToggleListener(object : OnPasswordToggleListener {
            override fun onPasswordToggleChanged(isChecked: Boolean) {
                if (!binding?.passwordInputEditText?.hasFocus()!!) return
                if (isChecked) {
                    bunn.setShyState()
                } else {
                    bunn.setPeekState()
                }
            }
        })

        binding?.emailInputEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Timber.d("emailInputWidth=${binding?.emailInputEditText?.width}, textWidth=${getTextWidth(binding?.emailInputEditText!!)}")
                bunn.setEyesPosition(getTextWidth(binding?.emailInputEditText!!) / binding?.emailInputEditText?.width!!)
            }

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                bunn.startTracking()
            }

        })
    }

    private fun getTextWidth(editText: TextInputEditText): Float {
        return editText.paint.measureText(editText.text.toString())
    }

}