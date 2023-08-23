package com.pdiot.resumableuploadmanager.models

import androidx.work.Data
import java.util.*

abstract class UploadWorkerHttpResponse (
    open val request: UploadWorkerHttpRequest,
    open val headers: HashMap<String, String>?,
    open val bodyString: String?,
    open val code: Int?,
    open val cookies: HashMap<String, String>?
)  {
    abstract fun getOutputData(carryOverData: Map<UploadWorkerDataConstants, String>?): Data
    abstract fun isValidResponseBody(): Boolean
}