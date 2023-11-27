package com.sasirekha.resumableuploadwizard.models

import com.google.gson.Gson

class ObjectMetadata (
    val requestId: String,
    val username: String,
    val requestTimestamp: Long,
    val recordName: String,
    val contentType: String,
    val contentLength: Long,
    val checksum: String
) {
    companion object {
        private val gson = Gson()
    }

    override fun toString(): String {
        return gson.toJson(this);
    }

    fun getMap(): HashMap<String, String> {
        val map = HashMap<String, String>()

        map.put("requestId", requestId)
        map.put("username", username)
        map.put("requestTimestamp", requestTimestamp.toString())
        map.put("recordName", recordName)
        map.put("contentType", contentType)
        map.put("contentLength", contentLength.toString())
        map.put("checksum", checksum)

        return map
    }

}