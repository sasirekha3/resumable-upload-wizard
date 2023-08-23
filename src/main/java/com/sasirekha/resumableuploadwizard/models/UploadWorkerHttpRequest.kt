package com.pdiot.resumableuploadmanager.models

import android.net.Network
import java.net.URL

abstract class UploadWorkerHttpRequest(open val workId: String,
                                       open val network: Network?,
                                       open val binaryBody: ByteArray?) {
    abstract fun getCustomHeaders(): Map<String, String>
    abstract fun getRequestBody(): ByteArray?
    abstract fun getInputString(constant: UploadWorkerDataConstants): String?
    abstract fun getCarryOverData(): Map<UploadWorkerDataConstants, String>?
    abstract fun getUrl(): URL
    abstract fun getContentType(): String
    abstract val carryOverDataList: List<UploadWorkerDataConstants>
    abstract val isLastRequest: Boolean

}