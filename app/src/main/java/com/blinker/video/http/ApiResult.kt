package com.blinker.video.http

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */
class ApiResult<T> {

    internal var status = 0
    val success
        get() = status == 200
    var errMsg: String = ""
    var body: T? = null
}