package com.sasirekha.resumableuploadwizard.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpRequest
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants.*

abstract class ResumableUploadWorker (context: Context, userParameters: WorkerParameters) : Worker(context, userParameters) {
    companion object {
        const val workNamePrefix = "BackupFile|"
    }
    abstract val workId: String

    val sequenceNumber: Int = (
                ((inputData.getString(CHUNK_FIRST_BYTE.name)?.toLong()!!)
                /
                inputData.getString(CHUNK_SIZE.name)?.toInt()!!)
            ).toInt() + 1


    abstract fun getWorkIdFromTags(): String
    abstract fun createRequest(): UploadWorkerHttpRequest
    abstract fun createOutputData(
        request: UploadWorkerHttpRequest,
        response: UploadWorkerHttpResponse
    ): Data

}