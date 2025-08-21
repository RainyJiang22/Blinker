package com.blinker.video.ui.pages.publish

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blinker.video.R
import com.blinker.video.databinding.ActivityLayoutCaptureBinding
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.utils.invokeViewBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
@SuppressLint("RestrictedApi")
@NavDestination(route = "activity_capture", type = NavDestination.NavType.Activity)
class CaptureActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private val viewBinding: ActivityLayoutCaptureBinding by invokeViewBinding()

    companion object {
        private const val TAG = "CaptureActivity"

        // 动态权限申请
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) Manifest.permission.WRITE_EXTERNAL_STORAGE else null
        ).filterNotNull().toTypedArray()

        // spring 动画参数配置
        private const val SPRING_STIFENESS_ALPHA_OUT = 100f
        private const val SPRING_STIFFNESS = 800f
        private const val SPRING_DAMPING_RATIO = 0.35f

        // 图片/视频文件名称，存放位置
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-sss"
        private const val PHOTO_TYPE = "image/jpeg"
        private const val VIDEO_TYPE = "video/mp4"
        private const val RELATIVE_PATH_PICTURE = "Pictures/Jetpack"
        private const val RELATIVE_PATH_VIDEO = "Movies/Jetpack"

        // request code
        private const val REQ_CAPTURE = 10001
        private const val PERMISSION_CODE = 1000

        // output file information
        private const val RESULT_FILE_PATH = "file_path"
        private const val RESULT_FILE_HEIGHT = "file_height"
        private const val RESULT_FILE_WIDTH = "file_width"
        private const val RESULT_FILE_TYPE = "file_type"

        // exported function used by publishActivity
        fun startActivityForResult(activity: Activity) {
            val intent = Intent(activity, CaptureActivity::class.java)
            activity.startActivityForResult(intent, REQ_CAPTURE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            val deniedPermissions = mutableListOf<String>()
            for (i in permissions.indices) {
                val permission = permissions[i]
                val result = grantResults[i]
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission)
                }
            }
            if (deniedPermissions.isEmpty()) {
                startCamera()
            } else {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.capture_permission_message))
                    .setNegativeButton(getString(R.string.capture_permission_no)) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.setPositiveButton(getString(R.string.capture_permission_ok)) { dialog, _ ->
                        ActivityCompat.requestPermissions(
                            this, deniedPermissions.toTypedArray(),
                            PERMISSION_CODE
                        )
                        dialog.dismiss()
                    }.create().show()

            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val lenFacing = when {
                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> throw IllegalStateException("Back and Front camera are unavailable")
            }

            // preview usecase
            val displayRotation = viewBinding.previewView.display.rotation
            val preview = Preview.Builder()
                .setCameraSelector(lenFacing)
                .setTargetRotation(displayRotation)
                .build().also {
                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
                }

            // imageCapture 图片拍摄
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(displayRotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // 设置期望的宽高比 16:9 ,4:3
                //.setTargetAspectRatio()
                // 设置期望的图片质量0-100
                .setJpegQuality(100)
                // 设置期望的的最大的分辨率，拍摄出来的图片分辨率不会高于1080,1920
                // 和setTargetAspectRatio不能同时设置，只能二选一
                .setResolutionSelector(
                    ResolutionSelector.Builder().setMaxResolution(Size(1920, 1080)).build()
                )
                .build()
            this.imageCapture = imageCapture


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, lenFacing, preview, imageCapture)
                bindUI()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindUI() {
        viewBinding.recordView.setOnClickListener {
            takePicture()
        }
    }

    private fun takePicture() {
        val vibrator = getSystemService(Vibrator::class.java) as Vibrator
        vibrator.vibrate(200)

        val fileName = SimpleDateFormat(FILENAME, Locale.CHINA).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(
                MediaStore.MediaColumns.MIME_TYPE,
                PHOTO_TYPE
            )
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    RELATIVE_PATH_PICTURE
                )
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    Log.d(TAG, "onImageSaved: capture success:${savedUri}")
                    onFileSaved(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CaptureActivity, exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun onFileSaved(savedUri: Uri?) {

    }

}