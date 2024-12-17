package com.blinker.video.http

import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author jiangshiyu
 * @date 2024/12/16
 */
class GsonResponseBodyConverter<T> constructor(private val gson: Gson, private val type: Type) :
    Converter<ResponseBody, ApiResult<T>> {
    override fun convert(value: ResponseBody): ApiResult<T> {
        value.use {
            if (type !is ParameterizedType || !ApiResult::class.java.isAssignableFrom((type.rawType) as Class<*>))
                throw java.lang.RuntimeException("The return type of the method must be ApiResult<*>")
            val apiResult = ApiResult<T>()
            val response = JSONObject(value.string())
            apiResult.status = response.optInt("status")
            apiResult.errMsg = response.optString("message")
            val data1: JSONObject? = response.optJSONObject("data")
            if (data1 != null) {
                val data2: String? = data1.optString("data")
                if (data2 != null) {
                    val argumentType = type.actualTypeArguments[0]
                    kotlin.runCatching {
                        apiResult.body = gson.fromJson(data2, argumentType)
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
            return apiResult
        }
    }
}