package com.blinker.video.ui.pages.login

/**
 * @author jiangshiyu
 * @date 2025/10/11
 */

fun map(value: Float, iStart: Float, iStop: Float, oStart: Float, oStop: Float): Float {
    return oStart + (oStop - oStart) * ((value - iStart) / (iStop - iStart))
}