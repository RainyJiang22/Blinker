package com.blinker.video.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.scale
import kotlinx.coroutines.async

object VideoUtil {
    const val TAG = "VideoUtil"
    const val MIN_SHOOT_DURATION = 500L // 最小剪辑时间2s
    val DEFAULT_SEEKBAR_PADDING = 33.5f.dp.toInt()
    const val MAX_COUNT_RANGE = 10 //seekBar的区域内一共有多少张图片
    const val VIDEO_MAX_TIME = 20
    const val MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L //视频最多剪切多长时间20s

    fun getBaseVideoCropFile(context: Context, tag: String? = null): File {
        return File(context.filesDir.path + File.separator + "video" + File.separator + "crop_$tag.mp4")

    }

//    val BASE_IMAGE_CROP_FILE =
//        File((requireNotNull(AppApplication.getApplication()).filesDir).path + File.separator + "airPod" + File.separator + "crop.png")
//
//    val BASE_IMAGE_CROP_CACHE_FILE =
//        File((requireNotNull(AppApplication.getApplication()).filesDir).path + File.separator + "airPod" + File.separator + "crop_cache.png")

    suspend fun decodeFrameBitmap(
        scope: CoroutineScope,
        inputFile: File,
        outPutFile: File,
        outPutW: Int,
        outPutH: Int,
        radius: Float,
        frameDuration: Long,
        onError: suspend () -> Unit,
        onSuccess: suspend () -> Unit,
    ) {
        runCatching {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(inputFile.path)
            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0L
            val frameCount = (duration * 1f / frameDuration).toInt()
            val fps = (1000 / frameDuration).toInt()
            val mission = ArrayList<Deferred<Unit?>>()
//            for (i in 0 until frameCount) {
//                val job = scope.async {
//                    val frameTime = frameDuration * i
//                    val bitmap = kotlin.runCatching {
//                        synchronized(mediaMetadataRetriever){
//                           mediaMetadataRetriever.getFrameAtTime(
//                                frameTime * 1000,
//                                MediaMetadataRetriever.OPTION_CLOSEST
//                            )
//                        }
//                    }.onFailure {
//                        it.printStackTrace()
//                    }.getOrNull() ?: return@async
//                    kotlin.runCatching {
//                        val bitmapFile =
//                            File(outPutFile, "frame_${i}.png").outputStream().buffered()
//                        val scaleBitmap = bitmap.alphaAndCorner(1f, outPutW, outPutH, radius)
//                        scaleBitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapFile)
//                        bitmapFile.close()
//                        bitmap.recycle()
//                        scaleBitmap.recycle()
//                    }.onFailure {
//                        it.printStackTrace()
//                    }
//                }
//                mission.add(job)
//            }
//
//            mission.forEach {
//                it.await()
//            }
//            mediaMetadataRetriever.release()

            val session = withContext(Dispatchers.IO) {
                val outPutStr = outPutFile.path + File.separator + "frame_%d.png"
                val cmd =
                    "-i ${inputFile.path} -vf fps=fps=${fps} -s ${outPutW}x${outPutH} ${outPutStr}"
                FFmpegKit.execute(cmd)
            }
            val outPutFiles = outPutFile.listFiles()
            if (outPutFiles.isNullOrEmpty() || session.returnCode.isValueError) {
                onError.invoke()
                return@runCatching
            }
            val fileCount = outPutFiles.size
            for (i in 0 until fileCount) {
                val job = scope.async(Dispatchers.IO) {
                    val file = outPutFiles[i]
                    var bitmap: Bitmap? = null
                    try {
                        bitmap =
                            BitmapFactory.decodeFile(file.path).alphaAndCorner(1f, outPutW, outPutH)
                        val fileInputStream = file.outputStream().buffered()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileInputStream)
                        fileInputStream.close()
                        bitmap.recycle()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } catch (o: OutOfMemoryError) {
                        o.printStackTrace()
                        System.gc()
                    } finally {

                    }
                }
                mission.add(job)
            }
            mission.forEach { it.await() }
            withContext(Dispatchers.Main.immediate) {
                onSuccess.invoke()
            }
        }.onFailure {
            it.printStackTrace()
            withContext(Dispatchers.Main.immediate) {
                onError.invoke()
            }
        }
    }


    suspend fun cropVideo(
        context: Context,
        inputFile: File?,
        outPutFile: File,
        startPosition: Long,
        endPosition: Long,
        onError: () -> Unit, onSuccess: (outPutPath:String?) -> Unit,
    ) {
        if (inputFile == null) {
            onError.invoke()
            return
        }
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(outPutFile.parent ?: "")
                if (dir.exists().not()) {
                    dir.mkdirs()
                }
                if (outPutFile.exists()) {
                    outPutFile.delete()
                }
                //outPutFile.createNewFile()
                val start = convertSecondsToMilliTime(startPosition)
                val duration = convertSecondsToMilliTime((endPosition - startPosition))

                val cmd =
                    "-ss $start -t $duration -accurate_seek -i ${inputFile.path} " + "-c:v copy " + "-vsync vfr ${outPutFile.path}"
                FFmpegKit.executeAsync(
                    cmd
                ) { session ->
                    Log.e(TAG, "${session.allLogsAsString}")
                    if (session.returnCode.isValueSuccess) {
                        saveVideoToGallery(
                            context, outPutFile.absolutePath, outPutFile.nameWithoutExtension
                        )

                        onSuccess.invoke(outPutFile.absolutePath)
                    } else if (session.returnCode.isValueError || session.returnCode.isValueCancel) {
                        onError.invoke()
                    }
                }

            }.onFailure {
                it.printStackTrace()
            }


        }
    }

    fun convertSecondsToMilliTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val millis = milliseconds % 1000

        return String.format(
            "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis
        )
    }

    suspend fun getFrameBitmap(
        inputFile: File,
        thumbCount: Int,
        startPosition: Long,
        endPosition: Long,
        outputWidth: Int,
        outputHeight: Int,
        callback: (bitmap: Bitmap, interval: Long) -> Unit,
    ) {
        runCatching {
            withContext(Dispatchers.IO) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(inputFile.path)
                val interval = (endPosition - startPosition) / (thumbCount - 1f)
                for (i in 0 until thumbCount) {
                    val frameTime = (startPosition + interval * i).toLong()
                    val bitmap = synchronized(mediaMetadataRetriever) {
                        runCatching {
                            mediaMetadataRetriever.getFrameAtTime(
                                frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                        }.onFailure {
                            it.printStackTrace()
                        }.getOrNull()
                    } ?: return@withContext
                    try {
                        val scaleBitmap = bitmap.scale(outputWidth, outputHeight)
                        withContext(Dispatchers.Main.immediate) {
                            callback.invoke(scaleBitmap, interval.toLong())
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    } finally {
                        bitmap.recycle()
                    }
                }
                mediaMetadataRetriever.release()
            }
        }.onFailure {
            it.printStackTrace()
        }
    }


    fun convertSecondsToTime(seconds: Long): String {
        var timeStr: String? = null
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) return "00:00" else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr = unitFormat(minute) + ":" + unitFormat(
                    second
                )
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr = unitFormat(hour) + ":" + unitFormat(
                    minute
                ) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }


    fun convertSecondsToFormat(seconds: Long, format: String?): String? {
        if (TextUtils.isEmpty(format)) return ""
        val date = Date(seconds)
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }

    private fun unitFormat(i: Int): String {
        var retStr: String? = null
        retStr = if (i in 0..9) "0$i" else "" + i
        return retStr
    }

    private val paint by lazy {
        Paint().apply { isAntiAlias = true }
    }


    /**
     * 保存视频到相册
     * @param videoPath 源视频路径（如：/storage/emulated/0/Download/video.mp4）
     * @param displayName 相册中显示的文件名（可选）
     * @return 保存成功返回true，失败返回false
     */
    fun saveVideoToGallery(
        context: Context,
        videoPath: String,
        displayName: String? = null,
    ): Boolean {
        val sourceFile = File(videoPath)
        if (!sourceFile.exists()) {
            Log.e(TAG, "源视频文件不存在: $videoPath")
            return false
        }

        // 生成文件名（如果未提供）
        val fileName = displayName ?: sourceFile.name

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore API
                saveVideoWithMediaStoreApi(context, fileName, sourceFile)
            } else {
                // Android 9- 使用传统文件系统
                saveVideoWithLegacyMethod(context, fileName, sourceFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存视频失败", e)
            false
        }
    }

    fun getVideoSize(videoPath: String): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        try {
            // 设置数据源
            retriever.setDataSource(videoPath)

            // 获取视频宽度
            val width =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                    ?: 0

            // 获取视频高度
            val height =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                    ?: 0
            // 获取旋转角度
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0

            // 如果旋转 90° 或 270°，交换宽高
            val effectiveWidth = if (rotation == 90 || rotation == 270) height else width
            val effectiveHeight = if (rotation == 90 || rotation == 270) width else height

            return Pair(effectiveWidth, effectiveHeight)
        } finally {
            // 释放资源
            retriever.release()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveVideoWithLegacyMethod(
        context: Context,
        fileName: String,
        sourceFile: File,
        result: ((outputPath: String) -> Unit)? = null,
    ): Boolean {
        // 1. 创建目标文件
        val galleryDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val targetFile = File(galleryDir, fileName)

        // 2. 复制文件
        sourceFile.copyTo(targetFile, overwrite = true)
        // 3. 通知媒体库更新
        //MediaLibraryUpdater.updateMediaLibrary(context, targetFile.absolutePath)
//        MediaScannerConnection.scanFile(
//            context,
//            arrayOf(targetFile.absolutePath),
//            arrayOf("video/mp4"),
//            null
//        )
        result?.invoke(targetFile.absolutePath)
        Log.d(TAG, "视频保存成功: ${targetFile.absolutePath}")
        return true
    }

    private fun saveVideoWithMediaStoreApi(
        context: Context,
        fileName: String,
        sourceFile: File,
    ): Boolean {
        // 1. 创建ContentValues
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        // 2. 插入MediaStore
        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("无法创建媒体项")

        // 3. 写入文件内容
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("无法打开输出流")

        // 4. 更新状态
        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        contentResolver.update(uri, contentValues, null, null)

        Log.d(TAG, "视频保存成功: $uri")
        return true
    }

}