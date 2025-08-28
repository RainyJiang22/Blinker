package com.blinker.video.ui.pages.publish

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.blinker.video.R
import com.blinker.video.ui.utils.FileUtil
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * @author jiangshiyu
 * @date 2025/8/28
 */
object UploadFileManager {
    suspend fun upload(
        context: Context,
        originalFilePath: String,
        mimeType: String,
        callback: (String?, String?) -> Unit,
    ) {
        val isVideo = MimeTypes.isVideo(mimeType)
        val workRequests = mutableListOf<OneTimeWorkRequest>()
        if (MimeTypes.isVideo(mimeType)) {
            // 提取视频的封面图
            val coverFilePath = FileUtil.generateVideoCoverFile(originalFilePath)
            if (TextUtils.isEmpty(coverFilePath)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.file_upload_generate_cover_fail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                callback(null, null)
                return
            }
            workRequests.add(getOneTimeWorkRequest(coverFilePath!!, isVideo))
        }
        workRequests.add(getOneTimeWorkRequest(originalFilePath, isVideo))
        enqueue(context, workRequests, callback)
    }

    private suspend fun enqueue(
        context: Context,
        workRequests: MutableList<OneTimeWorkRequest>,
        callback: (String?, String?) -> Unit,
    ) {
        val workContinuation = WorkManager.getInstance(context).beginWith(workRequests)
        workContinuation.enqueue()

        var coverFileUploadUrl: String? = null
        var originalFileUploadUrl: String? = null
        workContinuation.workInfosLiveData.asFlow().collectLatest { workInfos ->
            var failedCount = 0
            var completedCount = 0
            for (workInfo in workInfos) {
                val state = workInfo.state
                val outputData = workInfo.outputData
                val uuid = workInfo.id

                if (state == WorkInfo.State.FAILED) {
                    val coverFileUploadFail =
                        workRequests.size == 2 && uuid == workRequests.first().id
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            if (coverFileUploadFail) R.string.file_upload_cover_fail else R.string.file_upload_original_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    failedCount++
                } else if (state == WorkInfo.State.SUCCEEDED) {
                    val coverFileUploadSuccess =
                        workRequests.size == 2 && uuid == workRequests.first().id
                    val uploadUrl = outputData.getString("fileUrl")
                    if (coverFileUploadSuccess) {
                        coverFileUploadUrl = uploadUrl
                    } else {
                        originalFileUploadUrl = uploadUrl
                    }
                    completedCount++
                }
                if (completedCount + failedCount >= workRequests.size) {
                    callback(coverFileUploadUrl, originalFileUploadUrl)
                }
            }
        }
    }

    private fun getOneTimeWorkRequest(filePath: String, isVideo: Boolean): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("file", filePath)
            .putBoolean("video", isVideo)
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
}