package com.blinker.video.ui.pages.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.blinker.video.list.AbsListFragment
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.utils.invokeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment, route = "home_fragment")
class HomeFragment : AbsListFragment() {

    private val viewModel: HomeViewModel by invokeViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.pageFlow.collect {
                lifecycle.withStarted {
                    viewModel.setFeedType(getFeedType())
                    submitData(it)
                }
            }
        }
    }

    companion object {
        fun newInstance(feedType: String?): Fragment {
            return HomeFragment().apply {
                this.arguments = Bundle().apply {
                    putString(EXT_FEED_TYPE, feedType)
                }
            }
        }
    }

}