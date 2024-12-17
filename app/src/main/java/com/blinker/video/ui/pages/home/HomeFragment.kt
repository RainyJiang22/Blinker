package com.blinker.video.ui.pages.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blinker.video.list.AbsListFragment
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.utils.invokeViewModel
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment,route="home_fragment")
class HomeFragment : AbsListFragment() {

    private val viewModel: HomeViewModel by invokeViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.hotFeeds.collect {
                submitData(it)
            }
        }
    }

}