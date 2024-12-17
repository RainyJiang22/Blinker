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
                "com.blinker.video.ui.CategoryFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )
        navList.add(
            NavData(
                "tags_fragment",
                "com.blinker.video.ui.TagsFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )
        navList.add(
            NavData(
                "user_fragment",
                "com.blinker.video.ui.UserFragment",
                false,
                NavDestination.NavType.Fragment
            )
        )
    }

    fun get(): List<NavData> {
        val list = ArrayList<NavData>()
        list.addAll(navList)
        return list
    }
}