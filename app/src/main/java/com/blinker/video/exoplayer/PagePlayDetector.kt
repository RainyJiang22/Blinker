package com.blinker.video.exoplayer

import android.view.ViewGroup
import androidx.collection.objectListOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

/**
 * @author jiangshiyu
 * @date 2024/12/18
 */
class PagePlayDetector(
    private val pageName: String,
    private val lifecycleOwner: LifecycleOwner,
    private val listView: RecyclerView
) {

    private val mDetectorListeners: MutableList<IPlayDetector> = arrayListOf()
    private val pageListPlayer = PageListPlayer.get(pageName)

    fun addDetector(detector: IPlayDetector) {
        mDetectorListeners.add(detector)
    }

    fun removeDetector(detector: IPlayDetector) {
        mDetectorListeners.remove(detector)
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dx == 0 && dy == 0) {
                // 意味着列表初始数据加载成功,当调用了notifyItemRangeInsert之后，item 并没有立即被添加到列表上
                // 等itemView 被真正布局到recyclerview之后，会触发onScrolled
                postAutoPlay()
            } else {
                // 滑动中需要检测，正在播放的item 是否已经滑出屏幕，如果滑出则停止它
                if (pageListPlayer.isPlaying && !isTargetInBounds(pageListPlayer.attachedView)) {
                    pageListPlayer.stop(false)
                }
            }
        }
    }

    init {
        listView.addOnScrollListener(scrollListener)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> pageListPlayer.inActive()
                    Lifecycle.Event.ON_RESUME -> pageListPlayer.onActive()
                    Lifecycle.Event.ON_DESTROY -> {
                        mDetectorListeners.clear()
                        listView.removeOnScrollListener(scrollListener)
                        listView.removeCallbacks(delayAutoPlayRunnable)
                        pageListPlayer.stop(false)
                    }
                    else -> {}
                }
            }
        })
    }

    private val delayAutoPlayRunnable = Runnable { autoPlay() }
    private fun postAutoPlay() {
        listView.post(delayAutoPlayRunnable)
    }

    /**
     * 检测itemView的视频播放器容器viewGroup是否有至少1/2的高度在屏幕可视区域内
     */
    private fun isTargetInBounds(attachedView: ViewGroup?): Boolean {
        if (attachedView == null) {
            return false
        }

        if (!attachedView.isShown || !attachedView.isAttachedToWindow) {
            return false
        }
        val location = IntArray(2)
        attachedView.getLocationOnScreen(location)
        val center = location[1] + attachedView.height / 2

        ensureRecyclerViewLocation()

        return rvLocation?.run {
            center in first..second
        } ?: false
    }

    private var rvLocation: Pair<Int, Int>? = null
    private fun ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            val location = IntArray(2)
            listView.getLocationOnScreen(location)
            rvLocation = Pair(location[1], location[1] + listView.height)
        }
    }

    private fun autoPlay() {
        if (mDetectorListeners.isEmpty() || listView.childCount <= 0) {
            return
        }

        // 是否有正在播放的 item, 并且还在屏幕内
        if (pageListPlayer.isPlaying && isTargetInBounds(pageListPlayer.attachedView)) {
            return
        }

        var attachedViewListener: IPlayDetector? = null
        for (listener in mDetectorListeners) {
            val inBounds = isTargetInBounds(listener.getAttachView())
            if (inBounds) {
                attachedViewListener = listener
                break
            }
        }
        attachedViewListener?.run {
            togglePlay(this.getAttachView(), this.getVideoUrl())
        }
    }

    fun togglePlay(attachView: WrapperPlayerView, videoUrl: String) {
        pageListPlayer.togglePlay(attachView, videoUrl)
    }

    interface IPlayDetector {
        fun getAttachView(): WrapperPlayerView
        fun getVideoUrl(): String
    }
}