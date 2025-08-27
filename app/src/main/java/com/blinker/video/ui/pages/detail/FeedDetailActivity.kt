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

/**
 * @author jiangshiyu
 * @date 2025/8/27
 */
class FeedDetailActivity : AppCompatActivity() {

    companion object {
        private const val KEY_FEED = "key feed"
        private const val KEY_CATEGORY = "key_category"
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
        val feed: Feed? = intent.getParcelableExtra(KEY_FEED) as? Feed
        if (feed == null) {
            finish()
            return
        }
//        viewHandler = if (feed.itemType == TYPE_IMAGE_TEXT) {
//            ImageViewHandler(this)
//        } else {
//            VideoViewHandler(this)
//        }
        viewHandler = ImageViewHandler(this)
        viewHandler?.bindInitData(feed)
        setContentView(viewHandler?.getRootView())
    }
}