package com.blinker.video.ui.pages.tags

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * @author jiangshiyu
 * @date 2025/9/23
 */


@Composable
fun GlideImage(
    url: String,
    palette: Boolean = false,
    overSize: Int = 400,
    contentScale: ContentScale = ContentScale.FillHeight,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val defaultColor = LightGray.copy(alpha = 0.3f)
    var bitmapState by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var colorState by remember { mutableStateOf(defaultColor) }

    DisposableEffect(url) {
        val target = GlideImageTarget {
            bitmapState = it?.asImageBitmap()
            if (palette && it != null) {
                val paletteColor =
                    Palette.from(it).generate().getDarkVibrantColor(defaultColor.toArgb())
                colorState = Color(paletteColor)
            }
        }
        Glide.with(context).asBitmap().load(url).override(overSize).diskCacheStrategy(
            DiskCacheStrategy.ALL
        ).into(target)
        onDispose {
            runCatching {
                Glide.with(context).clear(target)
            }
        }
    }
    Surface(color = colorState, modifier = modifier) {
        bitmapState?.let {
            Image(
                it,
                contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}

private class GlideImageTarget(private val callback: (Bitmap?) -> Unit) : CustomTarget<Bitmap>() {
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        callback(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        callback(null)
    }

}