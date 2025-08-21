package com.blinker.video.model

import androidx.annotation.Keep

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */

@Keep
data class TagList(
    val activityIcon: String?,
    val background: String?,
    val enterNum: Int,
    val feedNum: Int,
    val followNum: Int,
    val hasFollow: Boolean,
    val icon: String?,
    val id: Int,
    val intro: String?,
    val tagId: Int,
    val title: String?
)