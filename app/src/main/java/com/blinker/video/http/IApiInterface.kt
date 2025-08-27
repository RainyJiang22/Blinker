package com.blinker.video.http

import com.blinker.video.model.Author
import com.blinker.video.model.Feed
import com.blinker.video.model.TagList
import com.blinker.video.model.TopComment
import com.google.gson.JsonObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
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


    /**
     * 发布一条帖子
     * @param coverUrl 视频封面图的http-url，如果发布的是视频帖子，则该参数必填
     * @param fileUrl 图片或视频文件的http-url,如果发布的是视频或图片帖子，则该参数必填
     * @param fileWidth 图片或视频文件的原始宽
     * @param fileHeight 图片或视频文件的原始高
     * @param tagId 选择的标签的tagId
     * @param tagTitle 选择的标签的title
     * @param tagTitle 发布的帖子的文本
     */
    @FormUrlEncoded
    @POST("feeds/publish")
    suspend fun publishFeed(
        @Field("coverUrl") coverUrl: String? = null,
        @Field("fileUrl") fileUrl: String? = null,
        @Field("fileWidth") fileWidth: Int = 0,
        @Field("fileHeight") fileHeight: Int = 0,
        @Field("tagId") tagId: Long,
        @Field("tagTitle") tagTitle: String,
        @Field("feedText") feedText: String,
        @Field("userId") userId: Long
    ): ApiResult<JsonObject>


    /**
     * 发布帖子时用于查询可用的标签集合
     * @param userId 当前登陆者的id
     * @param tagId 分页查询才需要，默认0即可
     */
    @GET("tag/queryTagList")
    suspend fun getTagList(
        @Query("userId") userId: Long,
        @Query("tagId") tagId: Long = 0
    ): ApiResult<List<TagList>>

    /**
     * 查询帖子的评论
     */
    @GET("comment/queryFeedComments")
    suspend fun getFeedCommentList(
        @Query("userId") userId: Long,
        @Query("itemId") itemId: Long,
        @Query("commentId") commentId: Long = 0
    ): ApiResult<List<TopComment>>

    /**
     * 发布一条评论,  其中videoUrl、imageUrl非必填
     */
    @POST("comment/addComment")
    @FormUrlEncoded
    suspend fun addComment(
        @Field("userId") userId: Long,
        @Field("itemId") itemId: Long,
        @Field("commentText") commentText: String,
        @Field("video_url") videoUrl: String? = null,
        @Field("image_url") imageUrl: String? = null,
        @Field("width") width: Int = 0,
        @Field("height") height: Int = 0
    ): ApiResult<TopComment>


    /**
     * 收藏or 取消收藏帖子
     */
    @GET("ugc/toggleFavorite")
    suspend fun toggleFeedFavorite(
        @Query("userId") userId: Long, @Query("itemId") itemId: Long
    ): ApiResult<JsonObject>

}