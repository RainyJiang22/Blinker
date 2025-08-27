package com.blinker.video.http

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */
class ApiResult<T> {
    internal var nextPageKey: Long? = 0
    internal var status = 0
    val success
        get() = status == 200
    var message: String = ""
    var body: T? = null
}