package com.pdiot.resumableuploadmanager.builders

import com.pdiot.resumableuploadmanager.models.SessionUrlResponse
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpResponse

class SessionUrlResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        return SessionUrlResponse(request!!, headers, body, code, cookies)
    }
}