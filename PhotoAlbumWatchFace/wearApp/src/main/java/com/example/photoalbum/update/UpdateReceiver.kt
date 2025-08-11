package com.example.photoalbum.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.photoalbum.data.StatusManager
import com.example.photoalbum.photos.checkAndAddPeriodicWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Updates the watch face, if necessary, when the overall app is updated, if the app contains a
 * newer default watch face within it.
 *
 * Uses a WorkManager job to avoid trying to complete this within the time allowed for the
 * onReceive.
 */
class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            val updateRequest = OneTimeWorkRequestBuilder<UpdateWorker>().build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(updateRequest)

            val statusManager = StatusManager(context)
            runBlocking {
                if (statusManager.photosEnabled.first()) {
                    checkAndAddPeriodicWorker(context)
                }
            }
        }
    }
}
