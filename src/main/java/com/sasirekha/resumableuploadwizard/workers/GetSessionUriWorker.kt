package com.pdiot.resumableuploadmanager.workers

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.WorkerParameters
import com.pdiot.resumableuploadmanager.builders.SessionUrlRequestBuilder
import com.pdiot.resumableuploadmanager.builders.SessionUrlResponseBuilder
import com.pdiot.resumableuploadmanager.clients.CustomHttpClient
import com.pdiot.resumableuploadmanager.exceptions.InvalidHttpResponseException
import com.pdiot.resumableuploadmanager.exceptions.MissingInputDataException
import com.pdiot.resumableuploadmanager.exceptions.MissingWorkIdException
import com.pdiot.resumableuploadmanager.models.UploadWorkerDataConstants
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpRequest
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpResponse

class GetSessionUriWorker (private val context: Context, userParameters: WorkerParameters
) : ResumableUploadWorker (context, userParameters) {
    companion object {
        private const val TAG = "GetSessionUriW"
    }
    private val apiClient = CustomHttpClient.getClient();
    override val workId: String = getWorkIdFromTags();

    override fun getWorkIdFromTags(): String {
        var workId: String? = null
        for(tag in tags) {
            if(tag.startsWith(workNamePrefix)){
                workId = tag;
            }
        }
        if(workId == null) {
            throw MissingWorkIdException("WorkId not found in list of worker tags");
        }
        return workId;
    }

    override fun createRequest(): UploadWorkerHttpRequest {
        Log.d(TAG, "Creating Request")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.CHUNK_SIZE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.DATA_LOCATION.name)}")
        try {
            val postRequestBuilder = SessionUrlRequestBuilder(inputData, workId, network)

            Log.d(TAG, "Created Put File Chunk Request successfully!!!")

            return postRequestBuilder.build()
        } catch(e: Exception) {
            Log.e(TAG, "Request build failed")
            e.message?.let { Log.e(TAG, it) }
            e.printStackTrace()
        }

        throw MissingInputDataException("Unable to parse")
    }

    override fun createOutputData(
        request: UploadWorkerHttpRequest,
        response: UploadWorkerHttpResponse
    ): Data {
        if(response.isValidResponseBody()) {
            val carryOverData = request.getCarryOverData()

            return response.getOutputData(carryOverData)
        }

        throw InvalidHttpResponseException("Unable to parse response")
    }

    override fun doWork(): Result {
        return try {
            val postRequest: UploadWorkerHttpRequest = createRequest()
            val response =  apiClient.performPost<SessionUrlResponseBuilder>(postRequest, SessionUrlResponseBuilder::class.java)
            val outputData = createOutputData(postRequest, response)
            Result.success(outputData)
        } catch (throwable: Exception) {
            Log.e("BACKUP_WORKER", "Error uploading content: " + throwable.stackTrace)
            throwable.message?.let { Log.e("REQUEST_PARSING_MESSAGE", it) };
            throwable.printStackTrace()
            Result.failure()
        }
    }

}