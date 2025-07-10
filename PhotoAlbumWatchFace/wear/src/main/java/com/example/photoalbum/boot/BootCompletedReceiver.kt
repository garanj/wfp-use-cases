package com.example.photoalbum.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.photoalbum.data.StatusManager
import com.example.photoalbum.photos.WORKER_TAG
import com.example.photoalbum.photos.checkAndAddPeriodicWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Receives a broadcast when the device is rebooted.
 *
 * This checks for the presence already of a WorkManager job to do photos download from the
 * album service. If not, one is created and downloads happen whilst the device is charging.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)
            val workers = workManager.getWorkInfosByTag(WORKER_TAG)
            if (workers.get().isEmpty()) {
                val statusManager = StatusManager(context)
                runBlocking {
                    if (statusManager.photosEnabled.first()) {
                        checkAndAddPeriodicWorker(context)
                    }
                }
            }
        }
    }
}

