package com.sasirekha.resumableuploadwizard.builders

import androidx.work.Data
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import junit.framework.TestCase.assertEquals
import okhttp3.Headers
import org.junit.Test
import java.util.HashMap


class FinalUploadResponseBuilderTest {
    @Test
    fun finalUploadBuilderTest() {
        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, "1024")
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, "testLocation")
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, "20")
            .putString(UploadWorkerDataConstants.SESSION_URI.name, "https://example.com")
            .putString(UploadWorkerDataConstants.CHECKSUM.name, "TestChecksum==")
            .build()
        val requestBuilder = ResumableUploadRequestBuilder(inputData, "testWorkId", null)
        requestBuilder.requestBody = "testBody".toByteArray()
        val request = requestBuilder.build()
        val builder = FinalUploadResponseBuilder()
        builder.setCorrespondingRequest(request)
        val responseBody = "{" +
                "\"id\": \"testId\"," +
                "\"kind\": \"kind\"," +
                "\"updated\": 1699326013," +
                "\"md5Hash\": \"TestChecksum==\"" +
                "}"
        builder.setBodyString(responseBody)
        builder.setHeadersAndCookies(Headers.Builder().build())
        builder.setResponseCode(200)
        val finalUploadResponse = builder.build()

        assertEquals(request, finalUploadResponse.request)
        assertEquals(200, finalUploadResponse.code)
        assertEquals(responseBody, finalUploadResponse.bodyString)
        assertEquals(null, finalUploadResponse.cookies)
        assertEquals(HashMap<String, String>(), finalUploadResponse.headers)
        assertEquals(true, finalUploadResponse.isValidResponseBody())
    }
}