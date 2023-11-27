package com.sasirekha.resumableuploadwizard

import android.net.Uri
import androidx.work.Data
import com.google.gson.Gson
import com.sasirekha.resumableuploadwizard.models.ObjectMetadata
import com.sasirekha.resumableuploadwizard.models.UploadWorkerDataConstants
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.net.URL
import java.util.Date
import java.util.UUID

class UploadConfigurationTest {
    private val TAG = "UploadConfigurationTest"

    @Test
    fun uploadConfigurationTest() {
        val gson = Gson()

        val fileName = "TestFile.csv"
        val username = "TEST_USER"
        val filePath = Uri.parse("content://test/path/TestFile.csv")
        val fileSize: Long = 44
        var checksum: String = "bXq/EGsHKPekEmoqDefq1w=="
        val requestId = UUID.randomUUID()
        val metadata = ObjectMetadata(requestId.toString(), username, Date().time, fileName,
            "text/csv", fileSize!!, checksum!!)

        val config = UploadConfiguration(0, 1024,
            "text/csv", filePath, fileSize!!, checksum!!, URL("http://example.com"),
            "x-api-key","MyApiKey", metadata.getMap())

        val inputData: Data = Data.Builder()
            .putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
            .putString(UploadWorkerDataConstants.CHUNK_SIZE.name, "1024")
            .putString(UploadWorkerDataConstants.CONTENT_TYPE.name, "text/csv")
            .putString(UploadWorkerDataConstants.DATA_LOCATION.name, filePath.toString())
            .putString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE.name, "44")
            .putString(UploadWorkerDataConstants.CHECKSUM.name, checksum)
            .putString(UploadWorkerDataConstants.METADATA.name, gson.toJson(metadata.getMap()))
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_URL.name, "http://example.com")
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_HEADER.name, "x-api-key")
            .putString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_VALUE.name, "MyApiKey")
            .build()

        assertEquals(inputData, config.getInitialInputData())
    }
}