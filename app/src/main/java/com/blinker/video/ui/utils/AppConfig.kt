package com.blinker.video.ui.utils

import android.content.Context
import com.blinker.video.ui.navigation.BottomBar
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author jiangshiyu
 * @date 2024/12/11
 */
object AppConfig {

    private var sBottomBar: BottomBar? = null

    fun getBottomConfig(context: Context, fileName: String = "main_tabs_config.json"): BottomBar {
        if (sBottomBar == null) {
            val content = parseFile(context, fileName)
            sBottomBar = SerializableHolder.toBean(content)
        }
        return sBottomBar!!
    }

    private fun parseFile(context: Context, fileName: String): String {
        val assets = context.assets
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