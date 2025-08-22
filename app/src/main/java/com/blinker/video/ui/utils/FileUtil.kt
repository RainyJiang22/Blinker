package com.blinker.video.ui.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
internal object FileUtil {

    fun generateVideoCoverFile(videoFilePath: String): String? {
        val retrieve = MediaMetadataRetriever()
        retrieve.setDataSource(videoFilePath)
        val bitmap = retrieve.frameAtTime
        if (bitmap != null) {
            val bytes = compressBitmap(bitmap, 200)
            val file = File(
                AppGlobals.getApplication().cacheDir,
                System.currentTimeMillis().toString() + ".jpeg"
            )
            try {
                file.createNewFile()
                val fos = FileOutputStream(file)
                fos.write(bytes)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return file.absolutePath
        }

        return null
    }

    private fun compressBitmap(bitmap: Bitmap, limit: Int): ByteArray? {
        if (limit > 0) {
            val baos = ByteArrayOutputStream()
            var options = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            while (baos.toByteArray().size > limit * 1024) {
                baos.reset()
                options -= 5
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            }
            val bytes = baos.toByteArray()
            try {
                baos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bytes
        }
        return null
    }
}