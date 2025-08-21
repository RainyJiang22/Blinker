package com.blinker.video.ui.utils

import com.blinker.video.http.ApiResult
import com.blinker.video.model.Feed
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Mock {

    fun hotFeed(): ApiResult<List<Feed>> {
        val gson = Gson()
        return gson.fromJson(AppConfig.parseFile("hot_feeds.json"),
            object : TypeToken<ApiResult<List<Feed>>>() {}.type)
    }

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