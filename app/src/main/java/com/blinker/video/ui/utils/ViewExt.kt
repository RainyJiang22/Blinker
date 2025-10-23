package com.blinker.video.ui.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FloatRange
import com.blinker.video.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.BitmapThumbnailImageViewTarget
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.DrawableThumbnailImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import androidx.core.graphics.createBitmap

/**
 * @author jiangshiyu
 * @date 2024/12/17
 */
fun View.setVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun TextView.setTextColor(condition: Boolean, trueRes: Int, falseRes: Int) {
    this.setTextColor(context.getColor(if (condition) trueRes else falseRes))
}

fun TextView.setTextVisibility(content: String?, goneWhenNull: Boolean = true) {
    if (TextUtils.isEmpty(content) && goneWhenNull) {
        visibility = View.GONE
        return
    }
    visibility = View.VISIBLE
    text = content
}

fun ImageView.setImageResource(condition: Boolean, trueRes: Int, falseRes: Int) {
    setImageResource(if (condition) trueRes else falseRes)
}

fun MaterialButton.setMaterialButton(
    content: String?,
    condition: Boolean,
    trueRes: Int,
    falseRes: Int,
) {
    if (!TextUtils.isEmpty(content)) {
        text = content
    }
    setIconResource(if (condition) trueRes else falseRes)
    val likeStateColor =
        ColorStateList.valueOf(context.getColor(if (condition) R.color.color_theme else R.color.color_3d3))
    iconTint = likeStateColor
    setTextColor(likeStateColor)
}

@SuppressLint("CheckResult")
fun ImageView.setImageUrl(imageUrl: String?, isCircle: Boolean = false, radius: Int = 0) {
    if (TextUtils.isEmpty(imageUrl)) {
        visibility = View.GONE
        return
    }
    visibility = View.VISIBLE
    val builder = Glide.with(this).load(imageUrl)
        .override(measuredWidth,measuredHeight)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    if (isCircle) {
        builder.transform(CircleCrop())
    } else if (radius > 0) {
        builder.transform(RoundedCornersTransformation(PixUtil.dp2px(radius), 0))
    }
    val layoutParams = this.layoutParams
    if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
        builder.override(layoutParams.width, layoutParams.height)
    }
    builder.into(this)
}

fun ImageView.load(imageUrl: String, callback: (Bitmap) -> Unit) {
    Glide.with(this).asBitmap().load(imageUrl)
        .override(measuredWidth,measuredHeight)
        .into(object : BitmapImageViewTarget(this) {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            super.onResourceReady(resource, transition)
            callback(resource)
        }
    })
}

fun ImageView.setBlurImageUrl(blurUrl: String, radius: Int) {
    Glide.with(this).load(blurUrl)
        .override(measuredWidth,measuredHeight)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .override(radius)
        .transform(BlurTransformation()).dontAnimate().into(object : DrawableImageViewTarget(this) {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                super.onResourceReady(resource, transition)
                background = resource
            }
        })
}

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun Bitmap.alphaAndCorner(
    @FloatRange(from = 0.0, to = 1.0) alpha: Float,
    targetW: Int,
    targetH: Int,
    radius: Float = 15f.dp,
): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.xfermode = null
    val outputBitmap = createBitmap(targetW, targetH)
    val canvas = Canvas(outputBitmap)
    val targetRectF = RectF(0f, 0f, targetW.toFloat(), targetH.toFloat())
    val srcRect = Rect()

    val targetScale = targetW.toFloat() / targetH
    val srcScale = width.toFloat() / height.toFloat()
    if (targetScale > srcScale) {
        val h = (width.toFloat() / targetScale).toInt()
        srcRect.set(0, 0, width, h)
        srcRect.offset(0, height / 2 - srcRect.height() / 2)
    } else {
        val w = (height.toFloat() * targetScale).toInt()
        srcRect.set(0, 0, w, height)
        srcRect.offset(width / 2 - srcRect.width() / 2, 0)
    }
    if (radius != 0f) {
        paint.color = Color.BLACK
        canvas.drawRoundRect(targetRectF, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
    paint.alpha = (alpha * 255).toInt()
    canvas.drawBitmap(this, srcRect, targetRectF, paint)
    paint.alpha = 255
    return outputBitmap
}

/**
 * 将 Bitmap 以 centerCrop 方式绘制到指定 Rect
 *
 * @param canvas 目标画布
 * @param bitmap 源 Bitmap
 * @param destRect 目标绘制区域
 */
fun drawBitmapCenterCrop(canvas: Canvas, bitmap: Bitmap, destRect: RectF) {
    // 计算源 Bitmap 和目标 Rect 的宽高比
    val srcWidth = bitmap.width.toFloat()
    val srcHeight = bitmap.height.toFloat()
    val destWidth = destRect.width()
    val destHeight = destRect.height()

    val srcRatio = srcWidth / srcHeight
    val destRatio = destWidth / destHeight

    // 创建变换矩阵
    val matrix = Matrix()

    // 根据宽高比决定缩放方式
    if (srcRatio > destRatio) {
        // 源图像更宽，需要缩放高度以匹配目标高度，然后裁剪宽度
        val scale = destHeight / srcHeight
        matrix.postScale(scale, scale)

        // 计算缩放后的宽度
        val scaledWidth = srcWidth * scale

        // 计算水平偏移量以居中裁剪
        val dx = (scaledWidth - destWidth) / -2
        matrix.postTranslate(dx, 0f)
    } else {
        // 源图像更高，需要缩放宽度以匹配目标宽度，然后裁剪高度
        val scale = destWidth / srcWidth
        matrix.postScale(scale, scale)

        // 计算缩放后的高度
        val scaledHeight = srcHeight * scale

        // 计算垂直偏移量以居中裁剪
        val dy = (scaledHeight - destHeight) / -2
        matrix.postTranslate(0f, dy)
    }

    // 绘制 Bitmap
    canvas.drawBitmap(bitmap, matrix, null)
}