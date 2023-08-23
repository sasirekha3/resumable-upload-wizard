package com.pdiot.resumableuploadmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import com.pdiot.resumableuploadmanager.exceptions.UploadException
import com.pdiot.resumableuploadmanager.models.MD5
import com.pdiot.resumableuploadmanager.workers.GetSessionUriWorker
import com.pdiot.resumableuploadmanager.workers.PutFileChunkWorker
import com.pdiot.resumableuploadmanager.workers.ResumableUploadWorker
import java.util.ArrayList


@SuppressLint("EnqueueWork")
class Manager(
    private val context: Context,
    private val configuration: UploadConfiguration,
    private val constraints: Constraints,
    private val existingWorkPolicy: ExistingWorkPolicy) {

    companion object {
        private const val TAG = "ResUplManager"
    }

    private val workManager = WorkManager.getInstance(context)
    private val md5 = MD5(context)
    private var continuation: WorkContinuation? = null
    private var mutableWorkRequestList: MutableList<OneTimeWorkRequest> = ArrayList()

    val totalNumberOfUploads: Int = (((configuration.totalObjectSize) / configuration.chunkSize)).toInt() + 1
    private var workRequestList: List<OneTimeWorkRequest>? = null

    init {
        // Get true file size
        val fileDescriptor = context.contentResolver.openFileDescriptor(configuration.dataLocation, "r")
        val fileSize: Long? = fileDescriptor?.statSize
        fileDescriptor?.close()

        var checksum: String? = null
        // Create workId from the string of the filePath (not file contents)
        val workId = "${ResumableUploadWorker.workNamePrefix}${md5.calculateMD5(configuration.dataLocation.toString())}"

        // Calculate MD5 checksum of file contents
        checksum = md5.calculateMD5(configuration.dataLocation)


        if(fileSize == null || fileSize == 0L || workId == null || checksum == null) {
            // TODO: throw custom exception
        }

        Log.d("FILE_SIZE", "$fileSize")

        // Enqueue initial work request
        val inputData = configuration.getInitialInputData()
        val sessionUriWork = OneTimeWorkRequestBuilder<GetSessionUriWorker>()
            .setConstraints(constraints)
            .addTag(workId)
            .setInputData(inputData).build()
        mutableWorkRequestList.add(sessionUriWork)

        continuation = workManager.beginUniqueWork(
            workId,
            existingWorkPolicy, sessionUriWork)

        // Split file into chunks and update continuation
        chainChunkedContinuationRequests(fileSize!!, workId)

        // Make work request list available for observability
        workRequestList = mutableWorkRequestList.toList()
    }

    fun enqueueWork() {
        // Actually enqueue the work requests
        continuation!!.enqueue();
    }

    fun getWorkRequests(): List<OneTimeWorkRequest> {
        return workRequestList!!
    }

    @SuppressLint("EnqueueWork")
    private fun chainChunkedContinuationRequests(fileSize: Long, workId: String){
        var offset = 0;
        val chunkSize = configuration.chunkSize
        val objectSize = fileSize
        // Add WorkRequests to blur the image the number of times requested
        while(offset < objectSize) {
            Log.d("TTTTTTTTT", "CALLING PUT FILE CHUNK WORKER");
            try {
                val uploadBuilder = OneTimeWorkRequestBuilder<PutFileChunkWorker>()
                    .addTag(workId)
                    .setConstraints(constraints)
                val uploadWork = uploadBuilder.build()
                mutableWorkRequestList.add(uploadWork)
                continuation = continuation!!.then(uploadWork)
            } catch(e: UploadException) {
                e.message?.let { Log.d(TAG, it) }
                break;
            }
            offset += chunkSize;
        }
    }
}