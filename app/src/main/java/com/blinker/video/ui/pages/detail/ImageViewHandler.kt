package com.blinker.video.ui.pages.detail

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFeedDetailTypeImageBinding
import com.blinker.video.model.Feed
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.utils.bindFeedImage
import com.blinker.video.ui.utils.invokeViewBinding

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class ImageViewHandler(context: FragmentActivity) : ViewHandler(context) {
    private val viewBinding: LayoutFeedDetailTypeImageBinding by invokeViewBinding()

    init {
        listView = viewBinding.listView
    }


    override fun bindInitData(feed: Feed) {
        super.bindInitData(feed)
        viewBinding.feedImage.bindFeedImage(getLifecycleOwner(), feed, PixUtil.dp2px(250))
        bindAuthorInfo(viewBinding.feedAuthor, viewBinding.feedText, viewBinding.feedLabel)
    }

    override fun getRootView(): View {
        return viewBinding.root
    }
}