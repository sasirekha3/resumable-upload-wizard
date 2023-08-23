package com.sasirekha.resumableuploadwizard.builders

import com.sasirekha.resumableuploadwizard.models.FinalUploadResponse
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse

class FinalUploadResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return FinalUploadResponse(request!!, headers, body, code, cookies)
    }
}