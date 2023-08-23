package com.pdiot.resumableuploadmanager.builders

import com.pdiot.resumableuploadmanager.models.FinalUploadResponse
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpResponse

class FinalUploadResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return FinalUploadResponse(request!!, headers, body, code, cookies)
    }
}