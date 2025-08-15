package com.blinker.video.ui.pages.home

import android.util.Log
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
import java.util.logging.Logger

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
class HomeViewModel : ViewModel() {

    val hotFeeds = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            enablePlaceholders = false,
            prefetchDistance = 1
        ), pagingSourceFactory = {
            HomePagingResource()
        }).flow.cachedIn(viewModelScope)

    private var feedType: String = "all"
    fun setFeedType(feedType: String) {
        this.feedType = feedType
    }

    inner class HomePagingResource : PagingSource<Long, Feed>() {
        override fun getRefreshKey(state: PagingState<Long, Feed>): Long? {
            return null
        }

        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Feed> {
            val feedId = params.key ?: 0L
            val result = runCatching {
                ApiService.getService().getFeeds(
                    feedId = feedId,
                    feedType = feedType,
                    userId = UserManager.userId()
                )
            }

            if (result.isFailure) {
                result.exceptionOrNull()?.printStackTrace()
            }
            val apiResult = result.getOrDefault(ApiResult())

            return if (apiResult.success && !apiResult.body.isNullOrEmpty()) {
                val data = apiResult.body!!
                val nextKey = if (data.isEmpty()) null else data.last().itemId

                Log.i("resource", "load:$data ")
                LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (nextKey == feedId) null else nextKey
                )
            } else {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null // 没有更多数据时必须 null
                )
            }
        }
    }
}