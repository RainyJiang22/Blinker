package com.blinker.video.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFragmentHomeBinding
import com.blinker.video.base.BaseFragment
import com.blinker.video.http.ApiService
import com.blinker.video.plugin.runtime.NavDestination
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment,route="home_fragment")
class HomeFragment : BaseFragment() {

    lateinit var homeBinding: LayoutFragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = LayoutFragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val apiResult = ApiService.getService().getFeeds()
        }
    }

}