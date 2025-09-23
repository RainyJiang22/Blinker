package com.blinker.video.ui.pages.tags

import com.blinker.video.model.Feed
import com.blinker.video.ui.utils.AppConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @author jiangshiyu
 * @date 2025/9/23
 * 模拟数据
 */
object Mock {

    fun feeds(): List<Feed> {
        val gson = Gson()
        return gson.fromJson(AppConfig.parseFile("mock_tags_feeds.json"),
            object : TypeToken<List<Feed>>() {}.type)
    }

    fun feeds2(): List<Feed> {
        val gson = Gson()
        return gson.fromJson(AppConfig.parseFile("mock_user_feeds.json"),
            object : TypeToken<List<Feed>>() {}.type)
    }
}