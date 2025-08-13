package com.blinker.video.model


import androidx.annotation.Keep

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */

const val TYPE_TEXT = 0 //纯文本类型帖子
const val TYPE_IMAGE_TEXT = 1 //图文类型帖子
const val TYPE_VIDEO = 2//视频类型帖子

@Keep
data class Feed(
    val activityIcon: String?,
    val activityText: String?,
    val author: Author?,
    val authorId: Long,
    val cover: String?,
    val createTime: Long,
    val duration: Double,
    val feedsText: String?,
    val height: Int,
    val id: Long,
    val itemId: Long,
    val itemType: Int,
    val topComment: TopComment?,
    val ugc: Ugc?,
    val url: String?,
    val width: Int
) {
    var backgroundColor: Int = 0
}

@Keep
data class Author(
    val avatar: String,
    val commentCount: Int,
    val description: String,
    val expiresTime: Int,
    val favoriteCount: Int,
    val feedCount: Int,
    val followCount: Int,
    val followerCount: Int,
    val hasFollow: Boolean,
    val historyCount: Int,
    val likeCount: Int,
    val name: String,
    val qqOpenId: String,
    val score: Int,
    val topCount: Int,
    val userId: Long,
)

@Keep
data class TopComment(
    val author: Author?,
    val commentCount: Int,
    val commentId: Long,
    val commentText: String?,
    val commentType: Int,
    val commentUgc: Ugc?,
    val createTime: Long,
    val hasLiked: Boolean,
    val height: Int,
    val id: Int,
    val imageUrl: String?,
    val itemId: Long,
    val likeCount: Int,
    val userId: Long,
    val videoUrl: String?,
    val width: Int,
)

@Keep
data class Ugc(
    val commentCount: Int,
    val hasFavorite: Boolean,
    val hasLiked: Boolean,
    val hasdiss: Boolean,
    val likeCount: Int,
    val shareCount: Int,
)