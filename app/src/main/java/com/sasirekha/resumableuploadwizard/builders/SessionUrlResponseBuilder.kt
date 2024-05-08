package com.sasirekha.resumableuploadwizard.builders

import com.sasirekha.resumableuploadwizard.models.SessionUrlResponse
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse

class SessionUrlResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return SessionUrlResponse(request!!, headers, body, code, cookies)
    }
}