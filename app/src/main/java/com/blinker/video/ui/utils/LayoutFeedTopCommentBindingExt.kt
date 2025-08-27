package com.blinker.video.ui.utils

import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFeedTopCommentBinding
import com.blinker.video.http.ApiService
import com.blinker.video.model.TopComment
import com.blinker.video.ui.pages.login.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2025/8/26
 */

fun LayoutFeedTopCommentBinding.bindComment(
    lifecycleOwner: LifecycleOwner,
    topComment: TopComment?,
    callback: (TopComment) -> Unit
) {
    root.setVisibility(topComment != null)
    mediaLayout.setVisibility(!TextUtils.isEmpty(topComment?.imageUrl))
    topComment?.run {
        commentAuthor.setTextVisibility(author?.name)
        commentAvatar.setImageUrl(author?.avatar, true)
        this@bindComment.commentText.setTextVisibility(commentText)
        commentLikeCount.setTextVisibility(this.getUgcOrDefault().likeCount.toString())
        commentPreviewVideoPlay.setVisibility(!TextUtils.isEmpty(videoUrl))
        commentPreview.setImageUrl(imageUrl)
        commentLikeCount.setTextColor(
            this.getUgcOrDefault().hasLiked,
            R.color.color_theme,
            R.color.color_3d3
        )
        commentLikeStatus.setImageResource(
            this.getUgcOrDefault().hasLiked,
            R.drawable.icon_cell_liked,
            R.drawable.icon_cell_like
        )

        commentLikeStatus.setOnClickListener {
            lifecycleOwner.lifecycleScope.launch {
                UserManager.loginIfNeed()
                UserManager.getUser().collectLatest {
                    if (it.userId > 0 || it.qqOpenId.isNotBlank()) {
                        val apiResult = ApiService.getService()
                            .toggleCommentLike(commentId, itemId, it.userId)
                        apiResult.body?.run {
                            val ugc = topComment.getUgcOrDefault()
                            ugc.hasLiked = this.getAsJsonPrimitive("hasLiked").asBoolean
                            ugc.likeCount = this.getAsJsonPrimitive("likeCount").asInt
                            callback(topComment)
                        }
                    }
                }
            }
        }
    }
}