package com.blinker.video.ui.pages.publish

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import com.blinker.video.R
import com.blinker.video.base.BaseActivity
import com.blinker.video.databinding.TailorActivityVideoCaptureBinding
import com.blinker.video.ui.pages.publish.CaptureActivity.Companion.RESULT_FILE_PATH
import com.blinker.video.ui.utils.VideoUtil
import com.blinker.video.ui.utils.dp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * @author jiangshiyu
 * @date 2025/10/23
 */
class VideoCaptureActivity :
    BaseActivity<TailorActivityVideoCaptureBinding, AndroidViewModel>() {
    companion object {
        private val TAG = "VideoPlayer"

        /**
         * 裁剪视频最小500毫秒
         */
        private const val MIN_LENGTH = 500L
        private const val EXT_MEDIA = "EXT_MEDIA"
        fun start(context: Context, uri: String?) {
            val intent = Intent(context, VideoCaptureActivity::class.java)
            if (uri != null) {
                intent.putExtra(EXT_MEDIA, uri)
            }
            context.startActivity(intent)
        }
    }


    /**
     * 开始、结束播放位置的百分比
     */
    var loopStartPercent = 0f  // 70%
    var loopEndPercent = 1f  // 70%
    var checkInterval = 50L  // 100ms 检查一次
    private val uriPath by lazy { intent.getStringExtra(EXT_MEDIA) }

    /**
     * 保存视频
     */
    private fun saveVideo() {
        if (loopEndPercent - loopStartPercent <= 0) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding?.loading?.show()
            val duration = player?.duration ?: 0L
            if (duration == 0L) {
               binding?.loading?.hide()
                Toast.makeText(
                    this@VideoCaptureActivity,
                    R.string.tailor_cutout_video_fail, Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            player?.pause()

            VideoUtil.cropVideo(
                this@VideoCaptureActivity,
                fileVideoCopy,
                VideoUtil.getBaseVideoCropFile(
                    this@VideoCaptureActivity,
                    fileVideoCopy?.nameWithoutExtension
                ),
                (loopStartPercent * duration).toLong(),
                (loopEndPercent * duration).toLong(),
//                    divX, divY, divW, divH,
                {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding?.loading?.hide()
                        Toast.makeText(
                            this@VideoCaptureActivity,
                            R.string.tailor_cutout_video_fail, Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { path->
                lifecycleScope.launch(Dispatchers.Main) {
                    binding?.loading?.hide()
//                    val dialog = SaveSucDialog()
//                    dialog.show(supportFragmentManager, "SaveSucDialog")
//                    delay(1000L)
//                    dialog.dismiss()
                    val intent = Intent()
                    intent.putExtra(RESULT_FILE_PATH, path)
                    setResult(RESULT_OK, intent)
                    finish()
                }


            }


//            loading.dismiss()
        }


    }

    private var fileVideoCopy: File? = null

    private var player: ExoPlayer? = null


    private fun playVideo(videoUri: Uri) {


        binding?.loading?.show()

        lifecycleScope.launch {
            fileVideoCopy = withContext(Dispatchers.IO) {
                copyVideoToCache(videoUri)
            } ?: return@launch

            // 释放旧播放器
            player?.release()
            // 创建媒体项
            val mediaItem = MediaItem.fromUri(Uri.fromFile(fileVideoCopy))
//            val mediaItem = MediaItem.fromUri(videoUri)

            // 设置 TrackSelector，控制视频质量和编码
            val trackerSelector = DefaultTrackSelector(this@VideoCaptureActivity).apply {
                setParameters(
                    buildUponParameters()
                        .setMaxVideoSizeSd()
                        .setPreferredVideoMimeType(MimeTypes.VIDEO_H264)
                )
            }
            // 配置播放器
            player =
                ExoPlayer.Builder(this@VideoCaptureActivity)
                    .setTrackSelector(trackerSelector)
                    .build()
            player?.setMediaItem(mediaItem)
            player?.volume = 0f
            binding?.playerView?.player = player
            player?.repeatMode = Player.REPEAT_MODE_ALL
//            player?.playWhenReady = true
            player?.removeListener(playListener)
            player?.addListener(playListener)
            player?.prepare()
//            player?.play()
            lifecycleScope.launch {
                while (isActive) {
                    if (player?.playbackState == Player.STATE_READY && player?.isPlaying == true) {
                        val currentPositionMs = player!!.currentPosition
                        val durationMs = player!!.duration

                        if (durationMs > 0) {
                            val progress = currentPositionMs.toFloat() / durationMs

                            if (progress >= loopEndPercent) {
                                // 在 IO 线程中执行 seekTo，避免阻塞 UI
                                seekTo((loopStartPercent * durationMs).toLong())
                            } else if (progress < loopStartPercent) {
                                seekTo((loopStartPercent * durationMs).toLong())
                            } else {
                                binding?.cutoutCtrl?.progress = progress
                            }

                        }
                    }
                    // 挂起协程，释放线程资源
                    delay(checkInterval)
                }
            }
        }
    }

    var isLoadingFrame = false
    private fun getVideoFrame() {
        if (binding?.cutoutCtrl?.bitmapFrame.isNullOrEmpty().not()) {
            binding?.loading?.hide()
            return
        }

        if (isLoadingFrame) {
            binding?.loading?.hide()
            return
        }
        isLoadingFrame = true
        val file = fileVideoCopy
        val duration = player?.duration
        if (file == null || duration == null) {
            binding?.loading?.hide()
            isLoadingFrame = false
            return
        }
        lifecycleScope.launch {


            val videoSize = VideoUtil.getVideoSize(file.absolutePath)

            val w = 60.dp

            val h = w * videoSize.second / videoSize.first


            val bitmaps = ArrayList<Bitmap>()
            withContext(Dispatchers.IO) {
                VideoUtil.getFrameBitmap(
                    file,
                    10,
                    0L,
                    duration,
                    w.toInt(),
                    h.toInt()
                ) { bitmap: Bitmap, interval: Long ->
                    bitmaps.add(bitmap)
                }
            }
            binding?.cutoutCtrl?.bitmapFrame = bitmaps

            isLoadingFrame = false
            binding?.loading?.hide()
        }
    }

    private fun seekTo(msec: Long) {
        kotlin.runCatching {
            player?.seekTo(msec)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private suspend fun copyVideoToCache(uri: Uri): File? {
        return try {
            // 获取文件名
            val fileName = getFileNameFromUri(uri) ?: "video_${System.currentTimeMillis()}.mp4"
            // 创建缓存文件
            val cacheFile = File(cacheDir, fileName)
            // 复制文件
            if (cacheFile.exists().not()) {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 获取文件名
    private fun getFileNameFromUri(uri: Uri): String {
        return "video_${uri.path?.substringAfterLast("/")}.mp4"
    }


    private val playListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            binding?.loading?.hide()
            Log.d(TAG, "onPlayerError = ${error.errorCodeName}")
            error.printStackTrace()

        }

        // 标记是否需要重置到 50% 位置
        private var shouldSeekToHalf = false
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            binding?.ivPlayPause?.setImageResource(
                if (isPlaying) {
                    R.drawable.tailor_ic_video_pause
                } else {
                    R.drawable.tailor_ic_video_play
                }
            )

            // 当播放停止时（用户点击暂停或播放结束）
            if (!isPlaying && player?.playbackState != Player.STATE_IDLE) {
                // 标记下次播放时需要重置到 50%
                shouldSeekToHalf = true
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                        Log.d(TAG, "准备中...")
                }

                Player.STATE_BUFFERING -> {
                        Log.d(TAG, "缓冲中...")
                }

                Player.STATE_READY -> {

                    getVideoFrame()


                    player?.let {
                        if (it.duration > MIN_LENGTH) {
                            binding?.cutoutCtrl?.minLengthP = MIN_LENGTH.toFloat() / it.duration
                        } else {
                            binding?.cutoutCtrl?.minLengthP = 0.5f

                        }

                    }


                    // 如果需要重置位置
//                    if (shouldSeekToHalf) {
//                        val duration = player?.duration?:C.TIME_UNSET
//                        if (duration != C.TIME_UNSET) {
//                            // 计算 50% 位置
//                            val halfPosition = duration / 2
//                            player?.seekTo(halfPosition)
//
//                            // 重置标记
//                            shouldSeekToHalf = false
//                        }
//                    }

                    Log.d(TAG, "准备完成！")
//                        player?.seekTo((6000).toLong())
                    //                    else {
//                        player?.play()
//                    }
                }

                Player.STATE_ENDED -> {
                    Log.d(TAG, "播放结束")
                }
            }
        }
    }

    private var playingOnPause = false

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (playingOnPause &&
            player?.playbackState == Player.STATE_READY
            && player?.playWhenReady?.not() == true
        ) {
            player?.play()
            playingOnPause = false
        }
    }

    override fun onPause() {
        super.onPause()
        playingOnPause = player?.isPlaying == true
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBundle(bundle: Bundle) {
    }

    override fun init(savedInstanceState: Bundle?) {
        if (uriPath == null) {
            finish()
            return
        }

        val uri = if (uriPath!!.startsWith("file://")) {
            Uri.fromFile(File(uriPath))
        } else {
            uriPath!!.toUri()
        }
        playVideo(uri)
        binding?.ivBack?.setOnClickListener { finish() }

        binding?.ivPlayPause?.setOnClickListener {
            if (player == null) return@setOnClickListener
            if (player?.isPlaying == true) {
                player?.pause()
            } else {
                player?.play()
            }

        }


        binding?.cutoutCtrl?.onTouchingListener = {
            binding?.tvTime?.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding?.cutoutCtrl?.onLengthChangeListener = { startP, endP, isScrollStart ->
            loopStartPercent = startP
            loopEndPercent = endP

            player?.let { player ->
                seekTo((loopStartPercent * player.duration).toLong())
                val timeMs =
                    if (isScrollStart) {
                        loopStartPercent
                    } else {
                        loopEndPercent
                    } * player.duration
                binding?.tvTime?.text = VideoUtil.convertSecondsToTime((timeMs / 1000f).toLong())

            }


        }
        binding?.tvSave?.setOnClickListener {
            saveVideo()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        player?.setVideoSurfaceView(null)
        player?.stop()
        player?.release()
        player?.removeListener(playListener)
        binding?.playerView?.player = null

    }

}