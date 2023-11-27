package com.sasirekha.resumableuploadwizard

import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import com.sasirekha.resumableuploadwizard.models.MD5
import com.sasirekha.resumableuploadwizard.models.ObjectMetadata
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Date
import java.util.UUID


class ManagerInstrumentedTest {
    private val TAG = "ManagerInstrumentedTest"

    @Test
    fun managerTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Write test data
        val fileContents = "h1,h2,h3,h4\n" +
                "a,b,c,d\n" +
                "e,f,g,h\n" +
                "a,b,c,d\n" +
                "e,f,g,h\n"
        val fileName = "TestFile.csv"
        val tempFile = File(context.filesDir, fileName)
        val fos = FileOutputStream(tempFile)
        saveAndClose(fileContents, fos)

        // Read test data like we would normally
        val username = "TEST_USER"
        val filePath = Uri.fromFile(tempFile)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val fileDescriptor = context.contentResolver.openFileDescriptor(filePath, "r")
        val fileSize: Long? = fileDescriptor?.statSize
        fileDescriptor?.close()
        val md5 = MD5(context)
        var checksum: String? = null

        try {
            // calculate MD5 checksum of file contents
            checksum = md5.calculateMD5(filePath)

        } catch(e: Exception) {
            Log.e(TAG, "Unable to calculate MD5 checksum of file contents")
            return
        }
        val requestId = UUID.randomUUID()
        val metadata = ObjectMetadata(requestId.toString(), username, Date().time, fileName,
            "text/csv", fileSize!!, checksum!!)

        val config = UploadConfiguration(0, 1024,
            "text/csv", filePath, fileSize!!, checksum!!, URL("http://example.com"),
            "x-api-key","MyApiKey",metadata.getMap())
        val existingWorkPolicy = ExistingWorkPolicy.KEEP

        val mgr = Manager(context, config, constraints, existingWorkPolicy)

        val workRequests = mgr.getWorkRequests()

        // There will need to be 2 work requests (one getSessionUrl, one resumableUpload)
        // for a file of size 44 with chunkSize set to 1024
        assertEquals(44, fileSize)
        assertEquals(2, workRequests.size)
        assertEquals("bXq/EGsHKPekEmoqDefq1w==", checksum)
    }

    private fun saveAndClose(data: String, fos: FileOutputStream) {
        fos.write(data.toByteArray())
        fos.close()
    }
}