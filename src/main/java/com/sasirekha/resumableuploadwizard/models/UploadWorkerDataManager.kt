package com.pdiot.resumableuploadmanager.models

import androidx.work.Data
import okhttp3.Request
import okhttp3.Response
import java.util.*

interface UploadWorkerDataManager {
    var carryOverData: HashMap<String, String>?
    val workId: String;
    fun createOutputData(httpResponse: Response, additionalData: Map<String, String>?): Data;
    fun createRequest(inputData: Data, additionalData: Map<String, String>?): Request;
}