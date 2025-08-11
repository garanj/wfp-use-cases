package com.example.devicedata.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Receives a broadcast when the device is rebooted.
 *
 * This triggers a WorkManager job to start the service. A WorkManager job is used to avoid trying
 * to complete the service start up in the time allowed by the onReceive method, as this time may
 * be insufficient time.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceStartUpRequest = OneTimeWorkRequestBuilder<ServiceStartupWorker>()
                .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(serviceStartUpRequest)
        }
    }
}