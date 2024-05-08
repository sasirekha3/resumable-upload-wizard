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
            requestBody = getInputData(METADATA)!!.toByteArray()
            return SessionUrlRequest(inputData, workId, network, requestBody)
        } catch(e: Exception) {
            val msg = "Unable to build request because: ${e.message}"
            Log.e(TAG, msg)
            e.printStackTrace()
            throw RequestBuildingException(msg)
        }
    }
}