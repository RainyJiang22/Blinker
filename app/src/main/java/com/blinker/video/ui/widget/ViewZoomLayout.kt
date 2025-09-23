package com.blinker.video.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.blinker.video.R
import com.blinker.video.exoplayer.WrapperPlayerView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author jiangshiyu
 * @date 2025/9/23
 */
class ViewZoomLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val viewDragHelper = ViewDragHelper.create(this, 1.0f, Callback())
    private val overScroller = OverScroller(context)
    private var playerView: WrapperPlayerView? = null
    private var scrollingView: View? = null
    private var minHeight: Int = 0
    private var maxHeight: Int = 0
    private var canDragZoom = false
    private var flingRunnable: Callback.FlingRunnable? = null


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (playerView == null) {
            playerView = findViewById(R.id.player_view)
            scrollingView = findViewById(R.id.list_view)

            val videoWidth = playerView!!.videoWidthPx
            val videoHeight = playerView!!.videoHeightPx
            minHeight = playerView!!.measuredHeight
            maxHeight = ((this.measuredWidth * 1.0f / videoWidth) * videoHeight).toInt()

            canDragZoom = videoHeight > videoWidth
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 将触摸事件传递给ViewDragHelper
        if (!canDragZoom) return super.onInterceptTouchEvent(ev)
        if (overScroller.currY != overScroller.finalY) {
            overScroller.abortAnimation()
            return true
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // 将触摸事件传递给ViewDragHelper
        if (!canDragZoom)return super.onTouchEvent(ev)
        viewDragHelper.processTouchEvent(ev)
        return true
    }

    inner class Callback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            // 尝试捕获子视图，如果返回true，则子视图将被捕获并可以进行拖动。
            if (!canDragZoom) return false
            // playerView!!.removeCallbacks(flingRunnable)
            return (playerView!!.bottom in minHeight..maxHeight)
        }

        // 返回子视图在水平和垂直方向上可以被拖动的最大距离。
        override fun getViewVerticalDragRange(child: View): Int {
            if (!canDragZoom) return 0
            return maxHeight - minHeight
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            //修正这个被拖拽的view 它的垂直方向上top的位置是多少
            if (dy == 0) return 0
            // dy>0 代表的是 手指从屏幕上 向下滑动
            // dy<0 代表的是 手指从屏幕下 向上滑动
            // 还代表了 滑动方向
            if ((dy < 0 && playerView!!.bottom <= minHeight)
                || (dy > 0 && playerView!!.bottom >= maxHeight)
                || (dy > 0 && scrollingView!!.canScrollVertically(-1))
            ) {
                return 0
            }
            var maxConsumed = 0
            if (dy > 0) {
                // 向下滑动，playerView被放大，但是playerView.Bottom+滑动距离也不应超过maxHeight
                maxConsumed = if (playerView!!.bottom + dy > maxHeight) {
                    maxHeight - playerView!!.bottom
                } else {
                    dy
                }
            } else {
                // 向上滑动.playerView被缩小，但是playerView.Bottom+滑动距离也不应小于minHeight
                maxConsumed = if (playerView!!.bottom + dy < minHeight) {
                    minHeight - playerView!!.bottom
                } else {
                    dy
                }
            }
            // 根据计算后的滑动距离修改playerView 的layoutParams
            val layoutParams = playerView!!.layoutParams
            layoutParams.height = layoutParams.height + maxConsumed
            playerView!!.layoutParams = layoutParams
            return maxConsumed
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if ((playerView!!.bottom in minHeight..maxHeight) && yvel != 0f) {
                playerView!!.removeCallbacks(flingRunnable)
                flingRunnable = FlingRunnable()
                flingRunnable?.fling(xvel.toInt(), yvel.toInt())
            }
        }

        inner class FlingRunnable : Runnable {

            fun fling(xvel: Int, yvel: Int) {
                /**
                 *startX:开始的X值，由于我们不需要再水平方向滑动 所以为0
                 *startY:开始滑动时Y的起始值，那就是flingview的bottom值
                 *xxel:水平方向上的速度，实际上为0的
                 *yxel:垂直方向上的速度。即松手时的速度
                 *minX:水平方向上 滚动回弹的越界最小值，给0即可
                 *maxX:水平方向上 滚动回弹越界的最大值，实际上给O也是一样的
                 *minY:垂直方向上 滚动回弹的越界最小值，给0即可
                 *maxy:垂直方向上，滚动回弹越界的最大值，实际上给0也一样
                 */
                overScroller.fling(
                    0,
                    playerView!!.bottom,
                    xvel,
                    yvel,
                    0,
                    Int.MAX_VALUE,
                    0,
                    Int.MAX_VALUE
                )
                run()
            }

            override fun run() {
                val layoutParams = playerView!!.layoutParams
                if (overScroller.computeScrollOffset() && layoutParams.height in minHeight..maxHeight) {
                    val newHeight = max(min(overScroller.currY, maxHeight), minHeight)
                    if (newHeight != layoutParams.height) {
                        layoutParams.height = newHeight
                        playerView!!.layoutParams = layoutParams
                    }
                    ViewCompat.postOnAnimation(playerView!!,this)
                }else{
                    playerView!!.removeCallbacks(this)
                }
            }
        }
    }
}