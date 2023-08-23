package com.sasirekha.resumableuploadwizard.models

import android.net.Network
import androidx.work.Data
import java.util.*

class ResumableUploadRequest(private val inputData: Data, override val workId: String, override val network: Network?, override val binaryBody: ByteArray?): UploadWorkerHttpRequest(workId, network, binaryBody) {
    val chunkSize: Int = getInputString(UploadWorkerDataConstants.CHUNK_SIZE)!!.toInt()
    val chunkFirstByte: Long = getInputString(UploadWorkerDataConstants.CHUNK_FIRST_BYTE)!!.toLong()
    val totalObjectSize: Long = getInputString(UploadWorkerDataConstants.TOTAL_OBJECT_SIZE)!!.toLong()
    val dataLocation: String = getInputString(UploadWorkerDataConstants.DATA_LOCATION)!!
    val contentTypeString: String = "application/octet-stream"
    val sessionUri: String = getInputString(UploadWorkerDataConstants.SESSION_URI)!!
    private var chunkLastByte: Long? = if (binaryBody != null) chunkFirstByte + binaryBody.size - 1 else null


    override fun getInputString(constant: UploadWorkerDataConstants): String? {
        return inputData.getString(constant.name)
    }

    override fun getCustomHeaders(): Map<String, String> {
        val map = HashMap<String, String>()
        map["Content-Range"] = "bytes ${chunkFirstByte}-${chunkLastByte}/${totalObjectSize}"
        return map
    }

    override fun getRequestBody(): ByteArray? {
        return binaryBody
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

    override fun getUrl(): java.net.URL {
        return java.net.URL(sessionUri)
    }

    override fun getContentType(): String {
        return contentTypeString
    }

    override val carryOverDataList: List<UploadWorkerDataConstants> = arrayListOf(
        UploadWorkerDataConstants.DATA_LOCATION,
        UploadWorkerDataConstants.CHUNK_SIZE,
        UploadWorkerDataConstants.CONTENT_TYPE,
        UploadWorkerDataConstants.TOTAL_OBJECT_SIZE,
        UploadWorkerDataConstants.SESSION_URI,
        UploadWorkerDataConstants.RECORD_NAME,
        UploadWorkerDataConstants.CHECKSUM
    )

    override val isLastRequest: Boolean = (chunkFirstByte + chunkSize >= totalObjectSize)

}