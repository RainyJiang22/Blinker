package com.blinker.video.ui.pages.publish

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withTranslation
import androidx.core.graphics.createBitmap
import com.blinker.video.R
import com.blinker.video.ui.utils.dp
import com.blinker.video.ui.utils.drawBitmapCenterCrop

/**
 * @author : created by Freeman on 2025/6/11
 */
class VideoCutoutCtrlView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val MODE_SCROLL_LEFT = 0
        const val MODE_SCROLL_RIGHT = 1
        const val MODE_NONE = 2
    }

    var bitmapFrame: ArrayList<Bitmap>? = null
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * 最小长度时间百分比
     */
    var minLengthP = 0f

    var progress = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var modeTouch = MODE_NONE

    /**
     * 内容占总体高度多少
     */
    private val contentHeightP = 0.8f
    private val radius = (4f).dp
    private val bitmapClipLeft by lazy {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tailor_clip_btn_left
        )
    }
    private val bitmapClipRight by lazy {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tailor_clip_btn_right
        )
    }
    private val paintBg by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#262535")
        }
    }
    private val paintContentRect by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 2.dp
            color = Color.WHITE

        }
    }

    var onLengthChangeListener: ((startP: Float, endP: Float, isScrollStart: Boolean) -> Unit)? =
        null
    var onTouchingListener: ((isTouch: Boolean) -> Unit)? = null

    private val rectLeftClip = RectF()
    private val rectRightClip = RectF()
    private val rectCache = RectF()
    private var bitmapProgress: Bitmap? = null
    private val rectContent = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val heightItem = contentHeightP * h

        val top = (h - heightItem) / 2f
        val widthItem = heightItem * bitmapClipLeft.width / bitmapClipLeft.height
        rectLeftClip.set(0f, top, widthItem + paintContentRect.strokeWidth, h - top)
        rectRightClip.set(w - widthItem - paintContentRect.strokeWidth, top, w.toFloat(), h - top)

        rectContent.set(
            rectLeftClip.width(),
            rectLeftClip.top + paintContentRect.strokeWidth,
            rectRightClip.left,
            rectLeftClip.bottom - paintContentRect.strokeWidth,
        )

        bitmapProgress = createBitmap(progressLineStroke.toInt(), h)
        val canvas = Canvas(bitmapProgress!!)

        val halfLineStroke = progressLineStroke / 2f
        canvas.drawRoundRect(
            0f,
            0f,
            bitmapProgress!!.width.toFloat(),
            bitmapProgress!!.height.toFloat(),
            halfLineStroke,
            halfLineStroke,
            paintProgress
        )
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawRoundRect(
            0f,
            canvas.height * (1f - contentHeightP) / 2f,
            canvas.width.toFloat(),
            canvas.height * (1f + contentHeightP) / 2f,
            radius,
            radius,
            paintBg
        )

        if (bitmapFrame?.isNotEmpty() == true) {
            val widthItem = rectContent.width() / bitmapFrame!!.size
            rectCache.set(0f, 0f, widthItem, rectContent.height())
            canvas.withTranslation(rectContent.left, rectContent.top) {
                bitmapFrame?.forEachIndexed { index, bitmap ->
                    drawBitmapCenterCrop(this, bitmap, rectCache)
                    translate(rectCache.width(), 0f)
                }
            }
        }

        val halfStrokeWidth = paintContentRect.strokeWidth / 2f
        paintBg.alpha = 179
        canvas.drawRect(
            0f,
            rectContent.top,
            rectLeftClip.right,
            rectContent.bottom,
            paintBg
        )
        canvas.drawRect(
            width.toFloat(),
            rectContent.top,
            rectRightClip.left,
            rectContent.bottom,
            paintBg
        )
        paintBg.alpha = 255

//        val halfLineStroke = progressLineStroke / 2f
//        val x = rectContent.left + (rectContent.width() * progress)
//        canvas.drawRoundRect(
//            x - halfLineStroke,
//            0f,
//            x + halfLineStroke,
//            height.toFloat(),
//            halfLineStroke,
//            halfLineStroke,
//            paintProgress
//        )
        bitmapProgress?.let {
            val x = rectContent.left + (rectContent.width() * progress)
            canvas.drawBitmap(it, x, 0f, null)
        }



        canvas.drawLine(
            rectLeftClip.right,
            rectLeftClip.top + halfStrokeWidth,
            rectRightClip.left,
            rectLeftClip.top + halfStrokeWidth,
            paintContentRect
        )
        canvas.drawLine(
            rectLeftClip.right,
            rectLeftClip.bottom - halfStrokeWidth,
            rectRightClip.left,
            rectLeftClip.bottom - halfStrokeWidth,
            paintContentRect
        )

        rectCache.set(
            rectLeftClip.left,
            rectLeftClip.top,
            rectLeftClip.right - paintContentRect.strokeWidth,
            rectLeftClip.bottom
        )
        canvas.drawRect(
            rectCache.right - (paintContentRect.strokeWidth),
            rectCache.top,
            rectCache.right + (paintContentRect.strokeWidth),
            rectCache.bottom,
            paintContentRect
        )

        canvas.drawBitmap(bitmapClipLeft, null, rectCache, null)
        rectCache.set(
            rectRightClip.left + paintContentRect.strokeWidth,
            rectRightClip.top,
            rectRightClip.right,
            rectRightClip.bottom
        )
        canvas.drawRect(
            rectCache.left - (paintContentRect.strokeWidth),
            rectCache.top,
            rectCache.left + (paintContentRect.strokeWidth),
            rectCache.bottom,
            paintContentRect
        )
        canvas.drawBitmap(bitmapClipRight, null, rectCache, null)


    }

    private val progressLineStroke = 3.dp
    private val paintProgress by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }
    private val gestureDetector =
        GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {
            var starX = 0f
            var endX = 0f

            var minLength = 0f
            override fun onDown(e: MotionEvent): Boolean {
                minLength = minLengthP * rectContent.width()
                if (rectLeftClip.contains(e.x, e.y)) {
                    onTouchingListener?.invoke(true)
                    starX = rectLeftClip.left
                    endX = rectLeftClip.right
                    modeTouch = MODE_SCROLL_LEFT
                    return true
                }
                if (rectRightClip.contains(e.x, e.y)) {
                    onTouchingListener?.invoke(true)

                    starX = rectRightClip.left
                    endX = rectRightClip.right
                    modeTouch = MODE_SCROLL_RIGHT
                    return true
                }
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val moveX = e2.x - (e1?.x ?: 0f)
                if (modeTouch == MODE_SCROLL_LEFT) {
                    if (rectLeftClip.left - distanceX <= 0) {
                        rectLeftClip.set(
                            0f, rectRightClip.top, rectRightClip.width(),
                            rectRightClip.bottom
                        )
                    } else if (rectLeftClip.right - distanceX >= rectRightClip.left - minLength) {
                        rectLeftClip.set(
                            rectRightClip.left - minLength - rectRightClip.width(),
                            rectRightClip.top,
                            rectRightClip.left - minLength,
                            rectRightClip.bottom
                        )
                    } else {
                        rectLeftClip.offset(-distanceX, 0f)
                    }
                    val startP = (rectLeftClip.right - rectContent.left) / rectContent.width()
                    val endP = (rectRightClip.left - rectContent.left) / rectContent.width()
                    onLengthChangeListener?.invoke(startP, endP, true)
                    progress = startP
                    invalidate()
                }

                if (modeTouch == MODE_SCROLL_RIGHT) {
                    if (rectRightClip.left - distanceX <= rectLeftClip.right + minLength) {
                        rectRightClip.set(
                            rectLeftClip.right + minLength,
                            rectLeftClip.top,
                            rectLeftClip.right + minLength + rectLeftClip.width(),
                            rectLeftClip.bottom
                        )
                    } else if (rectRightClip.right - distanceX >= width) {
                        rectRightClip.set(
                            width - rectLeftClip.width(),
                            rectLeftClip.top,
                            width.toFloat(),
                            rectLeftClip.bottom
                        )
                    } else {
                        rectRightClip.offset(-distanceX, 0f)
                    }
                    onLengthChangeListener?.let {
                        val startP = (rectLeftClip.right - rectContent.left) / rectContent.width()
                        val endP = (rectRightClip.left - rectContent.left) / rectContent.width()
                        it.invoke(startP, endP, false)
                    }

                    invalidate()
                }

                return true
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            modeTouch = MODE_NONE
            onTouchingListener?.invoke(false)

        }
        return true
    }
}