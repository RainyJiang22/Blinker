package com.blinker.video.ui.pages.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.blinker.video.http.ApiResult
import com.blinker.video.http.ApiService
import com.blinker.video.model.Feed
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.AbsPagingViewModel

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
class HomeViewModel : AbsPagingViewModel<Feed>() {
    private var feedType: String = "all"
    fun setFeedType(feedType: String) {
        this.feedType = feedType
    }

    override suspend fun doLoadPage(params: PagingSource.LoadParams<Long>): ApiResult<List<Feed>> {
        val apiResult = ApiService.getService().getFeeds(
            feedId = params.key ?: 0L,
            feedType = feedType,
            userId = UserManager.userId()
        )
        apiResult.nextPageKey = apiResult.body?.lastOrNull()?.itemId
        return apiResult
    }
}