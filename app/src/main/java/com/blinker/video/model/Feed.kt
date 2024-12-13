package com.blinker.video.model

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */
class Feed {
    val TYPE_IMAGE_TEXT = 1 //图文
    val TYPE_VIDEO = 2 //视频

    /**
     * id : 364
     * itemId : 6739143063064549000
     * itemType : 2
     * createTime : 1569079017
     * duration : 299.435
     * feeds_text : 当中国地图出来那一幕，我眼泪都出来了！
     * 太震撼了！
     * authorId : 3223400206308231
     * activityIcon : null
     * activityText : null
     * width : 640
     * height : 368
     * url : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/6739143063064549643.mp4
     * cover :
     */
    var id = 0
    var itemId: Long = 0
    var itemType = 0
    var createTime: Long = 0
    var duration = 0.0
    var feeds_text: String? = null
    var authorId: Long = 0
    var activityIcon: String? = null
    var activityText: String? = null
    var width = 0
    var height = 0
    var url: String? = null
    var cover: String? = null
    var author: User? = null
    var topComment: Comment? = null
    var ugc: Ugc? = null
}