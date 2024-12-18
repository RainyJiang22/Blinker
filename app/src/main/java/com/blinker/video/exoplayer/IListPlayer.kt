package com.blinker.video.exoplayer

/**
 * @author jiangshiyu
 * @date 2024/12/18
 */
interface IListPlayer {

    /**
     * 获取当前视频播放器的exoPlayerView(textureView)是否已经被挂在到某个item容器上
     */
    val attachedView: WrapperPlayerView?

    /**
     * 是否正在进行视频播放
     */
    val isPlaying: Boolean

    /**
     * 页面不可见时 暂停播放
     */
    fun inActive()

    /**
     * 页面恢复可见时 继续播放
     */
    fun onActive()

    /**
     * 点击播放/暂停
     */
    fun togglePlay(attachView: WrapperPlayerView, videoUrl: String)

    /**
     * 释放视频播放器资源
     */
    fun stop(release: Boolean)
}