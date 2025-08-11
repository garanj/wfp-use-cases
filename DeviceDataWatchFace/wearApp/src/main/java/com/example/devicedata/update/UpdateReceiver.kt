package com.example.devicedata.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.devicedata.boot.ServiceStartupWorker

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

            val serviceStartUpRequest = OneTimeWorkRequestBuilder<ServiceStartupWorker>()
                .build()
            workManager.enqueue(serviceStartUpRequest)
        }
    }
}
