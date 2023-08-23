package com.pdiot.resumableuploadwizard.models

import android.util.Log
import androidx.work.Data
import com.google.gson.Gson
import java.util.*

class SessionUrlResponse(
    override val request: UploadWorkerHttpRequest,
    override val headers: HashMap<String, String>?,
    override val bodyString: String?,
    override val code: Int?,
    override val cookies: HashMap<String, String>?): UploadWorkerHttpResponse(request, headers, bodyString, code, cookies) {

    companion object {
        private const val TAG = "SessionUrlResponse"
        private val gson = Gson()
    }

    private val body: SessionUrlBody = gson.fromJson(bodyString, SessionUrlBody::class.java)
    private class SessionUrlBody(val responseURL: String)

    override fun getOutputData(carryOverData: Map<UploadWorkerDataConstants, String>?): Data {
        val data = Data.Builder()
        if(carryOverData != null) {
            for(key in carryOverData.keys) {
                data.putString(key.name, carryOverData.get(key))
            }
        }
        data.putString(UploadWorkerDataConstants.SESSION_URI.name, body.responseURL)
        data.putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, "0")
        return data.build()
    }

    override fun isValidResponseBody(): Boolean {
        Log.d(TAG, "Body string: ${bodyString}")
        Log.d(TAG, "Body.responseURL: ${body.responseURL}")
        if(code == 200) {
            return true
        }
        return false
    }

    // responseURL

}