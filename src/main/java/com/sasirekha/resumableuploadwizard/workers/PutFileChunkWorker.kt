package com.sasirekha.resumableuploadwizard.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.WorkerParameters
import com.sasirekha.resumableuploadwizard.builders.FinalUploadResponseBuilder
import com.sasirekha.resumableuploadwizard.builders.ResumableUploadRequestBuilder
import com.sasirekha.resumableuploadwizard.builders.ResumableUploadResponseBuilder
import com.sasirekha.resumableuploadwizard.clients.CustomHttpClient
import com.sasirekha.resumableuploadwizard.clients.LocalFileReader
import com.sasirekha.resumableuploadwizard.exceptions.InvalidFileException
import com.sasirekha.resumableuploadwizard.exceptions.InvalidHttpResponseException
import com.sasirekha.resumableuploadwizard.exceptions.MissingInputDataException
import com.sasirekha.resumableuploadwizard.exceptions.MissingWorkIdException
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpRequest
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse
import java.util.concurrent.ConcurrentHashMap


class PutFileChunkWorker (val context: Context, userParameters: WorkerParameters
): ResumableUploadWorker(context, userParameters) {
    companion object {
        private const val TAG = "PutFileChunkW"
        private val readerMap = ConcurrentHashMap<String, LocalFileReader>()
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

    override fun doWork(): Result {
        return try {
            val putRequest: UploadWorkerHttpRequest = createRequest()
            val response = if(putRequest.isLastRequest) {
                apiClient.performPut<FinalUploadResponseBuilder>(putRequest, FinalUploadResponseBuilder::class.java)
            } else {
                apiClient.performPut<ResumableUploadResponseBuilder>(putRequest, ResumableUploadResponseBuilder::class.java)
            }
            val outputData = createOutputData(putRequest, response)
            Result.success(outputData)
        } catch (throwable: Exception) {
            Log.e("BACKUP_WORKER", "Error uploading content: " + throwable.stackTrace)
            throwable.message?.let { Log.e("REQUEST_PARSING_MESSAGE", it) };
            throwable.printStackTrace()

            // Remove the corresponding file reader
            removeCurrentFileReader()

            // Notify the user of failure
            ProgressNotification.notifyWorkCompletion(context, inputData.getString(
                UploadWorkerDataConstants.RECORD_NAME.name)!!, false);
            Result.failure()
        }
    }

    private fun getDataBinary(dataLocation: String, chunkSize: Int, totalObjectSize: Long, chunkFirstByte: Long): ByteArray {
        var uri: Uri? = null
        val localFileReader: LocalFileReader?
        try {
            uri = Uri.parse(dataLocation)

            // If this is the first time this is being called for this workId, instantiate localFileReader in readerMap
            if (readerMap.containsKey(workId)) localFileReader = readerMap[workId]
            else {
                localFileReader = LocalFileReader()
                localFileReader.open(context, uri, chunkSize, totalObjectSize);

                readerMap[workId] = localFileReader
            }
        } catch(e: Exception) {
            throw InvalidFileException("Selected file is not a content uri: ${uri}")
        }
        return localFileReader!!.getBytes(chunkFirstByte)
    }

    override fun createOutputData(
        request: UploadWorkerHttpRequest,
        response: UploadWorkerHttpResponse
    ): Data {

        // Add next chunk byte if response is successful
        if(response.isValidResponseBody()){
            val carryOverData = request.getCarryOverData()
            val outputData = response.getOutputData(carryOverData)

            if(request.isLastRequest){
                // If the last request was performed successfully with a valid response,
                // Notify the user of completion
                ProgressNotification.notifyWorkCompletion(context, request.getInputString(
                    UploadWorkerDataConstants.RECORD_NAME
                )!!, true)

                // Remove the corresponding file reader
                removeCurrentFileReader()

                Log.d(TAG, "UPLOAD COMPLETED SUCCESSFULLY!!!")
            }
            return outputData;
        }

        // Notify the user of failure
        val msg = "Invalid or error response (code: ${response.code}) received: ${response.bodyString}"
        Log.e(TAG, msg);

        throw InvalidHttpResponseException(msg)
    }

    private fun removeCurrentFileReader() {
        try {
            var localFileReader: LocalFileReader? = null;
            if (readerMap.containsKey(workId)) localFileReader = readerMap.get(workId)
            localFileReader?.close();
            readerMap.remove(workId);
        } catch(ex: Exception) {
            // Do Nothing
        }
    }

    override fun createRequest(): UploadWorkerHttpRequest {
        Log.d(TAG, "Creating Request")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.CHUNK_SIZE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.DATA_LOCATION.name)}")
        Log.d(TAG, "${inputData.getString(UploadWorkerDataConstants.SESSION_URI.name)}")
        try {
            val putRequestBuilder = ResumableUploadRequestBuilder(inputData, workId, network)

            // set request body
            putRequestBuilder.requestBody = getDataBinary(
                putRequestBuilder.getInputData(UploadWorkerDataConstants.DATA_LOCATION)!!,
                putRequestBuilder.getInputData(UploadWorkerDataConstants.CHUNK_SIZE)!!.toInt(),
                putRequestBuilder.getInputData(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE)!!.toLong(),
                putRequestBuilder.getInputData(UploadWorkerDataConstants.CHUNK_FIRST_BYTE)!!.toLong()
            )

            Log.d(TAG, "Created Put File Chunk Request successfully!!!")

            return putRequestBuilder.build()
        } catch(e: Exception) {
            Log.e(TAG, "Request build failed because: ${e.message}")
            e.printStackTrace()
        }

        throw MissingInputDataException("Unable to parse")
    }


}