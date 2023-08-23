package com.pdiot.resumableuploadmanager.models

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.lang.RuntimeException
import java.math.BigInteger
import java.net.URI
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.Base64.getEncoder


class MD5(val context: Context) {
    private val TAG = "MD5"

    fun calculateMD5(str: String): String? {
         try {
             val digest = MessageDigest.getInstance("MD5")
            val md5sum = digest.digest(str.toByteArray())
            val output = getEncoder().encodeToString(md5sum)
            return output
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Exception while getting digest", e)
        }
        return null
    }

    fun checkMD5(md5: String, contentUri: Uri?): Boolean {
        if (TextUtils.isEmpty(md5) || contentUri == null) {
            Log.e(TAG, "MD5 string empty or updateFile null")
            return false
        }
        val calculatedDigest = calculateMD5(contentUri)
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null")
            return false
        }
        Log.v(TAG, "Calculated digest: $calculatedDigest")
        Log.v(TAG, "Provided digest: $md5")
        return calculatedDigest.equals(md5, ignoreCase = true)
    }

    fun calculateMD5(contentUri: Uri): String? {
        val digest: MessageDigest
        digest = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Exception while getting digest", e)
            return null
        }
        var inputStream: BufferedInputStream? = null;
        try {
            inputStream = context.contentResolver.openInputStream(contentUri)!!.buffered()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Exception while getting FileInputStream", e)
            return null
        }
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (inputStream.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            val md5sum = digest.digest()
            val output = getEncoder().encodeToString(md5sum)
            output
        } catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                Log.e(TAG, "Exception on closing MD5 input stream", e)
            }
        }
    }
}