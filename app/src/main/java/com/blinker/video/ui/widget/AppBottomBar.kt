package com.blinker.video.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import com.blinker.video.R
import com.blinker.video.ui.utils.AppConfig
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarMenuView
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import androidx.core.view.forEach

/**
 * @author jiangshiyu
 * @date 2024/12/11
 */
@SuppressLint("RestrictedApi")
class AppBottomBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : BottomNavigationView(context, attrs) {
    private val sIcons = intArrayOf(
        R.drawable.icon_tab_main,
        R.drawable.icon_tab_category,
        R.drawable.icon_tab_publish,
        R.drawable.icon_tab_tags,
        R.drawable.icon_tab_user
    )

    init {
        val config = AppConfig.getBottomConfig()

        val states = arrayOfNulls<IntArray>(2)
        states[0] = IntArray(1) { android.R.attr.state_selected }
        states[1] = intArrayOf()

        val colors =
            intArrayOf(config.activeColor.toColorInt(), config.inActiveColor.toColorInt())
        val colorStateList = ColorStateList(states, colors)
        itemTextColor = colorStateList
        itemIconTintList = colorStateList
        //LABEL_VISIBILITY_LABELED:设置按钮的文本为一直显示模式
        //LABEL_VISIBILITY_AUTO:当按钮个数小于三个时一直显示，或者当按钮个数大于3个且小于5个时，被选中的那个按钮文本才会显示
        //LABEL_VISIBILITY_SELECTED：只有被选中的那个按钮的文本才会显示
        //LABEL_VISIBILITY_UNLABELED:所有的按钮文本都不显示
        labelVisibilityMode = LABEL_VISIBILITY_LABELED

        val tabs = config.tabs
        tabs.forEachIndexed { index, tab ->
            if (!tab.enable) return@forEachIndexed
            val menuItem = menu.add(0, tab.route.hashCode(), index, tab.title)
            menuItem.setIcon(sIcons[index])
        }
        tabs.forEachIndexed { index, tab ->
            println("forEachIndexed:${index},${tab.size}")
            val iconSize = dp2Px(tab.size)
            val menuView = getChildAt(0) as NavigationBarMenuView
            menuView.clipChildren = false
            menuView.clipToPadding = false
            val itemView = menuView.getChildAt(index) as BottomNavigationItemView
            itemView.setIconSize(iconSize)
            if (TextUtils.isEmpty(tab.title)) {
                itemView.setIconTintList(ColorStateList.valueOf(config.activeColor.toColorInt()))
                post(Runnable {
                    itemView.scrollBy(0, dp2Px(20))
                })
            }
        }

        if (config.selectTab >= 0) {
            val tab = tabs[config.selectTab]
            val itemId = tab.route.hashCode()
            post {
                selectedItemId = itemId
            }
        }
    }

    private fun dp2Px(size: Int): Int {
        val density = context.resources.displayMetrics.density
        return (density * size + 0.5f).roundToInt()
    }
}
