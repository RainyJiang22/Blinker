package com.blinker.video.ui.utils

import com.blinker.video.http.ApiService
import com.blinker.video.model.Feed
import com.blinker.video.model.Ugc
import com.blinker.video.ui.pages.login.UserManager
import kotlinx.coroutines.flow.collectLatest

/**
 * @author jiangshiyu
 * @date 2025/8/28
 */

suspend fun Feed.toggleFeedLike(like: Boolean, callback: (Ugc) -> Unit) {
    UserManager.loginIfNeed()
    UserManager.getUser().collectLatest {
        if (it.userId <= 0) return@collectLatest
        val apiResult = if (like) ApiService.getService()
            .toggleFeedLike(itemId, it.userId) else ApiService.getService()
            .toggleDissFeed(itemId, it.userId)
        apiResult.body?.run {
            val ugc = Ugc()
            ugc.hasLiked = this.getAsJsonPrimitive("hasLiked").asBoolean
            ugc.hasdiss = this.getAsJsonPrimitive("hasdiss").asBoolean
            ugc.likeCount = this.getAsJsonPrimitive("likeCount").asInt
            callback(ugc)
        }
    }
}

suspend fun Feed.toggleFavorite(callback: (Boolean) -> Unit) {
    UserManager.loginIfNeed()
    UserManager.getUser().collectLatest {
        if (it.userId <= 0) return@collectLatest
        val apiResult = ApiService.getService().toggleFeedFavorite(it.userId, itemId)
        apiResult.body?.run {
            val hasFavorite = this.getAsJsonPrimitive("hasFavorite").asBoolean
            callback(hasFavorite)
        }
    }
}