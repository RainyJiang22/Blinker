package com.blinker.video.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutAbsListLoadingFooterBinding
import com.blinker.video.databinding.LayoutAbsListLoadingFooterBinding.inflate

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
class FooterLodStateAdapter : LoadStateAdapter<FooterLodStateAdapter.LoadStateViewHolder>() {


    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        val loading = holder.binding.loading
        val loadingText = holder.binding.text

        when(loadState) {
            is LoadState.Loading -> {
                loadingText.setText(R.string.abs_list_loading_footer_loading)
                loading.show()
                return
            }

            is LoadState.Error -> {
                loadingText.setText(R.string.abs_list_loading_footer_error)
            }
            else -> {}
        }
        loading.hide()
        loading.postOnAnimation { loading.visibility = View. GONE }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadStateViewHolder(binding)
    }


    class LoadStateViewHolder(val binding:LayoutAbsListLoadingFooterBinding) :
        RecyclerView.ViewHolder(binding.root)



}