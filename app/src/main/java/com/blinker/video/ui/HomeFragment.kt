package com.blinker.video.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFragmentHomeBinding
import com.blinker.video.base.BaseFragment
import com.blinker.video.plugin.runtime.NavDestination

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
    var flag = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        homeBinding.navigateToCategoryFragment.setOnClickListener {
            // 对于fragment 类型的路由节点，在 navigate 跳转的时候  使用的fragmentTransaction#replace
            if (flag) {
                navController.navigate(R.id.category_fragment)
                flag = true
            } else {
                navController.navigate(
                    R.id.category_fragment,
                    null,
                    NavOptions.Builder().setRestoreState(true).build()
                )
            }
  //          navController.navigate(NavDeepLinkRequest.Builder.fromUri(Uri.parse("https://com.blinker.video/user?phone=124444")).build())
        }

        homeBinding.navigateUp.setOnClickListener {
            navController.clearBackStack(R.id.category_fragment)
        }
    }


}