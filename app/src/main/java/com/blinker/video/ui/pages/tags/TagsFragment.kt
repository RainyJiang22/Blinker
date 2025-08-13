package com.blinker.video.ui.pages.tags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blinker.video.base.BaseFragment
import com.blinker.video.plugin.runtime.NavDestination

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment, route = "tags_fragment")
class TagsFragment : BaseFragment()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("fragmentlife", "TagsFragment onCreate:$savedInstanceState")
    }


}