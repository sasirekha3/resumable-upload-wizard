package com.pdiot.resumableuploadmanager.builders

import android.net.Network
import androidx.work.Data
import com.pdiot.resumableuploadmanager.models.UploadWorkerDataConstants
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpRequest

abstract class UploadWorkerHttpRequestBuilder(open val inputData: Data, open val workId: String, open val network: Network?) {
    abstract var requestBody: ByteArray?

    abstract fun build(): UploadWorkerHttpRequest

    fun getInputData(constant: UploadWorkerDataConstants): String? {
        return inputData.getString(constant.name)
    }
}