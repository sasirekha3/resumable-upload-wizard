package com.pdiot.resumableuploadwizard.builders

import android.net.Network
import android.util.Log
import androidx.work.Data
import com.pdiot.resumableuploadwizard.exceptions.RequestBuildingException
import com.pdiot.resumableuploadwizard.models.ResumableUploadRequest
import com.pdiot.resumableuploadwizard.models.UploadWorkerHttpRequest

class ResumableUploadRequestBuilder (override val inputData: Data, override val workId: String, override val network: Network?):
    UploadWorkerHttpRequestBuilder(inputData, workId, network) {
    companion object {
        private const val TAG = "ResumableUploadReqB"
    }
    override var requestBody: ByteArray? = null

    override fun build(): UploadWorkerHttpRequest {
        Log.d(TAG, "requestBody: ${requestBody}")
        try {
            return ResumableUploadRequest(inputData, workId, network, requestBody)
        } catch(e: Exception) {
            val msg = "Unable to build request because: ${e.message}"
            Log.e(TAG, msg)
            e.printStackTrace()
            throw RequestBuildingException(msg)
        }
    }
}