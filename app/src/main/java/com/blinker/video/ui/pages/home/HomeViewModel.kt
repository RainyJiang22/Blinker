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

    private var feedType: String = "all"

    // 保存 PagingSource 引用，方便刷新
    private var currentSource: HomePagingResource? = null

    val hotFeeds = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            enablePlaceholders = false,
            prefetchDistance = 1
        ),
        pagingSourceFactory = {
            HomePagingResource().also { currentSource = it }
        }
    ).flow.cachedIn(viewModelScope)

    private var feedTypeInitialized = false

    fun setFeedType(feedType: String) {
        if (this.feedType != feedType) {
            this.feedType = feedType

            if (feedTypeInitialized) {
                // 只有后续切换才刷新
                currentSource?.invalidate()
            } else {
                // 第一次只是初始化，不刷新
                feedTypeInitialized = true
            }
        }
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
                    feedType = feedType, // 用最新的 feedType
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

                Log.i("resource", "load:$data")
                LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (nextKey == feedId) null else nextKey
                )
            } else {
                //没有更多数据
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        }
    }
}