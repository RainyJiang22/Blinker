package com.blinker.video.ui.pages.tags

import androidx.lifecycle.ViewModel
import com.blinker.video.http.ApiResult
import com.blinker.video.http.ApiService
import com.blinker.video.model.Feed
import com.blinker.video.ui.pages.login.UserManager

/**
 * @author jiangshiyu
 * @date 2025/9/28
 */
class TagsViewModel : ViewModel() {
    private var nextPageKey = 0L

    suspend fun loadData(refresh: Boolean = true): ApiResult<List<Feed>> {
        if (refresh) {
            nextPageKey = 0
        }
        val apiResult = ApiService.getService().getFeeds(
            feedId = nextPageKey,
            feedType = "all",
            userId = UserManager.userId()
        )
        nextPageKey = apiResult.body?.lastOrNull()?.id ?: 0
        return apiResult
    }
}