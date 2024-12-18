package com.blinker.video.exoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutListWrapperPlayerViewBinding
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.utils.setBlurImageUrl
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setVisibility
import com.google.android.exoplayer2.Player

/**
 * @author jiangshiyu
 * @date 2024/12/18
 *
 * 动态挂载 视频播放控制器，显示视频画面的playerView
 */
class WrapperPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs) {
    private var callback: Listener? = null
    private val viewBinding =
        LayoutListWrapperPlayerViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        viewBinding.playBtn.setOnClickListener {
            callback?.onTogglePlay(this)
        }
    }


    fun bindData(widthPx: Int, heightPx: Int, coverUrl: String?, videoUrl: String, maxHeight: Int) {
        // 1、根据视频的widthPx,heightPx 动态计算出cover、 blur 以及wrapperView的宽高

        viewBinding.cover.setImageUrl(coverUrl)

        if (widthPx < heightPx) {
            coverUrl?.run {
                viewBinding.blurBackground.setBlurImageUrl(this, 10)
                viewBinding.blurBackground.setVisibility(true)
            }
        } else {
            viewBinding.blurBackground.setVisibility(false)
        }

        setSize(widthPx, heightPx, PixUtil.getScreenWidth(), maxHeight)
    }

    private fun setSize(widthPx: Int, heightPx: Int, maxWidth: Int, maxHeight: Int) {
        // 这里要求做的事情 是 计算视频原始宽度>原始高度 /  原石高度>原石宽高时  cover、wrapperView等比缩放
        val coverWidth: Int
        val coverHeight: Int
        if (widthPx >= heightPx) {
            coverWidth = maxWidth
            coverHeight = (heightPx / (widthPx * 1.0f / maxWidth)).toInt()
        } else {
            coverHeight = maxHeight
            coverWidth = (widthPx / (heightPx * 1.0f / maxHeight)).toInt()
        }

        // 设置wrapper-view的宽高
        val wrapperViewParams = layoutParams
        wrapperViewParams.width = maxWidth
        wrapperViewParams.height = coverHeight
        layoutParams = wrapperViewParams

        // 设置高斯模糊背景view的宽高
        val blurParams = viewBinding.blurBackground.layoutParams
        blurParams.width = maxWidth
        blurParams.height = coverHeight
        viewBinding.blurBackground.layoutParams = blurParams

        // 设置cover-view封面图的宽高
        val coverParams: LayoutParams = viewBinding.cover.layoutParams as LayoutParams
        coverParams.width = coverWidth
        coverParams.height = coverHeight
        coverParams.gravity = Gravity.CENTER
        viewBinding.cover.scaleType = ImageView.ScaleType.FIT_CENTER
        viewBinding.cover.layoutParams = coverParams
    }

    fun onActive(playerView: View, controllerView: View) {
        val parent = playerView.parent
        if (parent != this) {
            if (parent != null) {
                (parent as ViewGroup).removeView(playerView)
            }
            val coverParams = viewBinding.cover.layoutParams
            this.addView(playerView, 1, coverParams)
        }

        val ctrlParent = controllerView.parent
        if (ctrlParent != this) {
            if (ctrlParent != null) {
                (ctrlParent as ViewGroup).removeView(controllerView)
            }
            val ctrlParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            ctrlParams.gravity = Gravity.BOTTOM
            this.addView(controllerView, ctrlParams)
        }
    }

    fun inActive() {
        viewBinding.cover.setVisibility(true)
        viewBinding.playBtn.setVisibility(true)
        viewBinding.playBtn.setImageResource(R.drawable.icon_video_play)
    }

    fun onPlayerStateChanged(playing: Boolean, playbackState: Int) {
        if (playing) {
            viewBinding.cover.setVisibility(false)
            viewBinding.bufferView.setVisibility(false)
            viewBinding.playBtn.setVisibility(true)
            viewBinding.playBtn.setImageResource(R.drawable.icon_video_pause)
        } else if (playbackState == Player.STATE_ENDED) {
            viewBinding.cover.setVisibility(true)
            viewBinding.playBtn.setVisibility(true)
            viewBinding.playBtn.setImageResource(R.drawable.icon_video_play)
        } else if (playbackState == Player.STATE_BUFFERING) {
            viewBinding.bufferView.setVisibility(true)
        }
    }

    fun onControllerVisibilityChange(visibility: Int, playEnd: Boolean) {
        viewBinding.playBtn.setVisibility(if (playEnd) true else visibility == View.VISIBLE)
    }

    fun setListener(callback: Listener) {
        this.callback = callback
    }

    interface Listener {
        fun onTogglePlay(attachView: WrapperPlayerView)
    }
}