package com.example.photoalbum.photos

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.photoalbum.TAG
import com.example.photoalbum.data.updateComplications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

const val WORKER_TAG = "photo_album_worker"

class PhotosWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_SCREEN_WIDTH = "screen_width"
        const val KEY_IMAGE_PATHS = "image_paths" // To return final file paths
        const val TARGET_SUBDIRECTORY = "photos"
        const val TEMP_SUBDIRECTORY_PREFIX = "temp_photos_"
        const val EXPECTED_IMAGE_COUNT = 4
        const val TAG = "PhotosWorker"
    }

    override suspend fun doWork(): Result {
        val screenWidth = inputData.getInt(KEY_SCREEN_WIDTH, 400)

        // Create a unique temporary directory for this download session
        val tempDirName = "$TEMP_SUBDIRECTORY_PREFIX${UUID.randomUUID()}"
        val tempDirectory = File(applicationContext.filesDir, tempDirName)
        val targetDirectory = File(applicationContext.filesDir, TARGET_SUBDIRECTORY)

        val downloadedImageTempPaths = mutableListOf<String>()
        val finalImagePaths = mutableListOf<String>()

        return withContext(Dispatchers.IO) {
            try {
                if (!tempDirectory.mkdirs()) {
                    return@withContext Result.failure()
                }
                Log.d(TAG, "Created temporary directory: ${tempDirectory.absolutePath}")

                for (i in 1..EXPECTED_IMAGE_COUNT) {
                    val imageUrl = "https://picsum.photos/$screenWidth?random=$i"
                    val uniqueID = UUID.randomUUID().toString()
                    val fileName = "$uniqueID.jpg"
                    val outputFile = File(tempDirectory, fileName) // Save to temp directory

                    Log.d(TAG, "Downloading image from: $imageUrl to ${outputFile.absolutePath}")

                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000 // 15 seconds
                    connection.readTimeout = 15000   // 15 seconds
                    connection.doInput = true
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Server returned HTTP ${connection.responseCode} for $imageUrl")
                        // If one image fails, we might want to abort the whole batch
                        // Clean up temp directory and fail
                        tempDirectory.deleteRecursively()
                        // Log.w(TAG, "Aborting download due to server error. Deleted temp directory.")
                        return@withContext Result.failure() // Or decide to continue and fail at the end if not all 10 are present
                    }

                    connection.inputStream.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    downloadedImageTempPaths.add(outputFile.absolutePath)
                    Log.d(TAG, "Successfully downloaded to temp: ${outputFile.absolutePath}")
                    delay(1_000)
                }

                // Check if all images were downloaded
                if (downloadedImageTempPaths.size == EXPECTED_IMAGE_COUNT) {
                    Log.i(
                        TAG,
                        "All $EXPECTED_IMAGE_COUNT images downloaded successfully to temporary directory."
                    )

                    // Delete existing target "photos" directory if it exists
                    if (targetDirectory.exists()) {
                        Log.d(
                            TAG,
                            "Deleting existing target directory: ${targetDirectory.absolutePath}"
                        )
                        if (!targetDirectory.deleteRecursively()) {
                            Log.e(
                                TAG,
                                "Failed to delete existing target directory: ${targetDirectory.absolutePath}"
                            )
                            // Clean up temp directory as we can't replace the old one
                            tempDirectory.deleteRecursively()
                            return@withContext Result.failure()
                        }
                        Log.d(TAG, "Successfully deleted existing target directory.")
                    }

                    // Rename temporary directory to target "photos" directory
                    Log.d(
                        TAG,
                        "Attempting to rename ${tempDirectory.absolutePath} to ${targetDirectory.absolutePath}"
                    )
                    if (tempDirectory.renameTo(targetDirectory)) {
                        Log.i(
                            TAG,
                            "Successfully renamed temporary directory to target directory: ${targetDirectory.absolutePath}"
                        )
                        // Update paths to reflect the new location in the "photos" directory
                        downloadedImageTempPaths.forEach { tempPath ->
                            val fileName = File(tempPath).name
                            finalImagePaths.add(File(targetDirectory, fileName).absolutePath)
                        }
                        val outputData =
                            workDataOf(KEY_IMAGE_PATHS to finalImagePaths.toTypedArray())
                        updateComplications(applicationContext)
                        return@withContext Result.success(outputData)
                    } else {
                        Log.e(TAG, "Failed to rename temporary directory to target directory.")
                        // Clean up temp directory as rename failed
                        tempDirectory.deleteRecursively()
                        return@withContext Result.failure()
                    }
                } else {
                    Log.w(
                        TAG,
                        "Not all images were downloaded (${downloadedImageTempPaths.size}/${EXPECTED_IMAGE_COUNT}). Cleaning up."
                    )
                    // Clean up temporary directory as not all images were downloaded
                    tempDirectory.deleteRecursively()
                    return@withContext Result.failure()
                }

            } catch (e: IOException) {
                Log.e(TAG, "IOException during image download or file operation", e)
                tempDirectory.deleteRecursively() // Clean up temp directory on error
                return@withContext Result.failure()
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred", e)
                tempDirectory.deleteRecursively() // Clean up temp directory on error
                return@withContext Result.failure()
            } finally {
                // Ensure temp directory is deleted if it still exists and wasn't successfully renamed
                // This handles cases where an exception might occur after temp dir creation but before rename/delete
                if (tempDirectory.exists() && tempDirectory.name.startsWith(TEMP_SUBDIRECTORY_PREFIX)) {
                    Log.d(
                        TAG,
                        "Final cleanup: Deleting residual temporary directory ${tempDirectory.absolutePath}"
                    )
                    tempDirectory.deleteRecursively()
                }
            }
        }
    }
}

fun checkAndAddPeriodicWorker(context: Context) {
    val workManager = WorkManager.getInstance(context)
    Log.d(TAG, "Adding periodic photo album downloader worker")

    // The downloading of new images must only take place when there is a network
    // connection and the device is on charge.
    val constraints = Constraints.Builder()
        .setRequiresCharging(true)
        .setRequiresStorageNotLow(true)
        .setRequiresBatteryNotLow(true)
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .build()

    val inputData = Data.Builder()
        .putInt(PhotosWorker.KEY_SCREEN_WIDTH, context.resources.displayMetrics.widthPixels)
        .build()

    val request =
        PeriodicWorkRequestBuilder<PhotosWorker>(repeatInterval = 12.hours.toJavaDuration())
            .addTag(WORKER_TAG)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
    workManager.enqueue(request)
}

fun queueImmediateWorker(context: Context) {
    val workManager = WorkManager.getInstance(context)


    val inputData = Data.Builder()
        .putInt(PhotosWorker.KEY_SCREEN_WIDTH, context.resources.displayMetrics.widthPixels)
        .build()

    val request = OneTimeWorkRequestBuilder<PhotosWorker>()
        .addTag(WORKER_TAG)
        .setInputData(inputData)
        .build()
    workManager.enqueueUniqueWork("immediate_download", ExistingWorkPolicy.KEEP, request)
}

fun removePeriodicWorker(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val periodicWork = workManager.cancelAllWorkByTag(WORKER_TAG)
}