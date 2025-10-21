package com.blinker.video.ui.pages.login

import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber


fun TextInputLayout.setPasswordToggleListener(listener: OnPasswordToggleListener) {
    // 监听 end icon 点击
    this.setEndIconOnClickListener {
        // endIconChecked 表示当前密码是否可见
        listener.onPasswordToggleChanged(this.isEndIconVisible.not())
        Timber.d("Password toggle clicked, isChecked = ${this.isEndIconVisible.not()}")
    }
}

fun TextInputLayout.isPasswordToggleChecked(): Boolean {
    return this.isEndIconVisible.not()
}

interface OnPasswordToggleListener {
    fun onPasswordToggleChanged(isChecked: Boolean)
}