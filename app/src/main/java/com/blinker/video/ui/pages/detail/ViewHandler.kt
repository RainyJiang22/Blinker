package com.blinker.video.ui.pages.detail


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFeedAuthorBinding
import com.blinker.video.databinding.LayoutFeedDetailBottomInteractionBinding
import com.blinker.video.databinding.LayoutFeedLabelBinding
import com.blinker.video.databinding.LayoutFeedTextBinding
import com.blinker.video.list.FooterLoadStateAdapter
import com.blinker.video.list.LoadingStateAdapter
import com.blinker.video.model.Feed
import com.blinker.video.model.TopComment
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.IViewBinding
import com.blinker.video.ui.utils.invokeViewModel
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setMaterialButton
import com.blinker.video.ui.utils.setTextVisibility
import com.blinker.video.ui.utils.toggleFavorite
import com.blinker.video.ui.utils.toggleFeedLike
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class ViewHandler(val context: FragmentActivity) : IViewBinding, ViewModelStoreOwner {
    private lateinit var feedCommentListAdapter: FeedCommentListAdapter
    private val feedCommentViewModel: FeedCommentViewModel by invokeViewModel()
    protected lateinit var listView: RecyclerView
    private lateinit var feedItem: Feed
    open lateinit var bottomInteractionBinding: LayoutFeedDetailBottomInteractionBinding

    open fun bindInitData(feed: Feed) {
        this.feedItem = feed
        bindFeedComments()
        bindBottomInteraction()
        bindLifecycle()
    }

    private fun bindLifecycle() {
        context.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(
                source: LifecycleOwner,
                event: Lifecycle.Event,
            ) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> onResume()
                    Lifecycle.Event.ON_PAUSE -> onPause()
                    Lifecycle.Event.ON_DESTROY -> onDestroy()
                    Lifecycle.Event.ON_STOP -> onStop()
                    else -> {}
                }
            }

        })

        context.onBackPressedDispatcher.addCallback(context, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }

        })
    }

    private fun bindBottomInteraction() {
        val commentDialog = CommentDialog.newInstance(feedItem.itemId)
        commentDialog.setCommentAddListener(object : CommentDialog.ICommentListener {
            override fun onAddComment(comment: TopComment) {
                feedCommentListAdapter.insertHeaderItem(comment)
                listView.post {
                    listView.scrollToPosition(0)
                }
            }
        })
        bottomInteractionBinding.inputView.setOnClickListener {
            context.lifecycleScope.launch {
                context.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    UserManager.loginIfNeed()
                    UserManager.getUser().collectLatest {
                        commentDialog.show(context.supportFragmentManager, "comment_dialog")
                    }
                }
            }
        }

        bottomInteractionBinding.interactionLike.setMaterialButton(
            "", feedItem.getUgcOrDefault().hasLiked,
            R.drawable.icon_cell_liked, R.drawable.icon_cell_like
        )
        bottomInteractionBinding.interactionFavorite.setMaterialButton(
            "", feedItem.getUgcOrDefault().hasFavorite,
            R.drawable.icon_collected, R.drawable.icon_collect
        )

        bottomInteractionBinding.interactionLike.setOnClickListener {
            context.lifecycleScope.launch {
                feedItem.toggleFeedLike(!feedItem.getUgcOrDefault().hasLiked) {
                    feedItem.getUgcOrDefault().hasLiked = it.hasLiked
                    feedItem.getUgcOrDefault().hasdiss = it.hasdiss
                    feedItem.getUgcOrDefault().likeCount = it.likeCount
                    bindBottomInteraction()
                }
            }
        }
        bottomInteractionBinding.interactionFavorite.setOnClickListener {
            context.lifecycleScope.launch {
                feedItem.toggleFavorite {
                    feedItem.getUgcOrDefault().hasFavorite = it
                    bindBottomInteraction()
                }
            }
        }
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

        getHeaderView()?.run {
            concatAdapter.addAdapter(0, object : LoadStateAdapter<RecyclerView.ViewHolder>() {
                override fun onBindViewHolder(
                    holder: RecyclerView.ViewHolder, loadState: LoadState,
                ) {
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    loadState: LoadState,
                ): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(this@run) {}
                }

                override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
                    return true
                }
            })
        }

        listView.adapter = concatAdapter
        context.lifecycleScope.launch {
            feedCommentListAdapter.addLoadStateListener {
                if (feedCommentListAdapter.itemCount > 0) {
                    concatAdapter.removeAdapter(loadingStateAdapter)
                }
            }
            feedCommentViewModel.pageFlow.collectLatest {
                feedCommentListAdapter.submitPagingData(it)
            }
        }
    }

    fun bindAuthorInfo(
        authorInfoBinding: LayoutFeedAuthorBinding,
        feedTextBinding: LayoutFeedTextBinding,
        feedLabelBinding: LayoutFeedLabelBinding,
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

    override val viewModelStore: ViewModelStore = context.viewModelStore


    open fun getHeaderView(): View? {
        return null
    }

    abstract fun getRootView(): View

    open fun onBackPressed() {}
    open fun onStop() {}
    open fun onPause() {}
    open fun onResume() {}
    open fun onDestroy() {}
}