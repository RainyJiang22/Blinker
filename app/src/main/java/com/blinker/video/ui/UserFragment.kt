package com.blinker.video.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFragmentUserBinding
import com.blinker.video.base.BaseFragment
import com.blinker.video.plugin.runtime.NavDestination

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

        userBinding.navigateBack.setOnClickListener {
            findNavController().popBackStack(
                R.id.tags_fragment,
                inclusive = false,
                saveState = true
            )
        }

        userBinding.navigateUp.setOnClickListener {
            findNavController().popBackStack(
                R.id.home_fragment,
                inclusive = false,
                saveState = true
            )
        }
    }

}