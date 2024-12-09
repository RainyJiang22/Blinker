package com.blinker.video.plugin.runtime

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NavDestination(val type: NavType, val route: String) {
    enum class NavType {
        Fragment,
        Activity,
        Dialog,
        None
    }
}