package com.blinker.video.ui.pages.detail

import androidx.paging.PagingSource
import com.blinker.video.http.ApiResult
import com.blinker.video.http.ApiService
import com.blinker.video.model.TopComment
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.AbsPagingViewModel

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class FeedCommentViewModel : AbsPagingViewModel<TopComment>() {

    private var itemId: Long = 0
    fun setItemId(itemId: Long) {
        this.itemId = itemId
    }

    override suspend fun doLoadPage(params: PagingSource.LoadParams<Long>): ApiResult<List<TopComment>> {
        val apiResult = ApiService.getService()
            .getFeedCommentList(UserManager.userId(), itemId, params.key ?: 0)
        apiResult.nextPageKey = apiResult.body?.lastOrNull()?.commentId
        return apiResult
    }
}