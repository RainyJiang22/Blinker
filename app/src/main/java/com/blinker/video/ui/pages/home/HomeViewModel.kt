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

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
class HomeViewModel : ViewModel() {

    val hotFeeds = Pager(config = PagingConfig(pageSize = 10, initialLoadSize = 10, enablePlaceholders = false), pagingSourceFactory = {
        HomePagingResource()
    }).flow.cachedIn(viewModelScope)

    inner class HomePagingResource: PagingSource<Long,Feed>() {
        override fun getRefreshKey(state: PagingState<Long, Feed>): Long? {
            return null
        }

        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Feed> {
            val result = kotlin.runCatching {
                ApiService.getService().getFeeds(feedId = params.key ?: 0L)
            }
            val apiResult = result.getOrDefault(ApiResult())
            if (apiResult.success && apiResult.body?.isNotEmpty() == true) {
                return LoadResult.Page(apiResult.body!!, null, apiResult.body?.last()?.id)
            }
            return LoadResult.Error(java.lang.RuntimeException("No more data to fetch"))
        }

    }
}