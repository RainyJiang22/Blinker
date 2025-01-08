package com.blinker.video.model

/**
 * @author jiangshiyu
 * @date 2025/1/8
 */
class Category {
    /**
     * activeSize : 16
     * normalSize : 14
     * activeColor : #ED7282
     * normalColor : #666666
     * select : 0
     * tabGravity : 0
     * tabs : [{"title":"图片","index":0,"tag":"pics","enable":true},{"title":"视频","index":1,"tag":"video","enable":true},{"title":"文本","index":1,"tag":"text","enable":true}]
     */
    var activeSize = 0
    var normalSize = 0
    var activeColor: String? = null
    var normalColor: String? = null
    var select = 0
    var tabGravity = 0
    var tabs: List<Tab>? = null

    class Tab {
        /**
         * title : 图片
         * index : 0
         * tag : pics
         */
        var title: String? = null
        var index = 0
        var tag: String? = null
    }

}