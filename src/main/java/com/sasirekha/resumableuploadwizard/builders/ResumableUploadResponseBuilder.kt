package com.pdiot.resumableuploadmanager.builders

import com.pdiot.resumableuploadmanager.models.ResumableUploadResponse
import com.pdiot.resumableuploadmanager.models.UploadWorkerHttpResponse

class ResumableUploadResponseBuilder: UploadWorkerHttpResponseBuilder() {
    override fun build(): UploadWorkerHttpResponse {
        val range: String? = this.headers?.get("Range")
        val byteRangeString = if(range == null) null else range.split("=")[1]
        val rangeStart: Long? =
            if(byteRangeString == null || "" == byteRangeString.split("-")[0]) null else byteRangeString.split("-")[0].toLong();
        val rangeEnd: Long? =
            if(byteRangeString == null || "" == byteRangeString.split("-")[1]) null else byteRangeString.split("-")[1].toLong();

        val nextChunkFirstByte = if(rangeEnd == null) null else rangeEnd + 1
        return ResumableUploadResponse(this.request!!, this.headers, this.body, this.code, this.cookies, nextChunkFirstByte)
    }
}
