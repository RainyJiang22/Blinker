package com.blinker.video.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

/**
 * @author jiangshiyu
 * @date 2025/8/26
 */
open class BackPressedEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defAttributeSet: Int = 0,
) : AppCompatEditText(context, attrs, defAttributeSet) {

    private var onBackKeyEvent: OnBackKeyEvent? = null

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            if (onBackKeyEvent?.onBackKeyPressed() == true) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    open fun setOnBackKeyEventListener(event: OnBackKeyEvent) {
        this.onBackKeyEvent = event
    }

    interface OnBackKeyEvent {
        fun onBackKeyPressed(): Boolean
    }

}