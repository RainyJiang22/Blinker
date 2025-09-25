package com.blinker.video.ui.pages.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.blinker.video.model.Feed
import com.blinker.video.model.TYPE_IMAGE_TEXT
import com.blinker.video.model.TYPE_TEXT
import com.blinker.video.model.TYPE_VIDEO
import com.blinker.video.ui.utils.parcelable

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class FeedDetailActivity : AppCompatActivity() {

    companion object {
        private const val KEY_FEED = "key feed"
        const val KEY_CATEGORY = "key_category"
        fun startFeedDetailActivity(
            context: Activity, item: Feed,
            category: String,
            shareView: View,
        ) {
            val intent = Intent(context, FeedDetailActivity::class.java)
            intent.putExtra(KEY_FEED, item)
            intent.putExtra(KEY_CATEGORY, category)

            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context,
                    shareView,
                    "share_view"
                )
            ActivityCompat.startActivity(context, intent, optionsCompat.toBundle())
        }
    }

    private var viewHandler: ViewHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val feed: Feed? = intent.parcelable<Feed>(KEY_FEED)
        if (feed == null) {
            finish()
            return
        }

        viewHandler = when (feed.itemType) {
            TYPE_VIDEO -> {
                VideoViewHandler(this)
            }

            TYPE_TEXT -> {
                TextViewHandler(this)
            }

            TYPE_IMAGE_TEXT -> {
                ImageViewHandler(this)
            }

            else -> {
                TextViewHandler(this)
            }
        }
        viewHandler?.bindInitData(feed)
        setContentView(viewHandler?.getRootView())
    }
}