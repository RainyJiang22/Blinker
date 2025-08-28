package com.blinker.video.ui.pages.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.insertHeaderItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.databinding.LayoutFeedTopCommentBinding
import com.blinker.video.model.TopComment
import com.blinker.video.ui.utils.bindComment
import com.blinker.video.ui.utils.setVisibility

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class FeedCommentListAdapter(val lifecycleOwner: LifecycleOwner, val context: Context) :
    PagingDataAdapter<TopComment, FeedCommentListAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<TopComment>() {
        override fun areItemsTheSame(oldItem: TopComment, newItem: TopComment): Boolean {
            return oldItem.itemId == newItem.itemId || oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TopComment, newItem: TopComment): Boolean {
            return oldItem == newItem
        }

    }) {

    private var pagingData:PagingData<TopComment>? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: TopComment = getItem(position)!!
        holder.bindTopComment(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutFeedTopCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    fun insertHeaderItem(comment: TopComment) {
        val newPagingData = this.pagingData?.insertHeaderItem(item = comment) ?: return
        this.submitPagingData(newPagingData)
    }

    fun submitPagingData(pagingData: PagingData<TopComment>) {
        this.pagingData = pagingData
        submitData(lifecycleOwner.lifecycle, pagingData)
    }

    inner class ViewHolder(private val binding: LayoutFeedTopCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindTopComment(item: TopComment) {
            binding.godComment.setVisibility(false)
            binding.bindComment(lifecycleOwner = lifecycleOwner, item) {
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }
}