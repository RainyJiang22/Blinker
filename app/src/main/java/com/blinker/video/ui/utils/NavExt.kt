package com.blinker.video.ui.utils

import android.content.ComponentName
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.get
import com.blinker.video.plugin.runtime.NavDestination.NavType
import com.blinker.video.plugin.runtime.NavRegistry
import com.blinker.video.ui.pages.MainActivity

/**
 * @author jiangshiyu
 * @date 2024/12/10
 */

fun NavController.switchTab(route: String, args: Bundle? = null) {
    val destId = route.hashCode()
    val list = backQueue.filter {
        it.destination.id == destId
    }
    if (list.isEmpty()) {
        navigateTo(route, args)
    } else {
        navigateBack(route,false,false)
    }
}


fun NavController.navigateTo(route: String, args: Bundle? = null, navOptions: NavOptions? = null) {
    navigate(route.hashCode(), args, navOptions)

}

fun NavController.navigateBack(
    route: String,
    inclusive: Boolean = false,
    saveState: Boolean = false
) {
    popBackStack(route.hashCode(), inclusive, saveState)
}

fun MainActivity.injectNavGraph(controller: NavController) {
    // 1. 构建navGraph路由表对象
    val provider = controller.navigatorProvider
    val graphNavigator = provider.get<NavGraphNavigator>("navigation")
    val navGraph = graphNavigator.createDestination()

    val iterator = NavRegistry.get().listIterator()
    while (iterator.hasNext()) {
        val navData = iterator.next()
        when (navData.type) {
            NavType.Fragment -> {
                val navigator = provider.get<FragmentNavigator>("fragment")
                val destination = navigator.createDestination();
                destination.id = navData.route.hashCode()
                destination.setClassName(navData.className)
                navGraph.addDestination(destination)
            }
            NavType.Activity -> {
                val navigator = provider.get<ActivityNavigator>("activity")
                val destination = navigator.createDestination();
                destination.id = navData.route.hashCode()
                destination.setComponentName(
                    ComponentName(
                        packageName,
                        navData.className
                    )
                )
                navGraph.addDestination(destination)
            }
            NavType.Dialog -> {
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