package com.blinker.video.model


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */

const val TYPE_TEXT = 0 //文本类型帖子
const val TYPE_IMAGE_TEXT = 1 //图文类型帖子
const val TYPE_VIDEO = 2//视频类型帖子

@Keep
@Parcelize
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
    var ugc: Ugc?,
    val url: String?,
    val width: Int,
    var backgroundColor: Int = 0
) : Parcelable {
    fun getUgcOrDefault(): Ugc {
        if (ugc == null) {
            ugc = Ugc()
        }
        return ugc!!
    }
}

@Entity(tableName = "author")
@Keep
@Parcelize
data class Author(
    val avatar: String,
    val commentCount: Int,
    val description: String?,
    val expiresTime: Long,
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
    @ColumnInfo(name = "topCount", defaultValue = "0")
    val topCount: Int,
    @PrimaryKey(autoGenerate = false)
    val userId: Long,
): Parcelable {
    constructor() : this("", 0, "", 0, 0, 0, 0, 0, false, 0, 0, "", "", 0, 0, 0)
}

@Keep
@Parcelize
data class TopComment(
    val author: Author?,
    //val commentCount: Int,
    val commentId: Long,
    val commentText: String?,
    val commentType: Int,
    var commentUgc: Ugc?,
    val createTime: Long,
    //val hasLiked: Boolean,
    val height: Int,
    val id: Int,
    val imageUrl: String?,
    val itemId: Long,
    //val likeCount: Int,
    val userId: Long,
    val videoUrl: String?,
    val width: Int,
): Parcelable {
    fun getUgcOrDefault(): Ugc {
        if (commentUgc == null) {
            commentUgc = Ugc()
        }
        return commentUgc!!
    }
}

@Keep
@Parcelize
data class Ugc(
    var commentCount: Int,
    val hasFavorite: Boolean,
    var hasLiked: Boolean,
    var hasdiss: Boolean,
    var likeCount: Int,
    var shareCount: Int,
): Parcelable {
    constructor() : this(0, false, false, false, 0, 0)
}