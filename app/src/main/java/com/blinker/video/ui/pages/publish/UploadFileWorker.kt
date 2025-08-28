package com.blinker.video.ui.pages.publish

import android.content.Context
import android.text.TextUtils
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.blinker.video.ui.utils.AliyunOssUtil

/**
 * @author jiangshiyu
 * @date 2025/8/22
 */
class UploadFileWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val filePath = inputData.getString("file")
        val isVideo = inputData.getBoolean("video", false)
        return if (TextUtils.isEmpty(filePath)) {
            return Result.failure()
        } else {
            val fileUrl = AliyunOssUtil.getManager().uploadFeedFiles(filePath, isVideo)
            val outputData = Data.Builder().putString("fileUrl", fileUrl).build()
            Result.success(outputData)
        }
    }
}