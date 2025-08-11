package com.example.devicedata.service

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.devicedata.MainActivity
import com.example.devicedata.R
import com.example.devicedata.data.DeviceData
import com.example.devicedata.data.StatusManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class DataService : LifecycleService() {
    private val statusManager by lazy { StatusManager(this) }
    private var running = false
    private val pointsList = mutableListOf<Double>().apply {
        repeat(10) { add(0.0) }
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForegroundService()
        if (!running) {
            running = true
            /*

            Here we are creating a loop and populating with example data, but in reality, for a
            connected device with an established BT connection, we should not be doing this.

            Instead, when the BT device provides new data, via a callback, that is when the new
            data is written to the data store.

            Separately, AlarmManager could be used to set an alarm to trigger if no data has been
            received before a certain time out.

             */

            lifecycleScope.launch {
                dataLoop()
            }
        }
        return START_STICKY
    }

    private suspend fun dataLoop() {
        while (true) {
            pointsList.add(Random.nextDouble())
            pointsList.removeAt(0)
            val deviceData = DeviceData(
                timestamp = System.currentTimeMillis(),
                readings = pointsList.toList()
            )
            statusManager.setDeviceData(deviceData)
            delay(10_000)
        }
    }

    private val activityPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onDestroy() {
        val component = ComponentName(this, StatusComplicationService::class.java)
        val requester = ComplicationDataSourceUpdateRequester.create(this, component)
        requester.requestUpdateAll()
        super.onDestroy()
    }

    private val ongoingActivityStatus = Status.Builder()
        .addTemplate("Ongoing")
        .build()

    private fun startForegroundService() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.health)
            .setOngoing(true)

        val ongoingActivity =
            OngoingActivity.Builder(
                applicationContext, NOTIFICATION_ID, notificationBuilder
            )
                .setAnimatedIcon(R.drawable.health)
                .setStaticIcon(R.drawable.health)
                .setTouchIntent(activityPendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

        ongoingActivity.apply(applicationContext)

        // Start the service in the foreground
        startForeground(
            NOTIFICATION_ID,
            notificationBuilder.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )

        // Add your code here to execute
        // For example, download a file, play music, etc.
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    companion object {
        const val CHANNEL_ID = "DataServiceChannel"
        const val NOTIFICATION_ID = 1
        const val TAG = "DataService"
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        @Suppress("DEPRECATION")
        fun isDataServiceRunning(context: Context): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            return manager.getRunningServices(Integer.MAX_VALUE).any {
                it.service.className == DataService::class.java.name
            }
        }
    }
}