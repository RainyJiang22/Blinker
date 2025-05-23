package com.blinker.video.ui.pages.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blinker.video.databinding.LayoutFragmentUserBinding
import com.blinker.video.base.BaseFragment
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.pages.login.UserManager

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment, route = "user_fragment")
class UserFragment : BaseFragment() {
    lateinit var userBinding: LayoutFragmentUserBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        userBinding = LayoutFragmentUserBinding.inflate(inflater, container, false)
        return userBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userBinding.navigateToUserFragment.setOnClickListener {
            UserManager.startLogin()
        }
    }

}