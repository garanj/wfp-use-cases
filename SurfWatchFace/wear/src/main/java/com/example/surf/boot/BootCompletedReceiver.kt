package com.example.surf.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager

/**
 * Receives a broadcast when the device is rebooted.
 *
 * This checks for the presence already of a WorkManager job to do surf download from the
 * album service. If not, one is created and downloads happen whilst the device is charging.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)

            // TODO: Check for the existence of the surf downloader job and add a periodic worker
            // if necessary. The periodic downloader should use a service such as stormglass.io
        }
    }
}

