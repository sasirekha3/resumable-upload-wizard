package com.pdiot.resumableuploadwizard.models

import androidx.work.Data
import java.util.*

class ResumableUploadResponse(
    override val request: UploadWorkerHttpRequest,
    override val headers: HashMap<String, String>?,
    override val bodyString: String?,
    override val code: Int?,
    override val cookies: HashMap<String, String>?,
    val nextChunkFirstByte: Long?): UploadWorkerHttpResponse(request, headers, bodyString, code, cookies) {

    override fun getOutputData(carryOverData: Map<UploadWorkerDataConstants, String>?): Data {
        val data = Data.Builder()
        if(carryOverData != null) {
            for(key in carryOverData.keys) {
                data.putString(key.name, carryOverData.get(key))
            }
        }
        data.putString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE.name, nextChunkFirstByte.toString())
        return data.build()
    }

    override fun isValidResponseBody(): Boolean {
        if(code == 308 && (bodyString == null || bodyString.equals("")) && nextChunkFirstByte != null){
            return true
        }
        return false
    }

}