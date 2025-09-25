package com.blinker.video.ui.pages.detail

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.blinker.video.databinding.LayoutFeedDetailTypeTextBinding
import com.blinker.video.model.Feed
import com.blinker.video.ui.utils.invokeViewBinding

/**
 * @author jiangshiyu
 * @date 2025/9/25
 * 纯文本
 */
class TextViewHandler(context: FragmentActivity) : ViewHandler(context) {
    private val viewBinding: LayoutFeedDetailTypeTextBinding by invokeViewBinding()

    init {
        listView = viewBinding.listView
        bottomInteractionBinding = viewBinding.bottomInteraction
        viewBinding.actionClose.setOnClickListener {
            context.finish()
        }
    }


    override fun bindInitData(feed: Feed) {
        super.bindInitData(feed)
        bindAuthorInfo(viewBinding.feedAuthor,viewBinding.feedText, viewBinding.feedLabel)
    }

    override fun getRootView(): View {
        return viewBinding.root
    }

    override fun onBackPressed() {
        viewBinding.actionClose.performClick()
    }


}