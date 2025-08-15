package com.blinker.video.http

import com.blinker.video.model.Author
import com.blinker.video.model.Feed
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */
interface IApiInterface {

    /**
     * 查询帖子列表
     * @param feedId 帖子的id, 分页时传列表最后一个帖子的id
     * @param feedType 查询的帖子的类型，all：全部类型，pics:仅图片类型，video:仅视频类型，text:仅文本类型
     * @param pageCount 分页的数量
     * @param userId 当前登陆者的id
     */
    @GET("feeds/queryHotFeedsList")
    suspend fun getFeeds(
        @Query("feedId") feedId: Long = 0,
        @Query("feedType") feedType: String = "all",
        @Query("pageCount") pageCount: Int = 10,
        @Query("userId") userId: Long = 0,
    ): ApiResult<List<Feed>>


    @GET("user/insert")
    suspend fun saveUser(
        @Query("name") name: String,
        @Query("avatar") avatar: String,
        @Query("qqOpenId") qq0penId: String,
        @Query("expires_time") expires_time: Long
    ): ApiResult<Author>

    /**
     * 对一个帖子的喜欢 或 取消喜欢
     * @param itemId 帖子的id
     * @param userId 当前登陆者的id
     */
    @GET("ugc/toggleFeedLike")
    suspend fun toggleFeedLike(
        @Query("itemId") itemId: Long,
        @Query("userId") userId: Long
    ): ApiResult<JsonObject>

    /**
     * 对一个帖子的踩 或取消踩
     * @param itemId 帖子的id
     * @param userId 当前登陆者的id
     */
    @GET("ugc/dissFeed/")
    suspend fun toggleDissFeed(
        @Query("itemId") itemId: Long, @Query("userId") userId: Long
    ): ApiResult<JsonObject>

    /**
     * 对帖子的评论进行点赞或取消点赞
     * @param commentId 评论的id
     * @param itemId 帖子的id
     * @param userId 当前登陆者的id
     */
    @GET("ugc/toggleCommentLike/")
    suspend fun toggleCommentLike(
        @Query("commentId") commentId: Long,
        @Query("itemId") itemId: Long,
        @Query("userId") userId: Long
    ): ApiResult<JsonObject>

}