package com.example.devicedata.boot

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.devicedata.TAG
import com.example.devicedata.data.StatusManager
import com.example.devicedata.service.DataService
import kotlinx.coroutines.flow.first

/**
 * A WorkManager job to start the data service, checking first for the necessary permissions.
 */
class ServiceStartupWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private val requiredPermissions = setOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    override suspend fun doWork(): Result {
        val statusManager = StatusManager(appContext)
        val serviceEnabled = statusManager.serviceEnabled.first()
        if (checkAllPermissions(appContext)) {
            if (serviceEnabled && !DataService.isDataServiceRunning(appContext)) {
                val fgsIntent = Intent(appContext, DataService::class.java)
                appContext.startForegroundService(fgsIntent)
            }
        } else {
            Log.w(TAG, "Missing required permissions to start service")
        }
        return Result.success()
    }

    private fun checkAllPermissions(context: Context) = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}