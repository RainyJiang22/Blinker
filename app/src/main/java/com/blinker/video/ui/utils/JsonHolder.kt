package com.blinker.video.ui.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * @author jiangshiyu
 * @date 2022/8/3
 */

/**
 * 简化序列化和反序列化操作的库，底层依赖 Gson 来实现具体的序列化和反序列化逻辑，
 * 对外向使用者暴露简单易用的 API
 */

val SerializableHolder: ISerializableHolder = GsonSerializableHolder()

object TypeTokenHolder {
    inline fun <reified T> type(): Type {
        return object : TypeToken<T>() {}.type
    }
}


sealed class ISerializableHolder {
    companion object {

        fun <T> getOrNull(block: () -> T): T? {
            try {
                return block()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        fun <T> getOrDefault(block: () -> T, defaultValue: T): T {
            try {
                return block() ?: defaultValue
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return defaultValue
        }
    }


    abstract fun toJson(ob: Any?): String

    /**
     * 获取已经格式化好的json
     */
    abstract fun toJsonPretty(ob: Any?): String

    abstract fun <T> toBean(json: String?, clazz: Class<T>): T

    abstract fun <T> toBean(json: String?, type: Type): T

    fun <T> toBeanOrNull(json: String?, clazz: Class<T>): T? {
        return getOrNull { toBean(json, clazz) }
    }

    fun <T> toBeanOrNull(json: String?, type: Type): T? {
        return getOrNull { toBean<T>(json, type) }
    }

    fun <T> toBeanOrDefault(json: String?, clazz: Class<T>, defaultValue: T): T {
        return getOrDefault(block = { toBean(json, clazz) }, defaultValue = defaultValue)
    }

    fun <T> toBeanOrDefault(json: String?, type: Type, defaultValue: T): T {
        return getOrDefault(block = { toBean(json, type) }, defaultValue = defaultValue)
    }

    inline fun <reified T> toBean(json: String?): T {
        return toBean(json, TypeTokenHolder.type<T>())
    }

    inline fun <reified T> toBeanOrNull(json: String?): T? {
        return getOrNull { toBean<T>(json) }
    }

    inline fun <reified T> toBeanOrDefault(json: String?, defaultValue: T): T {
        return getOrDefault(block = { toBean(json) }, defaultValue = defaultValue)
    }

}

private class GsonSerializableHolder : ISerializableHolder() {

    private val gson = Gson()

    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    override fun toJson(ob: Any?): String {
        try {
            return gson.toJson(ob)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ""
    }

    override fun toJsonPretty(ob: Any?): String {
        if (ob == null) {
            return ""
        }
        try {
            val toJson = prettyGson.toJson(ob)
            val jsonParser = JsonParser.parseString(toJson)
            return when {
                toJson.startsWith("{") -> {
                    prettyGson.toJson(jsonParser.asJsonObject)
                }
                toJson.startsWith("[") -> {
                    prettyGson.toJson(jsonParser.asJsonArray)
                }
                else -> {
                    ob.toString()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ob.toString()
    }

    override fun <T> toBean(json: String?, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    override fun <T> toBean(json: String?, type: Type): T {
        return gson.fromJson(json, type)
    }

}











