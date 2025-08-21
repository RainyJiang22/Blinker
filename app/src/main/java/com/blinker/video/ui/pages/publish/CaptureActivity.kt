package com.blinker.video.ui.pages.publish

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.Camera
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.lifecycleScope
import com.blinker.video.R
import com.blinker.video.databinding.ActivityLayoutCaptureBinding
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.utils.invokeViewBinding
import com.blinker.video.ui.utils.setVisibility
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
@SuppressLint("RestrictedApi")
@NavDestination(route = "activity_capture", type = NavDestination.NavType.Activity)
class CaptureActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private var videoCapture: VideoCapture<Recorder>? = null
    private lateinit var camera: Camera
    private var videoRecording: Recording? = null
    private val viewBinding: ActivityLayoutCaptureBinding by invokeViewBinding()
    private var outputFilePath: String? = null
    private var outputFileWidth: Int = 0
    private var outputFileHeight: Int = 0
    private var outputFileMimeType: String? = null

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
        private const val RELATIVE_PATH_PICTURE = "Pictures/Blinker"
        private const val RELATIVE_PATH_VIDEO = "Movies/Blinker"

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
            val cameraSelector = when {
                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> throw IllegalStateException("Back and Front camera are unavailable")
            }

            // preview usecase
            val displayRotation = viewBinding.previewView.display.rotation
            val preview = Preview.Builder()
                .setCameraSelector(cameraSelector)
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

            val usecase = mutableListOf(preview, imageCapture)
            // 构建recorder用于视频录制的对象
            if (isSupportCombinedUsages(cameraSelector, cameraProvider)) {
                val recorder =
                    Recorder.Builder()
                        .setQualitySelector(getQualitySelector(cameraProvider, cameraSelector))
                        .build()
                // 构建出VideoCapture对象
                val videoCapture = VideoCapture.withOutput(recorder)
                this.videoCapture = videoCapture
                usecase.add(videoCapture)
            }

            try {
                cameraProvider.unbindAll()
                this.camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    *usecase.toTypedArray()
                )
                bindUI()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun isSupportCombinedUsages(
        cameraSelector: CameraSelector,
        cameraProvider: CameraProvider,
    ): Boolean {
        val level = cameraSelector.filter(cameraProvider.availableCameraInfos)
            .firstOrNull()?.let { Camera2CameraInfo.from(it) }
            ?.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            ?: return false
        return level >= CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
    }

    private fun getQualitySelector(
        cameraProvider: CameraProvider,
        cameraSelector: CameraSelector,
    ): QualitySelector {

        val cameraInfo = cameraProvider.availableCameraInfos.filter {
            it.lensFacing == cameraSelector.lensFacing
        }

        val supportQualities = QualitySelector.getSupportedQualities(cameraInfo[0]).filter {
            listOf(Quality.FHD, Quality.HD, Quality.SD).contains(it)
        }
        return QualitySelector.from(supportQualities[0])
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindUI() {
        viewBinding.recordView.setOnClickListener {
            takePicture()
        }

        viewBinding.recordView.setOnLongClickListener {
            captureVideo()
            true
        }
        videoCapture?.run {
            viewBinding.captureTips.setText(R.string.capture_tips)
            viewBinding.recordView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP && videoRecording?.isClosed == false) {
                    videoRecording?.stop()
                }
                false
            }

            viewBinding.previewView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val meteringPointFactory = viewBinding.previewView.meteringPointFactory
                    val point = meteringPointFactory.createPoint(event.x, event.y)
                    val focusAction =
                        FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build()
                    this@CaptureActivity.camera.cameraControl.startFocusAndMetering(focusAction)
                    showFocusPoint(event.x, event.y)
                }
                true
            }
        }
    }

    private fun showFocusPoint(x: Float, y: Float) {
        val focusView = viewBinding.focusPoint
        val alphaAnim = SpringAnimation(focusView, DynamicAnimation.ALPHA, 1f).apply {
            spring.stiffness =
                SPRING_STIFFNESS
            spring.dampingRatio =
                SPRING_DAMPING_RATIO
            addEndListener { _, _, _, _ ->
                SpringAnimation(focusView, DynamicAnimation.ALPHA, 0f).apply {
                    spring.stiffness =
                        SPRING_STIFENESS_ALPHA_OUT
                    spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                }.start()
            }
        }

        val scaleXAnim = SpringAnimation(focusView, DynamicAnimation.SCALE_X, 1f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING_RATIO
        }
        val scaleYAnim = SpringAnimation(focusView, DynamicAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING_RATIO
        }

        focusView.setVisibility(true)
        focusView.translationX = x - focusView.width / 2
        focusView.translationY = y - focusView.height / 2
        focusView.alpha = 0f
        focusView.scaleX = 1.5f
        focusView.scaleY = 1.5f

        alphaAnim.start()
        scaleXAnim.start()
        scaleYAnim.start()
    }


    /**
     * 录制视频
     */
    private fun captureVideo() {
        val vibrator = getSystemService(Vibrator::class.java) as Vibrator
        vibrator.vibrate(200)
        viewBinding.captureTips.setVisibility(true)
        viewBinding.recordView.scaleY = 1.2f
        viewBinding.recordView.scaleX = 1.2f

        val fileName = SimpleDateFormat(FILENAME,Locale.CHINA).format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, VIDEO_TYPE)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH_VIDEO)
            }
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .setDurationLimitMillis(10 * 1000)
            .build()

        videoRecording = videoCapture!!.output.prepareRecording(this, outputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@CaptureActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }.start(ContextCompat.getMainExecutor(this)) {
                when (it) {
                    is VideoRecordEvent.Start -> {
                        // 开始视频录制
                        viewBinding.captureTips.setText(R.string.capture_tips_stop_recording)
                    }
                    is VideoRecordEvent.Status -> {
                        // 录制中，录制时长，文件体积等信息
                        val recordedMills =
                            TimeUnit.NANOSECONDS.toMillis(it.recordingStats.recordedDurationNanos)
                        viewBinding.recordView.progress =
                            (recordedMills * 1.0f / (10 * 1000) * 100).roundToInt()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!it.hasError()) {
                            val savedUri = it.outputResults.outputUri
                            onFileSaved(savedUri)
                            Log.e(TAG, "captureVideo success:${savedUri}")
                        } else {
                            // 录制视频失败
                            videoRecording?.close()
                            videoRecording = null
                        }
                        viewBinding.recordView.scaleY = 1.0f
                        viewBinding.recordView.scaleX = 1.0f
                        viewBinding.recordView.progress = 0
                        viewBinding.captureTips.setText(R.string.capture_tips)
                    }
                }
            }



    }

    /**
     * 拍摄图片
     */
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
                    val savedUri = outputFileResults.savedUri ?: return
                    Log.d(TAG, "onImageSaved: capture success:${savedUri}")
                    onFileSaved(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CaptureActivity, exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun onFileSaved(savedUri: Uri) {
        // 1.取出录制视频或图片的宽高，以及本地路径
        lifecycleScope.launch {
            val cursor = contentResolver.query(
                savedUri,
                arrayOf(
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.WIDTH,
                    MediaStore.MediaColumns.HEIGHT,
                    MediaStore.MediaColumns.MIME_TYPE
                ),
                null,
                null, null
            ) ?: return@launch
            cursor.moveToFirst()

            this@CaptureActivity.outputFilePath =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            this@CaptureActivity.outputFileMimeType =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            this@CaptureActivity.outputFileWidth =
                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
            this@CaptureActivity.outputFileHeight =
                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
            cursor.close()

            MediaScannerConnection.scanFile(
                this@CaptureActivity,
                arrayOf(outputFilePath),
                arrayOf(outputFileMimeType),
                null
            )

            withContext(Dispatchers.Main) {
                // 2. 跳转到预览页面，进行效果预览
                val video = MimeTypes.isVideo(this@CaptureActivity.outputFileMimeType)
                PreviewActivity.startActivityForResult(
                    this@CaptureActivity,
                    this@CaptureActivity.outputFilePath!!,
                    video,
                    getString(R.string.preview_ok)
                )
            }
        }
    }

}