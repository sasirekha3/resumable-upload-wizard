package com.sasirekha.resumableuploadwizard.clients

import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.ceil

class LocalFileReaderTest {
    private val TAG = "LocalFileReaderTest"

    @Test
    fun localFileReaderTest() {
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
        val filePath = Uri.fromFile(tempFile)

        val chunkSize = 4
        val totalObjectSize = tempFile.length()
        var readerCounter = 0
        val localFileReader = LocalFileReader()
        var chunkFirstByte = 0L
        var stringReadFromFile = StringBuilder()
        localFileReader.open(context, filePath, chunkSize, totalObjectSize)

        while(chunkFirstByte < totalObjectSize){
            val byteArray = localFileReader.getBytes(chunkFirstByte)
            stringReadFromFile.append(byteArray.toString(Charsets.UTF_8))
            chunkFirstByte += byteArray.size
            readerCounter++
        }

        localFileReader.close()

        Log.d(TAG, stringReadFromFile.toString())
        Log.d(TAG, readerCounter.toString())

        assertEquals(ceil(totalObjectSize.toDouble() / chunkSize.toDouble()).toInt(), readerCounter)
        assertEquals(stringReadFromFile.toString(), fileContents)
    }

    private fun saveAndClose(data: String, fos: FileOutputStream) {
        fos.write(data.toByteArray())
        fos.close()
    }
}