package com.blinker.video.model

/**
 * @author jiangshiyu
 * @date 2024/12/11
 */
data class BottomBar(

    val activeColor:String,

    val inActiveColor:String,

    val tabs:List<Tab>,
    //底部默认选中
    val selectTab:Int
)

data class Tab(
    var size: Int = 24,
    var enable: Boolean = false,
    var index: Int = 0,
    var route: String? = null,
    var title: String? = null,
)