package com.pdiot.resumableuploadwizard.models

import com.google.gson.Gson

//{
//    "requestId": "8ab652f9-e34f-47ea-9056-2d168ed47d3e",
//    "username": "TEST",
//    "requestTimestamp": "1686016615080",
//    "recordName": "TEST_FILE.csv",
//    "contentType": "text/csv",
//    "contentLength": 40
//}

class ObjectMetadata (
    val requestId: String,
    val username: String,
    val requestTimestamp: Long,
    val recordName: String,
    val contentType: String,
    val contentLength: Long
) {
    companion object {
        private val gson = Gson()
    }

    override fun toString(): String {
        return gson.toJson(this);
    }

}