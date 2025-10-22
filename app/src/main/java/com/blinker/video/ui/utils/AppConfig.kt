package com.blinker.video.ui.utils

import com.blinker.video.model.BottomBar
import com.blinker.video.model.Category
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author jiangshiyu
 * @date 2024/12/11
 */
object AppConfig {

    /**
     * QQ 应用 ID
     */
    const val QQ_APP_ID = "1108832891"

    private var sBottomBar: BottomBar? = null
    private var sCategory: Category? = null

    fun getBottomConfig(fileName: String = "main_tabs_config.json"): BottomBar {
        if (sBottomBar == null) {
            val content = parseFile(fileName)
            sBottomBar = SerializableHolder.toBean(content)
        }
        return sBottomBar!!
    }

    fun getCategory(): Category {
        if (sCategory == null) {
            val content: String = parseFile("category_tabs_config.json")
            sCategory = Gson().fromJson(content, Category::class.java)
        }
        return sCategory!!
    }

    fun parseFile(fileName: String): String {
        val assets = AppGlobals.getApplication().assets
        var inputStream: InputStream? = null
        var br: BufferedReader? = null
        val builder = StringBuilder()
        try {
            inputStream = assets.open(fileName)
            br = BufferedReader(InputStreamReader(inputStream))
            var line: String? = null
            while (br.readLine().also { line = it } != null) {
                builder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                br?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return builder.toString()
    }

}