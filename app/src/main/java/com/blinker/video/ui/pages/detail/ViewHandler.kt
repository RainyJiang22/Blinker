package com.blinker.video.ui.pages.detail


import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.databinding.LayoutFeedAuthorBinding
import com.blinker.video.databinding.LayoutFeedLabelBinding
import com.blinker.video.databinding.LayoutFeedTextBinding
import com.blinker.video.list.FooterLoadStateAdapter
import com.blinker.video.list.LoadingStateAdapter
import com.blinker.video.model.Feed
import com.blinker.video.ui.utils.IViewBinding
import com.blinker.video.ui.utils.invokeViewModel
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setTextVisibility
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class ViewHandler(val context: FragmentActivity) : IViewBinding, ViewModelStoreOwner {
    private val feedCommentViewModel: FeedCommentViewModel by invokeViewModel()
    protected lateinit var listView: RecyclerView
    private lateinit var feedItem: Feed

    open fun bindInitData(feed: Feed) {
        this.feedItem = feed
        bindFeedComments()
    }

    private fun bindFeedComments() {
        feedCommentViewModel.setItemId(feedItem.itemId)
        listView.layoutManager = LinearLayoutManager(context)
        listView.itemAnimator = null

        val feedCommentListAdapter = FeedCommentListAdapter(getLifecycleOwner(), context)
        val loadingStateAdapter = LoadingStateAdapter()
        val concatAdapter = feedCommentListAdapter.withLoadStateHeaderAndFooter(
            loadingStateAdapter,
            FooterLoadStateAdapter()
        )
        listView.adapter = concatAdapter
        context.lifecycleScope.launch {
            feedCommentListAdapter.addLoadStateListener {
                if (feedCommentListAdapter.itemCount > 0) {
                    concatAdapter.removeAdapter(loadingStateAdapter)
                }
            }
            feedCommentViewModel.pageFlow.collectLatest {
                feedCommentListAdapter.submitData(getLifecycleOwner().lifecycle, it)
            }
        }
    }

    fun bindAuthorInfo(
        authorInfoBinding: LayoutFeedAuthorBinding,
        feedTextBinding: LayoutFeedTextBinding,
        feedLabelBinding: LayoutFeedLabelBinding
    ) {
        authorInfoBinding.authorAvatar.setImageUrl(feedItem.author?.avatar)
        authorInfoBinding.authorName.text = feedItem.author?.name
        feedTextBinding.feedText.setTextVisibility(feedItem.feedsText)

        feedLabelBinding.root.text = feedItem.activityText
    }

    override fun getLayoutInflater(): LayoutInflater {
        return context.layoutInflater
    }

    override fun getLifecycleOwner(): LifecycleOwner {
        return context
    }

    override val viewModelStore: ViewModelStore
        get() = context.viewModelStore



    abstract fun getRootView(): View
}