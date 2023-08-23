package com.pdiot.resumableuploadwizard.builders

import com.pdiot.resumableuploadwizard.models.SessionUrlResponse
import com.pdiot.resumableuploadwizard.models.UploadWorkerHttpResponse

class SessionUrlResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return SessionUrlResponse(request!!, headers, body, code, cookies)
    }
}