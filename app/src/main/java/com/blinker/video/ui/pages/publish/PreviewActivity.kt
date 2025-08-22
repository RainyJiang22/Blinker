package com.blinker.video.ui.pages.publish

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blinker.video.R
import com.blinker.video.databinding.ActivityLayoutPreviewBinding
import com.blinker.video.databinding.ActivityLayoutPublishBinding
import com.blinker.video.exoplayer.PageListPlayer
import com.blinker.video.exoplayer.WrapperPlayerView
import com.blinker.video.ui.utils.invokeViewBinding
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setVisibility
import androidx.core.net.toUri
import kotlin.text.get

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
class PreviewActivity : AppCompatActivity() {

    companion object {
        private const val PAGE_NAME = "Preview"
        private const val KEY_PREVIEW_URL = "preview_url"
        private const val KEY_PREVIEW_VIDEO = "preview_video"
        private const val KEY_PREVIEW_BTN_TEXT = "preview_btn_text"
        const val REQ_PREVIEW = 1000

        fun startActivityForResult(
            activity: Activity,
            previewUrl: String,
            isVideo: Boolean,
            btnText: String?,
        ) {
            val intent = Intent(activity, PreviewActivity::class.java)
            intent.putExtra(KEY_PREVIEW_URL, previewUrl)
            intent.putExtra(KEY_PREVIEW_VIDEO, isVideo)
            intent.putExtra(KEY_PREVIEW_BTN_TEXT, btnText)
            activity.startActivityForResult(intent, REQ_PREVIEW)
            activity.overridePendingTransition(0, 0)
        }
    }

    private val viewBinding: ActivityLayoutPreviewBinding by invokeViewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        requestPermission()
    }

    private fun onGrantPermission() {
        val previewUrl: String = intent.getStringExtra(KEY_PREVIEW_URL)
            ?: return finish()
        val isVideo: Boolean = intent.getBooleanExtra(KEY_PREVIEW_VIDEO, false)
        val btnText: String? = intent.getStringExtra(KEY_PREVIEW_BTN_TEXT)
        if (TextUtils.isEmpty(btnText)) {
            viewBinding.actionOk.setVisibility(false)
        } else {
            viewBinding.actionOk.setVisibility(true)
            viewBinding.actionOk.text = btnText
            viewBinding.actionOk.setOnClickListener {
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
        viewBinding.actionClose.setOnClickListener {
            finish()
        }

        if (isVideo) {
            previewVideo(previewUrl)
        } else {
            previewImage(previewUrl)
        }
    }

    private fun previewImage(imageUrl: String) {
        viewBinding.photoView.setVisibility(true)
        viewBinding.photoView.setImageUrl(imageUrl)
    }

    private fun previewVideo(videoUrl: String) {
        val player = PageListPlayer.get(PAGE_NAME)
        viewBinding.playerView.setVisibility(true)
        viewBinding.playerView.setListener(object : WrapperPlayerView.Listener {
            override fun onTogglePlay(attachView: WrapperPlayerView) {
                player.togglePlay(attachView, videoUrl)
            }
        })
        player.togglePlay(viewBinding.playerView, videoUrl)
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PERMISSION_GRANTED)
        ) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQ_PREVIEW)
        } else {
            onGrantPermission()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PREVIEW) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                onGrantPermission()
            } else {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (showRationale) {
                    showNoAccess()
                } else {
                    goToSettings()
                }
            }
        }
    }
    private fun goToSettings() {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()).apply {
            this.addCategory(Intent.CATEGORY_DEFAULT)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    private fun showNoAccess() {
        AlertDialog.Builder(this).setTitle(R.string.preview_permission_message).setPositiveButton(
            R.string.capture_permission_no
        ) { _, _ ->
            finish()
        }.setNegativeButton(R.string.capture_permission_ok) { _, _ ->
            requestPermission()
        }.create().show()
    }

    override fun onPause() {
        super.onPause()
        PageListPlayer.get(pageName = PAGE_NAME).inActive()
    }

    override fun onResume() {
        super.onResume()
        PageListPlayer.get(pageName = PAGE_NAME).onActive()
    }

    override fun onDestroy() {
        super.onDestroy()
        PageListPlayer.stop(pageName = PAGE_NAME)
    }


}