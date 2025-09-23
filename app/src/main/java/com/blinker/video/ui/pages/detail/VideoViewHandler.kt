package com.blinker.video.ui.pages.detail

import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.FragmentActivity
import com.blinker.video.databinding.LayoutFeedDetailTypeVideoBinding
import com.blinker.video.databinding.LayoutFeedDetailTypeVideoHeaderBinding
import com.blinker.video.exoplayer.IListPlayer
import com.blinker.video.exoplayer.PageListPlayer
import com.blinker.video.exoplayer.WrapperPlayerView
import com.blinker.video.model.Feed
import com.blinker.video.ui.pages.detail.FeedDetailActivity.Companion.KEY_CATEGORY
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.utils.invokeViewBinding

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class VideoViewHandler(context: FragmentActivity) : ViewHandler(context) {

    private val viewBinding: LayoutFeedDetailTypeVideoBinding by invokeViewBinding()

    private val headerBinding: LayoutFeedDetailTypeVideoHeaderBinding by invokeViewBinding()
    private lateinit var player: IListPlayer


    init {
        listView = viewBinding.listView
        bottomInteractionBinding = viewBinding.bottomInteraction
        viewBinding.actionClose.setOnClickListener {
            context.finish()
        }
    }

    override fun bindInitData(feed: Feed) {
        super.bindInitData(feed)
        val category = context.intent.getStringExtra(KEY_CATEGORY) ?: return
        player = PageListPlayer.get(category)

        viewBinding.playerView.bindData(
            feed.width,
            feed.height,
            feed.cover,
            feed.url.toString(),
            PixUtil.dp2px(250)
        )
        viewBinding.playerView.setListener(object : WrapperPlayerView.Listener {
            override fun onTogglePlay(attachView: WrapperPlayerView) {
                player.togglePlay(attachView, feed.url.toString())
            }
        })
        player.togglePlay(viewBinding.playerView, feed.url.toString())
    }

    override fun getHeaderView(): View? {
        bindAuthorInfo(headerBinding.authorInfo, headerBinding.feedText, headerBinding.feedLabel)
        headerBinding.root.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        return headerBinding.root
    }

    override fun onBackPressed() {
        viewBinding.actionClose.performClick()
    }

    override fun onPause() {
        player.inActive()
    }

    override fun onResume() {
        player.onActive()
    }


    override fun getRootView(): View {
        return viewBinding.root
    }
}