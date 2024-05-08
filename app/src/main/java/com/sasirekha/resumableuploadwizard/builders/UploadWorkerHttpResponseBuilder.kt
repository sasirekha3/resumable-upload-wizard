package com.sasirekha.resumableuploadwizard.builders

import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpRequest
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse
import okhttp3.Headers
import java.util.*

abstract class UploadWorkerHttpResponseBuilder() {
    var request: UploadWorkerHttpRequest? = null
    var headers: HashMap<String, String>? = null
    var body: String? = null
    var code: Int? = null
    var cookies: HashMap<String, String>? = null

    // FinalUploadResponse body attributes
//    val kind: String
//    val id: String?
//    val selfLink: String?
//    val mediaLink: String?
//    val name: String?
//    val bucket: String?
//    val generation: String?
//    val metageneration: String?
//    val storageClass: String?
//    val size: String?
//    val md5Hash: String?
//    val crc32c: String?
//    val etag: String?
//    val timeCreated: String?
//    val updated: String?
//    val timeStorageClassUpdated: String?

    // ResumableUploadResponse body attributes
//    val responseURL: String

    fun setCorrespondingRequest(request: UploadWorkerHttpRequest) {
        this.request = request
    }

    fun setBodyString(bodyString: String) {
        this.body = bodyString
    }

    fun setHeadersAndCookies(headers: Headers) {
        this.headers = HashMap<String, String>()
        for(headerKey in headers.names()) {
            // Store cookies separately
            if("Cookie".equals(headerKey)) {
                this.cookies = HashMap<String, String>()
                val cookieStrings = headers.get(headerKey).split(";")
                for(cookie in cookieStrings) {
                    this.cookies!![cookie.split("=")[0]] = cookie.split("=")[1]
                }
            } else {
                this.headers!!.set(headerKey, headers.get(headerKey))
            }
        }
    }
    fun setResponseCode(code: Int) {
        this.code = code
    }

    abstract fun build(): UploadWorkerHttpResponse
}