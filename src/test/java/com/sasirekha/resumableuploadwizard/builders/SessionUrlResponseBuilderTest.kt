package com.sasirekha.resumableuploadwizard.builders

import androidx.work.Data
import com.google.gson.Gson
import com.sasirekha.resumableuploadwizard.models.ObjectMetadata
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import junit.framework.Assert.assertEquals
import okhttp3.Headers
import org.junit.Test

class SessionUrlResponseBuilderTest {
    @Test
    fun sessionUrlResponseBuilderTest() {
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

        val sessionUrlResponseBuilder = SessionUrlResponseBuilder()
        sessionUrlResponseBuilder.setCorrespondingRequest(sessionUrlRequest)
        sessionUrlResponseBuilder.setResponseCode(200)
        sessionUrlResponseBuilder.setBodyString("{\"responseURL\": \"https://example.com\"}")
        sessionUrlResponseBuilder.setHeadersAndCookies(Headers.Builder().build())

        val sessionUrlResponse = sessionUrlResponseBuilder.build()

        assertEquals(HashMap<String, String>(), sessionUrlResponse.headers)
        assertEquals(null, sessionUrlResponse.cookies)
        assertEquals("{\"responseURL\": \"https://example.com\"}", sessionUrlResponse.bodyString)
        assertEquals(200, sessionUrlResponse.code)
        assertEquals(true, sessionUrlResponse.isValidResponseBody())
    }
}