package com.blinker.video.model

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */
data class Ugc(
    /**
     * likeCount : 153
     * shareCount : 0
     * commentCount : 4454
     * hasFavorite : false
     * hasLiked : true
     * hasdiss:false
     */
    var likeCount: Int = 0,
    var shareCount: Int = 0,
    var commentCount: Int = 0,
    var hasFavorite: Boolean = false,
    var hasdiss: Boolean = false,
    var hasLiked: Boolean = false
)