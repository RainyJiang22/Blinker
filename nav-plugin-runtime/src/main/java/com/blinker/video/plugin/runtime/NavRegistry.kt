package com.blinker.video.plugin.runtime

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
object NavRegistry {

    private val navList: ArrayList<NavData> = ArrayList<NavData>()

    init {
        navList.add(NavData("home_fragment","com.blinker.video.navigation.HomeFragment", NavDestination.NavType.Fragment))
    }

    fun get(): List<NavData> {
        val list = ArrayList<NavData>()
        list.addAll(navList)
        return list
    }
}