package com.blinker.video.list

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.widget.LoadingStatusView

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class LoadingStateAdapter(val height: Int = PixUtil.getScreenHeight() / 3) :
    LoadStateAdapter<RecyclerView.ViewHolder>() {
    private var recyclerView: RecyclerView? = null
    private var loadingStatusView: LoadingStatusView? = null
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        loadState: LoadState,
    ) {
        loadingStatusView?.showEmpty(text = "快来抢第一把🪑吧~")

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): RecyclerView.ViewHolder {
        loadingStatusView = LoadingStatusView(parent.context)
        loadingStatusView?.layoutParams = ViewGroup.LayoutParams(-1, height)
        return object : RecyclerView.ViewHolder(loadingStatusView!!) {}
    }


}