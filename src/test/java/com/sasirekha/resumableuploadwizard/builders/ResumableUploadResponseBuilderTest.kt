package com.sasirekha.resumableuploadwizard.builders

import androidx.work.Data
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import junit.framework.TestCase
import okhttp3.Headers
import org.junit.Test
import java.util.HashMap

/* Range Header
Range: bytes=0-1999 (first 2000 bytes)
Range: bytes=-2000 (last 2000 bytes)
Range: bytes=2000- (from byte 2000 to end of file)
 */

class ResumableUploadResponseBuilderTest {
    @Test
    fun resumableUploadResponseBuilderTest() {
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
        requestBuilder.requestBody = "testBody".toByteArray()
        val request = requestBuilder.build()
        val builder = ResumableUploadResponseBuilder()

        builder.setCorrespondingRequest(request)
        builder.setBodyString("")
        val headersBuilder = Headers.Builder()
        headersBuilder.add("Range", "bytes=0-1023")
        builder.setHeadersAndCookies(headersBuilder.build())
        builder.setResponseCode(308)
        val resumableUploadResponse = builder.build()

        val validHeadersExample = HashMap<String, String>()
        validHeadersExample.put("Range", "bytes=0-1023")

        TestCase.assertEquals(request, resumableUploadResponse.request)
        TestCase.assertEquals(308, resumableUploadResponse.code)
        TestCase.assertEquals("", resumableUploadResponse.bodyString)
        TestCase.assertEquals(null, resumableUploadResponse.cookies)
        TestCase.assertEquals(validHeadersExample, resumableUploadResponse.headers)
        TestCase.assertEquals(true, resumableUploadResponse.isValidResponseBody())
    }

    @Test
    fun lastResumableUploadResponseBuilderTest() {
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
        requestBuilder.requestBody = "testBody".toByteArray()
        val request = requestBuilder.build()
        val builder = ResumableUploadResponseBuilder()

        builder.setCorrespondingRequest(request)
        builder.setBodyString("")
        val headersBuilder = Headers.Builder()
        headersBuilder.add("Range", "bytes=1024-")
        builder.setHeadersAndCookies(headersBuilder.build())
        builder.setResponseCode(308)
        val resumableUploadResponse = builder.build()

        val validHeadersExample = HashMap<String, String>()
        validHeadersExample.put("Range", "bytes=1024-")

        TestCase.assertEquals(request, resumableUploadResponse.request)
        TestCase.assertEquals(308, resumableUploadResponse.code)
        TestCase.assertEquals("", resumableUploadResponse.bodyString)
        TestCase.assertEquals(null, resumableUploadResponse.cookies)
        TestCase.assertEquals(validHeadersExample, resumableUploadResponse.headers)
        // This has to be a FinalUploadResponse, and not ResumableUploadRResponse since it is the last byte range
        TestCase.assertEquals(false, resumableUploadResponse.isValidResponseBody())
    }
}