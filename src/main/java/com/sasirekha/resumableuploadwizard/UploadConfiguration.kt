package com.pdiot.resumableuploadwizard

import android.net.Uri
import androidx.work.Data
import com.google.gson.Gson
import com.pdiot.resumableuploadwizard.models.UploadWorkerDataConstants
import java.util.*

class UploadConfiguration (
    val chunkFirstByte: Long,
    val chunkSize: Int,
    val contentType: String,
    val dataLocation: Uri,
    val totalObjectSize: Long,
    val checksum: String,
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
            .build()

        return inputData
    }

    //    val requestId: UUID
//    USERNAME
//    RECORD_NAME

}