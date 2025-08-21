package com.blinker.video.plugin.runtime

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
object NavRegistry {

    private val navList: ArrayList<NavData> = ArrayList<NavData>()

    init {
        navList.add(
            NavData(
                "home_fragment",
                "com.blinker.video.ui.pages.home.HomeFragment",
                true,
                NavDestination.NavType.Fragment
            )
        )
        navList.add(
            NavData(
                "category_fragment",
                "com.blinker.video.ui.pages.category.CategoryFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )
        navList.add(
            NavData(
                "tags_fragment",
                "com.blinker.video.ui.pages.tags.TagsFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )
        navList.add(
            NavData(
                "user_fragment",
                "com.blinker.video.ui.pages.user.UserFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )

        navList.add(
            NavData(
                "activity_capture",
                "com.blinker.video.ui.pages.publish.CaptureActivity",
                false,
                NavDestination.NavType.Activity
            )
        )
    }

    fun get(): List<NavData> {
        val list = ArrayList<NavData>()
        list.addAll(navList)
        return list
    }
}