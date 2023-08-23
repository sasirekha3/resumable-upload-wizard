package com.pdiot.resumableuploadwizard.clients

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pdiot.backupfilestogcp.exceptions.LocalFileReaderException
import java.io.BufferedInputStream
import java.io.IOException

class LocalFileReader {
    companion object {
        private const val TAG: String = "LocalFileReader";
    }
    private var context: Context? = null;
    private var inputStream: BufferedInputStream? = null;
    private var nextByteToBeRead: Long = 0;
    private var chunkSize: Int = 1;
    private var totalObjectSize: Long = 0;
    private var uri: Uri? = null;

    fun open(_context: Context, uri: Uri, chunkSize: Int, totalObjectSize: Long) {
        context = _context.applicationContext
        inputStream = context?.contentResolver?.openInputStream(uri)?.buffered();
        this.chunkSize = chunkSize;
        this.totalObjectSize = totalObjectSize;
        this.uri = uri;
    }

    fun getChunkLastByte(chunkFirstByte: Long, totalObjectSize: Long): Long {
        // Calculate chunkLastByte
        val chunkLastByte: Long =
            if((chunkFirstByte + chunkSize - 1) <= totalObjectSize - 1) (chunkFirstByte + chunkSize.toLong() - 1) else (totalObjectSize - 1)
        return chunkLastByte
    }

    fun getBytes(chunkFirstByte: Long): ByteArray {
        // if range is 0-255, chunk size is 256, 0th and 255th bytes are included
        val currentChunkSize: Int = (getChunkLastByte(chunkFirstByte, totalObjectSize) - chunkFirstByte).toInt() + 1;
        val byteArray = ByteArray(currentChunkSize);
        try {
            // if the required byte is before the byte at which the file pointer is pointing
            if(chunkFirstByte <= nextByteToBeRead) {
                // reopen the stream and skip the required number of bytes
                inputStream?.close()
                inputStream = context?.contentResolver?.openInputStream(uri!!)?.buffered();
                inputStream?.skip(chunkFirstByte);
                nextByteToBeRead = chunkFirstByte;
            // if the required byte is after the byte at which the file pointer is pointing
            } else if (chunkFirstByte > nextByteToBeRead) {
                val bytesSkipped = inputStream?.skip(chunkFirstByte - nextByteToBeRead);
                if (bytesSkipped != null) {
                    nextByteToBeRead += bytesSkipped
                }
            }
            // read bytes from the current byte at which the file pointer is pointing
            val bytesRead = inputStream?.read(byteArray, 0, currentChunkSize)

            // update nextByteToBeRead
            if (bytesRead != null && bytesRead == -1) {
                nextByteToBeRead = -1;
            } else if (bytesRead != null) {
                nextByteToBeRead += bytesRead
            }
        } catch(e: Exception) {
            Log.e(TAG, "Unable to read from file because: ${e.message}");
            throw LocalFileReaderException("File could not be read: ${e.message}");
        }
        return byteArray;
    }

    fun close() {
        try {
            inputStream?.close();
        } catch(e: IOException) {
            Log.e(TAG, "InputStream Could not be closed because: ${e.message}");
        }
    }
}