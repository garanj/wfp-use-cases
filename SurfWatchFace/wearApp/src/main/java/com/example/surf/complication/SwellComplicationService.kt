package com.example.surf.complication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.surf.data.StatusManager
import kotlinx.coroutines.flow.first
import java.time.Duration
import kotlin.math.min
import kotlin.time.Duration.Companion.hours


data class SwellData(
    val height: Double,
    val period: Int,
    val timestamp: Long
)

/**
 * A mock function to simulate fetching Swell data. In a real application, this would
 * involve making a network request or querying a database.
 */
private fun getNextSwellData(): List<SwellData> {
    // In a real app, you would fetch this data from an API.
    // These are placeholder values for demonstration.
    return listOf(
        SwellData(
            height = 5.0,
            period = 15,
            timestamp = System.currentTimeMillis()
        ),
        SwellData(
            height = 6.0,
            period = 17,
            timestamp = System.currentTimeMillis() + 3.hours.inWholeMilliseconds
        ),
        SwellData(
            height = 3.0,
            period = 14,
            timestamp = System.currentTimeMillis() + 6.hours.inWholeMilliseconds
        )
    )
}


/**
 * The complication service that provides Swell data to the watch face.
 */
class SwellComplicationService : SuspendingComplicationDataSourceService() {
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

        val swellDataList = getNextSwellData()

        val swellIndex =
            min(swellDataList.size - 1, swellStateMap.getValue(request.complicationInstanceId))

        val text: String
        val title: String

        val swellData = swellDataList[swellIndex]

        title = "%.1fm @ %ds".format(swellData.height, swellData.period)
        text = swellData.timestamp.toString()

        return RangedValueComplicationData.Builder(
            value = swellData.period.toFloat(),
            min = 0f,
            max = swellDataList.maxOf { it.period }.toFloat(),
            contentDescription = PlainComplicationText.Builder("Swell information").build()
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
            value = 17f,
            min = 0f,
            max = 20f,
            contentDescription = PlainComplicationText.Builder("Swell Information").build()
        )
            .setText(PlainComplicationText.Builder("10.2").build())
            .setTitle(PlainComplicationText.Builder(System.currentTimeMillis().toString()).build())
            .build()
    }

    private fun createTapIntent(context: Context, complicationInstanceId: Int): PendingIntent {
        val intent =
            SwellComplicationToggleReceiver.getToggleIntent(context, complicationInstanceId)
        return PendingIntent.getBroadcast(
            context,
            complicationInstanceId, // Use instance ID as the request code to ensure uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun formatTimeDifference(timeMillis: Long): String {
        val duration = Duration.ofMillis(timeMillis - System.currentTimeMillis())
        val absDuration = duration.abs()

        return when {
            absDuration.toHours() >= 1 -> {
                val sign = if (duration.isNegative) "-" else "+"
                val hours = absDuration.toHours()
                "$sign$hours h"
            }

            absDuration.toMinutes() >= 5 -> {
                val sign = if (duration.isNegative) "-" else "+"
                val minutes = absDuration.toMinutes()
                "$sign$minutes min"
            }

            else -> "Now"
        }
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
        val swellStateMap = mutableMapOf<Int, Int>().withDefault { 0 }
    }
}

/**
 * A BroadcastReceiver that handles tap events on the complication.
 */
class SwellComplicationToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val complicationInstanceId = intent.getIntExtra(EXTRA_COMPLICATION_ID, -1)
        if (complicationInstanceId == -1) return

        val swellData = getNextSwellData()

        val currentState = SwellComplicationService.swellStateMap[complicationInstanceId] ?: 0
        SwellComplicationService.swellStateMap[complicationInstanceId] =
            (currentState + 1) % swellData.size

        // Create a requester to trigger an update for the complication.
        val requester = ComplicationDataSourceUpdateRequester.create(
            context = context,
            complicationDataSourceComponent = ComponentName(
                context,
                SwellComplicationService::class.java
            )
        )
        // Request an immediate update for the specific complication instance.
        requester.requestUpdate(complicationInstanceId)
    }

    companion object {
        private const val EXTRA_COMPLICATION_ID = "com.example.surf.swell.COMPLICATION_ID"

        fun getToggleIntent(context: Context, complicationInstanceId: Int): Intent {
            return Intent(context, SwellComplicationToggleReceiver::class.java).apply {
                putExtra(EXTRA_COMPLICATION_ID, complicationInstanceId)
            }
        }
    }
}
