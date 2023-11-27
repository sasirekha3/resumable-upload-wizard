package com.sasirekha.resumableuploadwizard.builders

import androidx.work.Data
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import org.junit.Test
import junit.framework.TestCase.assertEquals

class ResumableUploadRequestBuilderTest {

    @Test
    fun resumableUploadRequestBuilderTest() {
        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, "1024")
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, "testLocation")
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, "2000")
            .putString(UploadWorkerDataConstants.SESSION_URI.name, "https://example.com")
            .putString(UploadWorkerDataConstants.CHECKSUM.name, "TestChecksum==")
            .build()
        val requestBuilder = ResumableUploadRequestBuilder(inputData, "testWorkId", null)
        val requestBody = "testBody".toByteArray()
        requestBuilder.requestBody = requestBody
        val request = requestBuilder.build()

        assertEquals(requestBody, request.binaryBody)
        assertEquals(false, request.isLastRequest)
        assertEquals("testWorkId", request.workId)
        assertEquals(null, request.network)

    }

    @Test
    fun lastResumableUploadRequestBuilderTest() {
        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "1024")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, "1024")
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, "testLocation")
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, "2000")
            .putString(UploadWorkerDataConstants.SESSION_URI.name, "https://example.com")
            .putString(UploadWorkerDataConstants.CHECKSUM.name, "TestChecksum==")
            .build()
        val requestBuilder = ResumableUploadRequestBuilder(inputData, "testWorkId", null)
        val requestBody = "testBody".toByteArray()
        requestBuilder.requestBody = requestBody
        val request = requestBuilder.build()

        assertEquals(requestBody, request.binaryBody)
        assertEquals(true, request.isLastRequest)
        assertEquals("testWorkId", request.workId)
        assertEquals(null, request.network)

    }
}