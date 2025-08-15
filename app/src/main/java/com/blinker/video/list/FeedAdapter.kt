package com.blinker.video.list

import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFeedAuthorBinding
import com.blinker.video.databinding.LayoutFeedInteractionBinding
import com.blinker.video.databinding.LayoutFeedLabelBinding
import com.blinker.video.databinding.LayoutFeedTextBinding
import com.blinker.video.databinding.LayoutFeedTopCommentBinding
import com.blinker.video.exoplayer.PagePlayDetector
import com.blinker.video.exoplayer.WrapperPlayerView
import com.blinker.video.http.ApiService
import com.blinker.video.model.Author
import com.blinker.video.model.Feed
import com.blinker.video.model.TYPE_IMAGE_TEXT
import com.blinker.video.model.TYPE_TEXT
import com.blinker.video.model.TYPE_VIDEO
import com.blinker.video.model.TopComment
import com.blinker.video.model.Ugc
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.utils.load
import com.blinker.video.ui.utils.setImageResource
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setMaterialButton
import com.blinker.video.ui.utils.setTextColor
import com.blinker.video.ui.utils.setTextVisibility
import com.blinker.video.ui.utils.setVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
class FeedAdapter constructor(
    private val pageName: String,
    private val lifecycleOwner: LifecycleOwner
) :
    PagingDataAdapter<Feed, FeedAdapter.FeedViewHolder>(object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }) {

    private lateinit var playDetector: PagePlayDetector

    override fun getItemViewType(position: Int): Int {
        val feedItem = getItem(position) ?: return 0
        return feedItem.itemType
    }

    override fun onBindViewHolder(
        holder: FeedViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        if (payloads[0] is Ugc) {
            holder.bindInteraction(payloads[0] as Ugc, getItem(position)!!.itemId)
        } else if (payloads[0] is TopComment) {
            holder.bindTopComment(payloads[0] as TopComment)
        }
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feedItem = getItem(position) ?: return
        holder.bindAuthor(feedItem.author)
        holder.bindFeedContent(feedItem.feedsText)
        if (!holder.isVideo()) {
            holder.bindFeedImage(
                feedItem.width,
                feedItem.height,
                PixUtil.dp2px(300),
                feedItem.cover
            )
        } else {
            holder.bindVideoData(
                feedItem.width,
                feedItem.height,
                PixUtil.dp2px(300),
                feedItem.cover,
                feedItem.url
            )
        }
        holder.bindLabel(feedItem.activityText)
        holder.bindTopComment(feedItem.topComment)
        holder.bindInteraction(feedItem.getUgcOrDefault(), feedItem.itemId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        if (viewType != TYPE_TEXT && viewType != TYPE_IMAGE_TEXT && viewType != TYPE_VIDEO) {
            val view = View(parent.context)
            view.visibility = View.GONE
            return FeedViewHolder(view)
        }
        val layoutResId =
            if (viewType == TYPE_IMAGE_TEXT || viewType == TYPE_TEXT) R.layout.layout_feed_type_image else R.layout.layout_feed_type_video
        return FeedViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(layoutResId, parent, false)
        )
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        PagePlayDetector.IPlayDetector {
        private val authorBinding =
            LayoutFeedAuthorBinding.bind(itemView.findViewById(R.id.feed_author))
        private val feedTextBinding =
            LayoutFeedTextBinding.bind(itemView.findViewById(R.id.feed_text))
        private val feedImage: ImageView? = itemView.findViewById(R.id.feed_image)
        private val labelBinding =
            LayoutFeedLabelBinding.bind(itemView.findViewById(R.id.feed_label))
        private val commentBinding =
            LayoutFeedTopCommentBinding.bind(itemView.findViewById(R.id.feed_comment))
        private val interactionBinding =
            LayoutFeedInteractionBinding.bind(itemView.findViewById(R.id.feed_interaction))
        private val playerView: WrapperPlayerView? = itemView.findViewById(R.id.feed_video)

        fun bindAuthor(author: Author?) {
            author?.run {
                authorBinding.authorAvatar.setImageUrl(avatar, true)
                authorBinding.authorName.text = name
            }
        }

        fun bindFeedContent(feedsText: String?) {
            feedTextBinding.root.setTextVisibility(feedsText)
        }

        fun bindFeedImage(width: Int, height: Int, maxHeight: Int, cover: String?) {
            if (feedImage == null || TextUtils.isEmpty(cover)) {
                feedImage?.visibility = View.GONE
                return
            }
            val feedItem = getItem(layoutPosition) ?: return
            feedImage.visibility = View.VISIBLE
            feedImage.load(cover!!) {
                if (width <= 0 && height <= 0) {
                    setFeedImageSize(it.width, it.height, maxHeight)
                }
                if (feedItem.backgroundColor == 0) {
                    lifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO) {
                        val defaultColor = feedImage.context.getColor(R.color.color_theme_10)
                        val color = Palette.Builder(it).generate().getMutedColor(defaultColor)
                        feedItem.backgroundColor = color
                        withContext(lifecycleOwner.lifecycle.coroutineScope.coroutineContext) {
                            feedImage.background = ColorDrawable(feedItem.backgroundColor)
                        }
                    }
                } else {
                    feedImage.background = ColorDrawable(feedItem.backgroundColor)
                }
            }

            if (width > 0 && height > 0) {
                setFeedImageSize(width, height, maxHeight)
            }
        }

        private fun setFeedImageSize(width: Int, height: Int, maxHeight: Int) {
            val finalWidth: Int = PixUtil.getScreenWidth();
            val finalHeight: Int = if (width > height) {
                (height / (width * 1.0f / finalWidth)).toInt()
            } else {
                maxHeight
            }
            val params = feedImage!!.layoutParams as LinearLayout.LayoutParams
            params.width = finalWidth
            params.height = finalHeight
            params.gravity = Gravity.CENTER
            feedImage.scaleType = ImageView.ScaleType.FIT_CENTER
            feedImage.layoutParams = params
        }

        fun bindLabel(activityText: String?) {
            labelBinding.root.setTextVisibility(activityText)
        }

        fun bindTopComment(topComment: TopComment?) {
            commentBinding.root.setVisibility(topComment != null)
            commentBinding.mediaLayout.setVisibility(topComment?.imageUrl != null)
            topComment?.run {
                commentBinding.commentAuthor.setTextVisibility(author?.name)
                commentBinding.commentAvatar.setImageUrl(author?.avatar, true)
                commentBinding.commentText.setTextVisibility(commentText)
                commentBinding.commentLikeCount.setTextVisibility(this.getUgcOrDefault().likeCount.toString())
                commentBinding.commentPreviewVideoPlay.setVisibility(videoUrl != null)
                commentBinding.commentLikeCount.setTextColor(
                    this.getUgcOrDefault().hasLiked,
                    R.color.color_theme,
                    R.color.color_3d3
                )
                commentBinding.commentLikeStatus.setImageResource(
                    this.getUgcOrDefault().hasLiked,
                    R.drawable.icon_cell_liked,
                    R.drawable.icon_cell_like
                )

                commentBinding.commentLikeStatus.setOnClickListener {
                    lifecycleOwner.lifecycleScope.launch {
                        UserManager.loginIfNeed()
                        UserManager.getUser().collectLatest {
                            val apiResult = ApiService.getService()
                                .toggleCommentLike(commentId, itemId, it.userId)
                            apiResult.body?.run {
                                val ugc = topComment.getUgcOrDefault()
                                ugc.hasLiked = this.getAsJsonPrimitive("hasLiked").asBoolean
                                ugc.likeCount = this.getAsJsonPrimitive("likeCount").asInt
                                notifyItemChanged(layoutPosition, topComment)
                            }
                        }
                    }
                }
            }
        }

        fun bindInteraction(ugc: Ugc, itemId: Long) {
            ugc.run {
                interactionBinding.interactionLike.setMaterialButton(
                    likeCount.toString(), hasLiked,
                    R.drawable.icon_cell_liked,
                    R.drawable.icon_cell_like
                )
                interactionBinding.interactionDiss.setMaterialButton(
                    null, hasdiss,
                    R.drawable.icon_cell_dissed,
                    R.drawable.icon_cell_diss
                )
                interactionBinding.interactionComment.text = commentCount.toString()
                interactionBinding.interactionShare.text = shareCount.toString()
            }
            interactionBinding.interactionLike.setOnClickListener {
                toggleFeedLike(itemId, true)
            }
            interactionBinding.interactionDiss.setOnClickListener {
                toggleFeedLike(itemId, false)
            }
        }

        private fun toggleFeedLike(itemId: Long, like: Boolean) {
            lifecycleOwner.lifecycleScope.launch {
                UserManager.loginIfNeed()
                UserManager.getUser().collectLatest {
                    if (it.userId <= 0) return@collectLatest
                    val apiResult = if (like) ApiService.getService()
                        .toggleFeedLike(itemId, it.userId) else ApiService.getService()
                        .toggleDissFeed(itemId, it.userId)
                    apiResult.body?.run {
                        val ugc = snapshot().items[layoutPosition].getUgcOrDefault()
                        ugc.hasLiked = this.getAsJsonPrimitive("hasLiked").asBoolean
                        ugc.hasdiss = this.getAsJsonPrimitive("hasdiss").asBoolean
                        ugc.likeCount = this.getAsJsonPrimitive("likeCount").asInt
                        notifyItemChanged(layoutPosition, ugc)
                    }
                }
            }
        }

        fun bindVideoData(width: Int, height: Int, maxHeight: Int, cover: String?, url: String?) {
            url?.run {
                playerView?.run {
                    setVisibility(true)
                    bindData(width, height, cover, url, maxHeight)
                    setListener(object : WrapperPlayerView.Listener {
                        override fun onTogglePlay(attachView: WrapperPlayerView) {
                            playDetector.togglePlay(attachView, url)
                        }
                    })
                }
            }
        }

        override fun getAttachView(): WrapperPlayerView {
            return playerView!!
        }

        override fun getVideoUrl(): String {
            return getItem(layoutPosition)?.url!!
        }

        fun isVideo(): Boolean {
            return getItem(layoutPosition)?.itemType == TYPE_VIDEO
        }
    }

    override fun onViewAttachedToWindow(holder: FeedViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.isVideo()) {
            playDetector.addDetector(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: FeedViewHolder) {
        super.onViewDetachedFromWindow(holder)
        playDetector.removeDetector(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        playDetector = PagePlayDetector(pageName, lifecycleOwner, recyclerView)
    }
}