package com.blinker.video.model

import android.net.Uri
import androidx.annotation.Keep

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
@Keep
data class LocalMedia(
    val uri: Uri?,
    val isVideo: Boolean = false,
    val videoThumbPath: String? = null,
)