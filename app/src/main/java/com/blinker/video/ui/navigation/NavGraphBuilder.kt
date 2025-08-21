package com.blinker.video.ui.navigation

import android.content.ComponentName
import androidx.fragment.app.FragmentActivity
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.get
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.plugin.runtime.NavRegistry
import kotlin.hashCode

/**
 * @author jiangshiyu
 * @date 2024/12/13
 */
object NavGraphBuilder {

    fun build(controller: NavController, context: FragmentActivity, containerId: Int) {
        // 1. 构建navGraph路由表对象
        val provider = controller.navigatorProvider
        val graphNavigator = provider.get<NavGraphNavigator>("navigation")
        val navGraph = graphNavigator.createDestination()

        val iterator = NavRegistry.get().listIterator()
        while (iterator.hasNext()) {
            val navData = iterator.next()
            when (navData.type) {
                NavDestination.NavType.Fragment -> {
                    val navigator = provider.get<FragmentNavigator>("fragment")
                    val destination = navigator.createDestination();
                    destination.id = navData.route.hashCode()
                    destination.setClassName(navData.className)
                    navGraph.addDestination(destination)
                }
                NavDestination.NavType.Activity -> {
                    val navigator = provider.get<ActivityNavigator>("activity")
                    val destination = navigator.createDestination();
                    destination.id = navData.route.hashCode()
                    destination.setComponentName(
                        ComponentName(
                            context.packageName,
                            navData.className
                        )
                    )
                    navGraph.addDestination(destination)
                }

                NavDestination.NavType.Dialog -> {
                    val navigator = provider.get<DialogFragmentNavigator>("dialog")
                    val destination = navigator.createDestination();
                    destination.id = navData.route.hashCode()
                    destination.setClassName(navData.className)
                    navGraph.addDestination(destination)
                }
                else -> {
                    throw java.lang.IllegalStateException("cant create NavGraph,because unknown ${navData.type}")
                }
            }

            if (navData.asStarter) {
                navGraph.setStartDestination(navData.route.hashCode())
            }
        }

        controller.setGraph(navGraph, null)
    }
}