package com.sasirekha.resumableuploadwizard.builders

import android.net.Network
import android.util.Log
import androidx.work.Data
import com.sasirekha.resumableuploadwizard.exceptions.RequestBuildingException
import com.sasirekha.resumableuploadwizard.models.ObjectMetadata
import com.sasirekha.resumableuploadwizard.models.SessionUrlRequest
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants.*
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpRequest
import java.util.*

class SessionUrlRequestBuilder(override val inputData: Data, override val workId: String, override val network: Network?): UploadWorkerHttpRequestBuilder(inputData, workId, network) {
    companion object {
        private const val TAG = "SessionUrlReqB"
    }
    override var requestBody: ByteArray? = null

    override fun build(): UploadWorkerHttpRequest {
        try {
            val requestId: String =
                getInputData(REQUEST_ID)!!;
            val recordName: String =
                getInputData(RECORD_NAME)!!;
            val username: String =
                getInputData(USERNAME)!!;
            val requestTimestamp: Long = Date().time;
            val contentType: String = getInputData(CONTENT_TYPE)!!
            val contentLength: Long = getInputData(TOTAL_OBJECT_SIZE)!!.toLong()
            requestBody = ObjectMetadata(
                requestId,
                username,
                requestTimestamp,
                recordName,
                contentType,
                contentLength
            ).toString().toByteArray()
            return SessionUrlRequest(inputData, workId, network, requestBody)
        } catch(e: Exception) {
            val msg = "Unable to build request because: ${e.message}"
            Log.e(TAG, msg)
            e.printStackTrace()
            throw RequestBuildingException(msg)
        }
    }
}