package com.blinker.video.ui.pages.detail

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.blinker.video.databinding.LayoutFeedDetailTypeVideoBinding
import com.blinker.video.ui.utils.invokeViewBinding

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class VideoViewHandler(context: FragmentActivity) : ViewHandler(context) {

    private val viewBinding: LayoutFeedDetailTypeVideoBinding by invokeViewBinding()


    init {
        listView = viewBinding.listView
        bottomInteractionBinding = viewBinding.bottomInteraction
    }

    override fun getRootView(): View {
        return viewBinding.root
    }
}