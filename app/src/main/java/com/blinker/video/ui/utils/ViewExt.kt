package com.blinker.video.ui.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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