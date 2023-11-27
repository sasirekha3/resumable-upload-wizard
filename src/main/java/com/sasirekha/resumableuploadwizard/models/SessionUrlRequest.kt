package com.sasirekha.resumableuploadwizard.models

import android.net.Network
import androidx.work.Data
import java.net.URL
import java.util.*

class SessionUrlRequest(val inputData: Data, override val workId: String, override val network: Network?, override val binaryBody: ByteArray?):
    UploadWorkerHttpRequest(workId, network, binaryBody) {


    override fun getCustomHeaders(): Map<String, String> {
        val authHeader = HashMap<String, String>()
        getInputString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_HEADER)?.let {
            getInputString(UploadWorkerDataConstants.SESSION_REQUEST_API_AUTH_VALUE)?.let { it1 ->
                authHeader.put(
                    it,
                    it1
                )
            }
        }
        return authHeader
    }

    override fun getRequestBody(): ByteArray? {
        return binaryBody
    }

    override fun getInputString(constant: UploadWorkerDataConstants): String? {
        return inputData.getString(constant.name)
    }

    override fun getCarryOverData(): Map<UploadWorkerDataConstants, String>? {
        var map: HashMap<UploadWorkerDataConstants, String>? = null
        if(carryOverDataList.isNotEmpty()) {
            map = HashMap<UploadWorkerDataConstants, String>()
            for(item in carryOverDataList){
                getInputString(item)?.let { map.put(item, it) }
            }
        }
        return map
    }

    override fun getUrl(): URL {
        return URL(getInputString(UploadWorkerDataConstants.SESSION_REQUEST_API_URL))
    }

    override fun getContentType(): String {
        return "application/json"
    }

override val carryOverDataList: List<UploadWorkerDataConstants> = arrayListOf(
    UploadWorkerDataConstants.DATA_LOCATION,
    UploadWorkerDataConstants.CHUNK_SIZE,
    UploadWorkerDataConstants.CONTENT_TYPE,
    UploadWorkerDataConstants.TOTAL_OBJECT_SIZE,
    UploadWorkerDataConstants.RECORD_NAME,
    UploadWorkerDataConstants.CHECKSUM
    )
    override val isLastRequest: Boolean = false
}