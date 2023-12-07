# Resumable Upload Wizard

ResumableUploadWizard is a wrapper around the [Resumable Upload API](https://cloud.google.com/storage/docs/resumable-uploads "Resumable uploads") by Google Cloud Platform for Android apps. This library uses [WorkManager](https://developer.android.com/reference/androidx/work/WorkManager) and [OneTimeWorkRequest](https://developer.android.com/reference/androidx/work/OneTimeWorkRequest) chains to upload files based on an `UploadConfiguration` introduced by this library.

The `WorkManager` is instantiated using this library's `Manager` class, which exposes all automatically created chained `WorkRequest`s to the calling function, should the user require tracking using `Observer`s.

# Prerequisites

A ResumableUpload to a Google Cloud bucket requires a signed URL that we will be calling `SessionURL` through the rest of this document. This signed URL needs to be vended using the correct credentials by an authenticated API to the Android App.

The user is required to host their own SessionURL vending API which accepts a POST request and returns the `SessionURL` using the following response format:
```json
{
  "responseURL": "<GCP's Session URL>"
}
```

The URL of this user hosted API will henceforth be referred to as `gcpapiurl`. The user has the option to specify authentication headers for the POST request. Additionally, any object metadata passed to the `UploadConfiguration` instance will be sent as the JSON body of the POST Request to the API.

# Usage

Import the library into your Android App project.

## Instantiate Upload Configuration

Steps:

1. Calculate the MD5 checksum of the file that needs to be uploaded using the built-in `MD5` class to cross-verify that the integrity of the file was maintained while uploading.
2. (Optionally) specify metadata that needs to be sent to your SessionURL vending API in the form of a `JSONObject`. You may use the built-in class `ObjectMetadata` if the fields inside satisfy your needs. Else, you may leave it null or empty.
3. Instantiate the `UploadConfiguration` class

```kotlin
// Step 1: Calculate MD5 checksum
val fileDescriptor = context.contentResolver.openFileDescriptor(filePath, "r")
val fileSize: Long? = fileDescriptor?.statSize
fileDescriptor?.close()
val md5 = MD5(context)
var checksum: String? = null

try {
    // calculate MD5 checksum of file contents
    checksum = md5.calculateMD5(filePath)

} catch(e: Exception) {
    Log.e(TAG, "Unable to calculate MD5 checksum of file contents")
    return
}
val requestId = UUID.randomUUID()

// Step 2: Specify ObjectMetadata
val metadata = ObjectMetadata(requestId.toString(), username, Date().time, fileName,
    "text/csv", fileSize!!, checksum!!)

// Step 3: Instantiate UploadConfiguration
val config = UploadConfiguration(0, BuildConfig.uploadchunksizebytes.toInt(),
    "text/csv", filePath, fileSize!!, checksum!!, URL(BuildConfig.gcpapiurl),
    "x-api-key", BuildConfig.gcpapixapikey, metadata.getMap())
```

## Choose an ExistingWorkPolicy and work constraints

Steps:

1. Specify work constraints using `androidx.work.Constraints`.
2. Specify what to do with existing work using `androidx.work.ExistingWorkPolicy`. 

```kotlin
// Step 1: Specify constraints
val constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

// Step 2: Choose an ExistingWorkPolicy
val existingWorkPolicy = ExistingWorkPolicy.KEEP
```

## Instantiate ResumableUploadWizard's Manager

The `Manager` class is context dependent.

```kotlin
val mgr = Manager(context, config, constraints, existingWorkPolicy)
```

## Optionally observe all or some of the WorkRequests for completion status

```kotlin
// Observe last WorkRequest for completion so that we are notified when the upload has been 
// completed successfully, or if it has failed.
val workRequests = mgr.getWorkRequests()
        val workId = mgr.workId

        val lastWorkRequest = workRequests[workRequests.size - 1]
        mgr.enqueueWork()
        WorkManager.getInstance(context)
            // requestId is the WorkRequest id
            .getWorkInfoByIdLiveData(lastWorkRequest.id)
            .observe(lifecycleOwner, androidx.lifecycle.Observer { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Do something with progress information
                        ProgressNotification.notifyWorkCompletion(context, fileName, true)
                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                        // Do something with progress information
                        ProgressNotification.notifyWorkCompletion(context, fileName, false)
                    }
                }
            })
```