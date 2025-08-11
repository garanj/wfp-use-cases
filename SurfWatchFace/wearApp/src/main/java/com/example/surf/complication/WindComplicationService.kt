package com.example.surf.complication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.surf.data.StatusManager
import kotlinx.coroutines.flow.first
import kotlin.math.min
import kotlin.time.Duration.Companion.hours


data class WindData(
    val speed: Double,
    val direction: Double,
    val timestamp: Long
)

/**
 * A mock function to simulate fetching wind data. In a real application, this would
 * involve making a network request or querying a database.
 */
private fun getNextWindData(): List<WindData> {
    // In a real app, you would fetch this data from an API.
    // These are placeholder values for demonstration.
    return listOf(
        WindData(
            speed = 10.0,
            direction = 280.0,
            timestamp = System.currentTimeMillis()
        ),
        WindData(
            speed = 15.0,
            direction = 310.0,
            timestamp = System.currentTimeMillis() + 3.hours.inWholeMilliseconds
        ),
        WindData(
            speed = 12.0,
            direction = 30.0,
            timestamp = System.currentTimeMillis() + 6.hours.inWholeMilliseconds
        )
    )
}


/**
 * The complication service that provides wind data to the watch face.
 */
class WindComplicationService : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(this) }

    /**
     * This is the primary method for providing complication data. It is called by the system
     * whenever the complication needs to be updated.
     *
     * @param request The request object containing details about the complication.
     * @return The ComplicationData to be displayed, or null if the request is not supported.
     */
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.RANGED_VALUE) {
            return null
        }

        if (!statusManager.surfEnabled.first()) {
            return notConfigured()
        }

        val windDataList = getNextWindData()

        Log.i("TAG", "Wind data: $windDataList")
        Log.i("TAG", "State: $windStateMap")
        Log.i("TAG", "Instance Id: ${request.complicationInstanceId}")

        val windIndex =
            min(windDataList.size - 1, windStateMap.getValue(request.complicationInstanceId))

        val text: String
        val title: String

        val windData = windDataList[windIndex]

        title = windData.speed.toString()
        text = windData.timestamp.toString()

        return RangedValueComplicationData.Builder(
            value = windData.direction.toFloat(),
            min = 0f,
            max = 360f,
            contentDescription = PlainComplicationText.Builder("Wind information").build()
        )
            .setText(PlainComplicationText.Builder(text).build())
            .setTitle(PlainComplicationText.Builder(title).build())
            .setTapAction(createTapIntent(this, request.complicationInstanceId))
            .build()
    }

    /**
     * Provides preview data for the watch face complication editor. This is used to show
     * the user what the complication will look like before they add it.
     *
     * @param type The type of complication for which to generate preview data.
     * @return Preview ComplicationData.
     */
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.RANGED_VALUE) {
            return null
        }
        return RangedValueComplicationData.Builder(
            value = 210f,
            min = 0f,
            max = 360f,
            contentDescription = PlainComplicationText.Builder("Wind Information").build()
        )
            .setText(PlainComplicationText.Builder("10.2").build())
            .setTitle(PlainComplicationText.Builder(System.currentTimeMillis().toString()).build())
            .build()
    }

    private fun createTapIntent(context: Context, complicationInstanceId: Int): PendingIntent {
        val intent = WindComplicationToggleReceiver.getToggleIntent(context, complicationInstanceId)
        return PendingIntent.getBroadcast(
            context,
            complicationInstanceId, // Use instance ID as the request code to ensure uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notConfigured(): ComplicationData {
        return RangedValueComplicationData.Builder(
            value = 0f,
            min = 0f,
            max = 20f,
            contentDescription = PlainComplicationText.Builder("Swell Information").build()
        )
            .setText(PlainComplicationText.Builder("NOT_CONFIGURED").build())
            .build()
    }

    companion object {
        val windStateMap = mutableMapOf<Int, Int>().withDefault { 0 }
    }
}

/**
 * A BroadcastReceiver that handles tap events on the complication.
 */
class WindComplicationToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val complicationInstanceId = intent.getIntExtra(EXTRA_COMPLICATION_ID, -1)
        if (complicationInstanceId == -1) return

        val windData = getNextWindData()

        val currentState = WindComplicationService.windStateMap[complicationInstanceId] ?: 0
        WindComplicationService.windStateMap[complicationInstanceId] =
            (currentState + 1) % windData.size

        // Create a requester to trigger an update for the complication.
        val requester = ComplicationDataSourceUpdateRequester.create(
            context = context,
            complicationDataSourceComponent = ComponentName(
                context,
                WindComplicationService::class.java
            )
        )
        // Request an immediate update for the specific complication instance.
        requester.requestUpdate(complicationInstanceId)
    }

    companion object {
        private const val EXTRA_COMPLICATION_ID = "com.example.surf.wind.COMPLICATION_ID"

        fun getToggleIntent(context: Context, complicationInstanceId: Int): Intent {
            return Intent(context, WindComplicationToggleReceiver::class.java).apply {
                putExtra(EXTRA_COMPLICATION_ID, complicationInstanceId)
            }
        }
    }
}
