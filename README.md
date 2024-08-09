# Resumable Upload Wizard

ResumableUploadWizard is a Kotlin library to upload very large files to Google Cloud Platform's object storage service (Cloud Storage) in a "resumable" manner. 

The library accepts credentials and bucket properties for the upload and schedules Android Tasks in the background to complete the upload even after the app has become inactive. Each Android Task carries out a chunked upload from the local filesystem, while allowing the user of the library to configure resource constraints (such as, upload only when battery is not low).

## Library Usage

A complete Android app that demonstrates how this library can be used is [here](https://github.com/sasirekha3/resumable-upload-to-gcp).

### Adding the library as a dependency

Ensure that your `local.properties` contains the following fields:

```properties
gcp.api.url=<link to your API that vends a presignedURL for resumable upload>
gcp.api.xapikey=<The api key of that API>
# 4 * 1024 * 1024 bytes = 4194304  bytes = 4 MiB
upload.chunk.size.bytes=4194304
```

Add a .env file to the root repository folder and ensure it contains the following: 

```properties
USERNAME=<your github username>
TOKEN=<your github personal access token>
```

The library and source code are public. However, github requires an access token only to download the dependency.

Ensure you add the following lines to either `settings.gradle` or your module `build.gradle` (wherever you define the repositories from which your dependencies are resolved).

```groovy
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven {
            url = uri("https://maven.pkg.github.com/sasirekha3/resumable-upload-wizard")

            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}
```

Add the following dependencies:

```groovy
    // Additional dependencies
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'androidx.work:work-runtime-ktx:2.7.1'
    implementation 'com.squareup.okhttp3:okhttp:3.2.0'

    // Resumable Upload Library
    implementation 'com.sasirekha:resumableuploadwizard:2.0'
```

### Configuration and Setup

Make sure you instantiate WorkManager explicitly in order to be able to configure constraints by implementing `Configuration.Provider` in your application class.

For example:

```kotlin
package com.pdiot.backupfilestogcp

import android.app.Application
import androidx.work.Configuration

class BackupFilesToGCP(): Application(), Configuration.Provider {

    // Configure work manager manually
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMaxSchedulerLimit(21)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
```


### Performing the upload

You will need the `contentUri` of the file to perform this upload. In the following example, this is stored in the variable `filePath`.

#### WorkManager Setup

Establish constraints for the upload:

```kotlin
// Specify constraints to upload file
val constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()
```

#### File Integrity

To ensure integrity of the uploaded file, a checksum is attached to the file metadata that is automatically used for verification by GCP. Create the checksum using `MD5` class from this library:

```kotlin
// Calculate MD5 checksum of the file by reading it
val fileDescriptor = context.contentResolver.openFileDescriptor(filePath, "r")
val fileSize: Long? = fileDescriptor?.statSize
fileDescriptor?.close()
val md5 = com.sasirekha.resumableuploadwizard.models.MD5(context)
var checksum: String? = null
try {
    // calculate checksum of file contents
    checksum = md5.calculateMD5(filePath)

} catch(e: Exception) {
    Log.e(TAG, "Unable to calculate MD5 checksum of file contents")
    return
}
```

#### Object Metadata

Object Metadata is metadata that is uploaded in the form of headers to GCP. It is associated with the file and can be processed using cloud function triggers. We take advantage of this feature to specify additional information such as fileType, etc.

```kotlin
// Create a request ID for tracking
        val requestId = UUID.randomUUID()

        // Create metadata to be stored with the file
        val metadata = ObjectMetadata(
            requestId.toString(), username, Date().time, fileName,
            "text/csv", fileSize!!, checksum!!
        )
```

#### Upload Configuration

Specify an upload configuration using fields from all of the above steps:

```kotlin
// Create upload config
val config = UploadConfiguration(0, BuildConfig.uploadchunksizebytes.toInt(),
    "text/csv", filePath, fileSize!!, checksum!!, URL(BuildConfig.gcpapiurl),
    "x-api-key", BuildConfig.gcpapixapikey, metadata.getMap())
```

#### Instantiate the Manager 

Set existing work policy to `KEEP` to avoid sending multiple upload requests for the same file before the previous one is completed.

```kotlin
// Set work policy
val existingWorkPolicy = ExistingWorkPolicy.KEEP

// Instantiate the manager class
val mgr = Manager(context, config, constraints, existingWorkPolicy)

// Get work requests for progress tracking
val workRequests = mgr.getWorkRequests()
val workId = mgr.workId

// Get last request
val lastWorkRequest = workRequests[workRequests.size - 1]
```

#### Start the upload

```kotlin
// Start processing
mgr.enqueueWork()
```

#### Configure an observer to send a notification after the upload is complete

```kotlin
// Notify after last request is complete by attaching an observer to the Live Data of the work
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



