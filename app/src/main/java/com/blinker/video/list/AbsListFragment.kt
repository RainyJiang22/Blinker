package com.blinker.video.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.blinker.video.R
import com.blinker.video.databinding.LayoutAbsListFragmentBinding
import com.blinker.video.model.Feed
import com.blinker.video.ui.utils.invokeViewBinding
import com.blinker.video.ui.utils.setVisibility
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
open class AbsListFragment : Fragment(R.layout.layout_abs_list_fragment) {
    private val viewBinding: LayoutAbsListFragmentBinding by invokeViewBinding()
    private lateinit var feedAdapter: FeedAdapter

    companion object {
        const val EXT_FEED_TYPE = "feedType"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        val context:Context = requireContext()

        viewBinding.listView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        feedAdapter = FeedAdapter(getFeedType(), lifecycleOwner = viewLifecycleOwner)
        val contactAdapter = feedAdapter.withLoadStateFooter(FooterLoadStateAdapter())
        viewBinding.listView.adapter = contactAdapter
        viewBinding.listView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewBinding.refreshLayout.setColorSchemeColors(context.getColor(R.color.color_theme))
        viewBinding.refreshLayout.setOnRefreshListener {
            feedAdapter.refresh()
        }

        lifecycleScope.launch {
            // 监听列表数据刷新的动作，比如 加载分页或刷新
            // 此时隐藏loading，显示出列表
            feedAdapter.onPagesUpdatedFlow.collect {
                val hasData = feedAdapter.itemCount > 0
                viewBinding.refreshLayout.isRefreshing = false
                viewBinding.listView.setVisibility(hasData)
                viewBinding.loadingStatus.setVisibility(!hasData)
                if(!hasData) {
                    viewBinding.loadingStatus.showEmpty {
                        feedAdapter.retry()
                    }
                }
            }

//            // 监听Paging的加载状态，如果refresh 动作也就是首次加载是失败，且列表上没有任何item
//            // 此时显示出空布局
//            feedAdapter.loadStateFlow.collect {
//                if (it.source.refresh is LoadState.Error && feedAdapter.itemCount <= 0) {
//                    viewBinding.loadingStatus.showEmpty {
//                        feedAdapter.retry()
//                    }
//                }
//            }
        }
    }

    fun getFeedType(): String {
        return arguments?.getString(EXT_FEED_TYPE) ?: "all"
    }

    fun submitData(pagingData: PagingData<Feed>) {
        lifecycleScope.launch {
            feedAdapter.submitData(pagingData)
        }
    }


}