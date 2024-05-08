package com.sasirekha.resumableuploadwizard

import android.net.Uri
import androidx.work.Data
import com.google.gson.Gson
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import java.net.URL
import java.util.*

class UploadConfiguration (
    val chunkFirstByte: Long,
    val chunkSize: Int,
    val contentType: String,
    val dataLocation: Uri,
    val totalObjectSize: Long,
    val checksum: String,
    val sessionRequestUrl: URL,
    val sessionRequestAuthHeader: String,
    val sessionRequestAuthValue: String,
    val metadata: HashMap<String, String>?) {
    companion object {
        private val gson = Gson()
    }

    private fun getMetadataString(): String {
        return gson.toJson(metadata)
    }

    fun getInitialInputData(): Data {
        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, chunkSize.toString())
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, dataLocation.toString())
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, totalObjectSize.toString())
            .putString(UploadWorkerDataConstants.METADATA.name, getMetadataString())
            .putString(UploadWorkerDataConstants.CHECKSUM.name, checksum)
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_URL.name, sessionRequestUrl.toString())
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_HEADER.name, sessionRequestAuthHeader)
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_VALUE.name, sessionRequestAuthValue)
            .build()

        return inputData
    }

    //    val requestId: UUID
//    USERNAME
//    RECORD_NAME

}