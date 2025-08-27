package com.blinker.video.ui.pages.publish

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.blinker.video.databinding.ActivityLayoutPublishBinding
import com.blinker.video.http.ApiService
import com.blinker.video.model.TagList
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.FileUtil
import com.blinker.video.ui.utils.invokeViewBinding
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setVisibility
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
@Suppress("DEPRECATION")
@NavDestination(route = "activity_publish", type = NavDestination.NavType.Activity)
class PublishActivity : AppCompatActivity() {

    private var selectedTagList: TagList? = null
    private val viewBinding: ActivityLayoutPublishBinding by invokeViewBinding()
    private var width: Int = 0
    private var height: Int = 0
    private var filePath: String? = null
    private var mimeType: String? = null
    private var coverFileUploadUUID: UUID? = null
    private var originFileUploadUUID: UUID? = null
    private var coverFileUploadUrl: String? = null
    private var originFileUploadUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.actionAddTag.setOnClickListener {
            showBottomSheetDialog()
        }

        viewBinding.actionAddFile.setOnClickListener {
            CaptureActivity.startActivityForResult(this)
        }

        viewBinding.actionPublish.setOnClickListener {
            publish()
        }

        viewBinding.actionClose.setOnClickListener {
            finish()
        }
    }


    private fun publish() {
        if (TextUtils.isEmpty(viewBinding.inputView.text)) return
        viewBinding.actionPublish.setVisibility(false)
        viewBinding.actionPublishProgress.setVisibility(true)
        viewBinding.actionPublishProgress.show()

        lifecycleScope.launch {
            val workRequests = mutableListOf<OneTimeWorkRequest>()
            if (!TextUtils.isEmpty(filePath)) {
                if (MimeTypes.isVideo(mimeType)) {
                    // 提取视频的封面图
                    val coverFilePath = FileUtil.generateVideoCoverFile(filePath!!)
                    if (TextUtils.isEmpty(coverFilePath)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PublishActivity,
                                "生成封面图文件失败,请重试",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@withContext
                        }
                    }

                    val uploadCoverFileWorkRequest = getOneTimeWorkRequest(coverFilePath!!)
                    this@PublishActivity.coverFileUploadUUID = uploadCoverFileWorkRequest.id
                    workRequests.add(uploadCoverFileWorkRequest)
                }

                val originFileUploadWorkRequest = getOneTimeWorkRequest(filePath!!)
                this@PublishActivity.originFileUploadUUID = originFileUploadWorkRequest.id
                workRequests.add(originFileUploadWorkRequest)

                // 添加文件上传的任务到workManager的队列
                enqueue(workRequests)
            } else {
                publishFeed()
            }
        }
    }

    private fun enqueue(workRequests: MutableList<OneTimeWorkRequest>) {
        val workContinuation = WorkManager.getInstance(this).beginWith(workRequests)
        workContinuation.enqueue()

        workContinuation.workInfosLiveData.observe(this) { workInfos ->
            var failedCount = 0
            var completedCount = 0
            for (workInfo in workInfos) {
                val state = workInfo.state
                val outputData = workInfo.outputData

                val uuid = workInfo.id

                if (state == WorkInfo.State.FAILED) {
                    if (uuid == this.coverFileUploadUUID) {
                        Toast.makeText(this, "封面图上传失败", Toast.LENGTH_SHORT).show()
                    } else if (uuid == this.originFileUploadUUID) {
                        Toast.makeText(this, "原始文件上传失败", Toast.LENGTH_SHORT).show()
                    }
                    failedCount++
                } else if (state == WorkInfo.State.SUCCEEDED) {
                    val uploadUrl = outputData.getString("fileUrl")
                    Log.i("Publish", "enqueue:$uploadUrl ")
                    if (uuid == this.coverFileUploadUUID) {
                        this.coverFileUploadUrl = uploadUrl
                    } else if (uuid == this.originFileUploadUUID) {
                        this.originFileUploadUrl = uploadUrl
                    }
                    completedCount++
                }
                if (completedCount >= workRequests.size) {
                   publishFeed()
                } else if (failedCount > 0) {
                    recoverUIState()
                }
            }
        }
    }

    private fun recoverUIState() {
        viewBinding.actionPublish.setVisibility(true)
        viewBinding.actionPublishProgress.setVisibility(false)
        viewBinding.actionPublishProgress.hide()
    }

    private fun getOneTimeWorkRequest(filePath: String): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("file", filePath)
            .build()
        return OneTimeWorkRequest
            .Builder(UploadFileWorker::class.java)
            .setInputData(inputData)
//            .setConstraints(constraints)
//            //设置一个拦截器，在任务执行之前 可以做一次拦截，去修改入参的数据然后返回新的数据交由worker使用
//            .setInputMerger(null)
//            //当一个任务被调度失败后，所要采取的重试策略，可以通过BackoffPolicy来执行具体的策略
//            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//            //任务被调度执行的延迟时间
//            .setInitialDelay(10, TimeUnit.MILLISECONDS)
//            //设置该任务尝试执行的最大次数
//            .setInitialRunAttemptCount(2)
//            //指定该任务被调度的时间
//            .setScheduleRequestedAt(System.currentTimeMillis()+1000, TimeUnit.MILLISECONDS)
//            //当一个任务执行状态变成finish时，又没有后续的观察者来消费这个结果，难么workmanager会在
//            //内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
//            .keepResultsForAtLeast(10,TimeUnit.MILLISECONDS)
            .build()
    }


    private fun publishFeed() {
        lifecycleScope.launch {
            kotlin.runCatching {

                Log.i("Publish", "publishFeed:width $width, height $height, " +
                        "coverFileUpload:$coverFileUploadUrl," +
                        "tagId:${selectedTagList?.tagId},title:${selectedTagList?.title}," +
                        "title: ${viewBinding.inputView.text}" +
                        "userId:${UserManager.userId()}" +
                        "originFileUpload:$originFileUploadUrl")
                val apiResult = ApiService.getService().publishFeed(
                    coverFileUploadUrl,
                    originFileUploadUrl,
                    width,
                    height,
                    selectedTagList?.tagId ?: 0L,
                    selectedTagList?.title ?: "",
                    viewBinding.inputView.text.toString(),
                    UserManager.userId()
                )


                withContext(Dispatchers.Main) {
                    if (apiResult.success) {
                        Toast.makeText(this@PublishActivity, "帖子发布成功", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@PublishActivity, "帖子发布失败", Toast.LENGTH_SHORT).show()
                        recoverUIState()
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun showBottomSheetDialog() {
        val fragment = TagBottomSheetDialogFragment()
        fragment.setOnTagItemSelectedListener(object :
            TagBottomSheetDialogFragment.OnTagItemSelectedListener {
            override fun onTagItemSelected(tagList: TagList) {
                this@PublishActivity.selectedTagList = tagList
                viewBinding.actionAddTag.text = tagList.title
            }
        })
        fragment.show(supportFragmentManager, "tag_dialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQ_CAPTURE && data != null) {
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0)
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0)
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH)
            mimeType = data.getStringExtra(CaptureActivity.RESULT_FILE_TYPE)
            showFileThumbnail()
        }
    }

    private fun showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) return
        viewBinding.actionAddFile.setVisibility(false)
        viewBinding.fileContainer.setVisibility(true)
        viewBinding.cover.setImageUrl(filePath)
        viewBinding.videoIcon.setVisibility(MimeTypes.isVideo(mimeType))
        viewBinding.cover.setOnClickListener {
            PreviewActivity.startActivityForResult(this, filePath!!, true, null)
        }

        viewBinding.actionDeleteFile.setOnClickListener {
            viewBinding.actionAddFile.setVisibility(true)
            viewBinding.fileContainer.setVisibility(false)
            viewBinding.cover.setImageDrawable(null)
            filePath = null
            mimeType = null
            width = 0
            height = 0
        }
    }
}