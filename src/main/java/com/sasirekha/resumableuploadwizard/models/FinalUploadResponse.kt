package com.pdiot.resumableuploadwizard.models

import androidx.work.Data
import com.google.gson.Gson
import java.util.*

//{
//    "kind": "storage#object",
//    "id": "resumable-upload-destination/test-file-1686016615080.csv/1686017983341662",
//    "selfLink": "https://www.googleapis.com/storage/v1/b/resumable-upload-destination/o/test-file-1686016615080.csv",
//    "mediaLink": "https://storage.googleapis.com/download/storage/v1/b/resumable-upload-destination/o/test-file-1686016615080.csv?generation=1686017983341662&alt=media",
//    "name": "test-file-1686016615080.csv",
//    "bucket": "resumable-upload-destination",
//    "generation": "1686017983341662",
//    "metageneration": "1",
//    "storageClass": "STANDARD",
//    "size": "40",
//    "md5Hash": "e4tdyzf3GA4G3Ai88Mecng==",
//    "crc32c": "S/VMTQ==",
//    "etag": "CN7I7bfKrf8CEAE=",
//    "timeCreated": "2023-06-06T02:19:43.408Z",
//    "updated": "2023-06-06T02:19:43.408Z",
//    "timeStorageClassUpdated": "2023-06-06T02:19:43.408Z"
//}

class FinalUploadResponse(
    override val request: UploadWorkerHttpRequest,
    override val headers: HashMap<String, String>?,
    override val bodyString: String?,
    override val code: Int?,
    override val cookies: HashMap<String, String>?
    ): UploadWorkerHttpResponse(request, headers, bodyString, code, cookies) {

    companion object {
        private val gson = Gson()
    }

    private val body: FinalUploadBody = gson.fromJson(bodyString, FinalUploadBody::class.java)

    private class FinalUploadBody (val kind: String,
                                   val id: String?,
                                   val selfLink: String?,
                                   val mediaLink: String?,
                                   val name: String?,
                                   val bucket: String?,
                                   val generation: String?,
                                   val metageneration: String?,
                                   val storageClass: String?,
                                   val size: String?,
                                   val md5Hash: String?,
                                   val crc32c: String?,
                                   val etag: String?,
                                   val timeCreated: String?,
                                   val updated: String?,
                                   val timeStorageClassUpdated: String?)


    override fun getOutputData(carryOverData: Map<UploadWorkerDataConstants, String>?): Data {
        val data = Data.Builder()
        if(carryOverData != null) {
            for(key in carryOverData.keys) {
                data.putString(key.name, carryOverData.get(key))
            }
        }
        data.putString("body_md5Hash", body.md5Hash)
        data.putString("body_updated", body.updated)
        data.putString("body_kind", body.kind)
        data.putString("body_id", body.id)
        return data.build()
    }

    override fun isValidResponseBody(): Boolean {
        if((code == 200 || code == 201) && body.md5Hash.equals(request.getInputString(UploadWorkerDataConstants.CHECKSUM))){
            return true
        }
        return false
    }
}