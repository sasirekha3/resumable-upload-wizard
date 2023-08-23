package com.pdiot.resumableuploadwizard.builders

import com.pdiot.resumableuploadwizard.models.FinalUploadResponse
import com.pdiot.resumableuploadwizard.models.UploadWorkerHttpResponse

class FinalUploadResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return FinalUploadResponse(request!!, headers, body, code, cookies)
    }
}