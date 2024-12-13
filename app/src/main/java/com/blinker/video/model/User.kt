package com.blinker.video.model

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */
data class User(
    /**
     * id : 962
     * userId : 3223400206308231
     * name : 二师弟请随我来
     * avatar :
     * description :
     * likeCount : 0
     * topCommentCount : 0
     * followCount : 0
     * followerCount : 0
     * qqOpenId : null
     * expires_time : 0
     * score : 0
     * historyCount : 0
     * commentCount : 0
     * favoriteCount : 0
     * feedCount : 0
     * hasFollow : false
     */
    var id: Int = 0,
    var userId: Long = 0,
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var likeCount: Int = 0,
    var topCommentCount: Int = 0,
    var followCount: Int = 0,
    var followerCount: Int = 0,
    var qqOpenId: String? = null,
    var expires_time: Long = 0,
    var score: Int = 0,
    var historyCount: Int = 0,
    var commentCount: Int = 0,
    var favoriteCount: Int = 0,
    var feedCount: Int = 0,
    var hasFollow: Boolean = false
)