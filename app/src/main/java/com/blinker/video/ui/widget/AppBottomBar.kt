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
class AppBottomBarWithCenter @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : BottomNavigationView(context, attrs) {

    private val sIcons = intArrayOf(
        R.drawable.icon_tab_main,
        R.drawable.icon_tab_category,
        R.drawable.icon_tab_publish, // 中间大按钮
        R.drawable.icon_tab_tags,
        R.drawable.icon_tab_user
    )

    init {
        val config = AppConfig.getBottomConfig()

        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        )
        val colors = intArrayOf(
            config.activeColor.toColorInt(),
            config.inActiveColor.toColorInt()
        )
        val colorStateList = ColorStateList(states, colors)
        itemTextColor = colorStateList
        itemIconTintList = colorStateList
        labelVisibilityMode = LABEL_VISIBILITY_SELECTED

        val tabs = config.tabs
        tabs.forEachIndexed { index, tab ->
            if (!tab.enable) return@forEachIndexed
            menu.add(0, tab.route.hashCode(), index, tab.title).setIcon(sIcons[index])
        }

        post {
            val menuView = getChildAt(0) as NavigationBarMenuView
            var menuIndex = 0
            tabs.forEach { tab ->
                if (!tab.enable) return@forEach

                val itemView = menuView.getChildAt(menuIndex) as BottomNavigationItemView

                // 中间按钮放大并隐藏文字
                if (tab.route == "publish") {
                    val iconSizePx = dpToPx(tab.size * 1.5f)
                    itemView.setIconSize(iconSizePx)
                    itemView.setShifting(false)
                    itemView.setLabelVisibilityMode(LABEL_VISIBILITY_UNLABELED)

                    // 设置点击事件，手动更新选中状态
                    itemView.setOnClickListener {
                        // 清除其他菜单选中状态
                        menu.forEach { it.isChecked = false }
                        itemView.isSelected = true
                        // 可触发中间按钮回调
                        onCenterClickListener?.invoke()
                    }
                } else {
                    val iconSizePx = dpToPx(tab.size.toFloat())
                    itemView.setIconSize(iconSizePx)
                }
                menuIndex++
            }
        }

        // 默认选中
        if (config.selectTab >= 0 && config.selectTab < tabs.size) {
            post {
                selectedItemId = tabs[config.selectTab].route.hashCode()
            }
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        ).toInt()
    }

    private var onCenterClickListener: (() -> Unit)? = null
    fun setOnCenterClickListener(listener: () -> Unit) {
        onCenterClickListener = listener
    }
}
