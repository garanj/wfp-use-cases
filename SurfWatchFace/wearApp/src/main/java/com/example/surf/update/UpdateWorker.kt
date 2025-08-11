package com.example.surf.update

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.surf.R
import com.example.surf.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val defaultWatchFaceName = "default_watchface.apk"

/**
 * WorkManager worker that tries to update the default watch face, if installed.
 *
 * Checks which watch faces the package already has installed, and if there is a default watch face
 * in the assets bundle. Compares the versions of these to determine whether an update is necessary
 * and if so, updates the default watch face, taking also the new watch face validation token from
 * the manifest file.
 */
class UpdateWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val watchFacePushManager = WatchFacePushManagerFactory.createWatchFacePushManager(appContext)

        val watchFaces = watchFacePushManager.listWatchFaces().installedWatchFaceDetails
            .associateBy { it.packageName }

        try {
            val copiedFile = File.createTempFile("tmp", ".apk", appContext.cacheDir)
            copiedFile.deleteOnExit()
            appContext.assets.open(defaultWatchFaceName).use { inputStream ->
                FileOutputStream(copiedFile).use { outputStream -> inputStream.copyTo(outputStream) }
            }
            val packageInfo =
                appContext.packageManager.getPackageArchiveInfo(copiedFile.absolutePath, 0)

            packageInfo?.let { newPkg ->
                watchFaces[newPkg.packageName]?.let { curPkg ->
                    if (newPkg.longVersionCode > curPkg.versionCode) {
                        val pfd = ParcelFileDescriptor.open(
                            copiedFile,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                        val token = appContext.getString(R.string.default_wf_token)
                        watchFacePushManager.updateWatchFace(curPkg.slotId, pfd, token)
                        pfd.close()
                    }
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Watch face not updated", e)
        }
        return Result.success()
    }
}