package com.sasirekha.resumableuploadwizard.builders

import androidx.work.Data
import com.google.gson.Gson
import com.sasirekha.resumableuploadwizard.models.ObjectMetadata
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import junit.framework.Assert.assertEquals
import org.junit.Test

//{
//    "requestId": "8test2f9-e34f-47ea-9056-2d16test7d3e",
//    "username": "TEST",
//    "requestTimestamp": "1686016615080",
//    "recordName": "TEST_FILE.csv",
//    "contentType": "text/csv",
//    "contentLength": 40,
//    "checksum": "TestlAkR/ltestoMyC0bZA=="
//}

class SessionUrlRequestBuilderTest {
    @Test
    fun sessionUrlRequestBuilderTest() {
        val gson = Gson()
        val objectMetadata = ObjectMetadata("8test2f9-e34f-47ea-9056-2d16test7d3e",
            "TEST", 1686016615080L, "TEST_FILE.csv",
            "text/csv", 40, "TestlAkR/ltestoMyC0bZA==")
        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, "1024")
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, "testLocation")
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, "2000")
            .putString(UploadWorkerDataConstants.CHECKSUM.name, "TestChecksum==")
            .putString(UploadWorkerDataConstants.METADATA.name, gson.toJson(objectMetadata.getMap()))
            .build()

        val sessionUrlRequestBuilder = SessionUrlRequestBuilder(inputData, "testWorkId", null)
        val sessionUrlRequest = sessionUrlRequestBuilder.build()

        val validRequestBody = gson.toJson(objectMetadata.getMap()).toByteArray()

        assertEquals(false, sessionUrlRequest.isLastRequest)
        assertEquals("testWorkId", sessionUrlRequest.workId)
//        assertEquals(validRequestBody, sessionUrlRequest.binaryBody)
        assertEquals(null, sessionUrlRequest.network)
        assertEquals(arrayListOf(
            UploadWorkerDataConstants.DATA_LOCATION,
            UploadWorkerDataConstants.CHUNK_SIZE,
            UploadWorkerDataConstants.CONTENT_TYPE,
            UploadWorkerDataConstants.TOTAL_OBJECT_SIZE,
            UploadWorkerDataConstants.RECORD_NAME,
            UploadWorkerDataConstants.CHECKSUM
        ), sessionUrlRequest.carryOverDataList)
    }
}