@file:Suppress("DEPRECATION")

package com.blinker.video.exoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import com.blinker.video.R
import com.blinker.video.ui.utils.AppGlobals
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import androidx.core.net.toUri
import com.google.android.exoplayer2.upstream.DataSpec
import java.io.File

/**
 * @author jiangshiyu
 * @date 2024/12/18
 */
@SuppressLint("InflateParams")
class PageListPlayer : IListPlayer, Player.Listener, StyledPlayerControlView.VisibilityListener {
    private var exoPlayer: ExoPlayer
    private var exoPlayerView: StyledPlayerView
    private var exoControllerView: StyledPlayerControlView

    private var playingUrl: String? = null
    private var playing: Boolean = false

    init {
        val application = AppGlobals.getApplication()
        exoPlayer = ExoPlayer.Builder(application).build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

        exoPlayerView = LayoutInflater.from(application).inflate(
            R.layout.layout_exo_player_view, null
        ) as StyledPlayerView
        exoControllerView = LayoutInflater.from(application).inflate(
            R.layout.layout_exo_player_controller_view, null
        ) as StyledPlayerControlView


        // 把播放器实例 和 playerView 和 controllerView 相关联
        // 如此视频画面才会正常显示出来，播放进度条和时间才能自动更新
        exoPlayerView.player = exoPlayer
        exoControllerView.player = exoPlayer
    }

    override var attachedView: WrapperPlayerView? = null
    override val isPlaying: Boolean
        get() = playing

    override fun inActive() {
        if (TextUtils.isEmpty(playingUrl) || attachedView == null) {
            return
        }
        exoPlayer.playWhenReady = false
        exoPlayer.removeListener(this)
        exoControllerView.removeVisibilityListener(this)
        attachedView?.inActive()
    }

    override fun onActive() {
        if (TextUtils.isEmpty(playingUrl) || attachedView == null) {
            return
        }
        exoPlayer.playWhenReady = true
        exoPlayer.addListener(this)
        exoControllerView.addVisibilityListener(this)
        exoControllerView.show()
        attachedView?.onActive(exoPlayerView, exoControllerView)
        if (exoPlayer.playbackState == Player.STATE_READY) {
            onPlayerStateChanged(true, Player.STATE_READY)
        } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun togglePlay(attachView: WrapperPlayerView, videoUrl: String) {
        attachedView?.setOnTouchListener(null)
        attachView.setOnTouchListener { _, _ ->
            exoControllerView.show()
            true
        }
        if (TextUtils.equals(videoUrl, playingUrl)) {
            // 意味着是 点击了正在播放的item  暂停或继续播放按钮
            if (playing) {
                inActive()
            } else {
                this.attachedView = attachView
                onActive()
            }
        } else {
            inActive()
            this.playingUrl = videoUrl
            this.attachedView = attachView

            exoPlayer.setMediaSource(createMediaSource(videoUrl))
            exoPlayer.prepare()

            onActive()
        }
    }

    override fun stop(release: Boolean) {
        playing = false
        playingUrl = null
        exoPlayer.playWhenReady = false
        exoControllerView.hideImmediately()
        attachedView?.removeView(exoPlayerView)
        attachedView?.removeView(exoControllerView)
        attachedView = null

        if (release) {
            exoPlayer.release()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        playing = playbackState == Player.STATE_READY && playWhenReady
        attachedView?.onPlayerStateChanged(isPlaying, playbackState)
        Log.e("PageListPlay", "onPlayerStateChanged: $playbackState")
    }

    override fun onVisibilityChange(visibility: Int) {
        attachedView?.onControllerVisibilityChange(
            visibility,
            exoPlayer.playbackState == Player.STATE_ENDED
        )
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        exoPlayer.playWhenReady = true
    }

    companion object {
        private val application = AppGlobals.getApplication()
        private val cache = SimpleCache(
            application.cacheDir,
            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200),
            StandaloneDatabaseProvider(
                application
            )
        )
        private val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory().setCache(cache).setFragmentSize(Long.MAX_VALUE)
            )
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)

        private val progressiveMediaSourceFactory =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)

        private val sPageListPlayers = hashMapOf<String, IListPlayer>()
        fun get(pageName: String): IListPlayer {
            var pageListPlayer = sPageListPlayers[pageName]
            if (pageListPlayer == null) {
                pageListPlayer = PageListPlayer()
                sPageListPlayers[pageName] = pageListPlayer
            }

            return pageListPlayer
        }

        fun stop(pageName: String, release: Boolean = true) {
            if (release) {
                sPageListPlayers.remove(pageName)?.stop(true)
            } else {
                sPageListPlayers[pageName]?.stop(false)
            }
        }

        fun createMediaSource(videoUrl: String): MediaSource {
            val file = File(videoUrl)
            // 如果是本地视频文件，则重新创建一个ProgressiveMediaSource，
            // 并且dataSourceFactory指定为FileDataSource.factory,才能正常的从本地文件播放视频
            if (file.exists()) {
                val dataSpec = DataSpec(Uri.fromFile(file))
                val fileDataSource = FileDataSource()
                fileDataSource.open(dataSpec)
                val uri = fileDataSource.uri
                val factory = ProgressiveMediaSource.Factory(FileDataSource.Factory())
                return factory.createMediaSource(MediaItem.fromUri(uri!!))
            } else {
                return progressiveMediaSourceFactory.createMediaSource(
                    MediaItem.fromUri(videoUrl.toUri())
                )
            }
        }
    }
}