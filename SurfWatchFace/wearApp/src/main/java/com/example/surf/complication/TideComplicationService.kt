package com.example.surf.complication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.surf.R
import com.example.surf.data.StatusManager
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A data class to hold the information about the next tides.
 * This would typically be populated by a network call or a local database.
 */
data class TideData(
    val highTideTime: LocalTime,
    val lowTideTime: LocalTime,
    val highTideHeight: Double,
    val lowTideHeight: Double,
    val tideProgress: Float // A value from 0.0f (low) to 100.0f (high)
)

/**
 * A mock function to simulate fetching tide data. In a real application, this would
 * involve making a network request or querying a database.
 */
private fun getNextTideData(): TideData {
    // In a real app, you would fetch this data from an API.
    // These are placeholder values for demonstration.
    return TideData(
        highTideTime = LocalTime.of(14, 52),
        lowTideTime = LocalTime.of(8, 31),
        highTideHeight = 2.1,
        lowTideHeight = 0.4,
        tideProgress = 65.0f
    )
}


/**
 * The complication service that provides tide data to the watch face.
 */
class TideComplicationService : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(this) }

    // The formatter for displaying time in the complication.
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

        val tideData = getNextTideData()

        // Determine whether to show high tide or low tide data based on our state map.
        // If no state is stored for this instance, default to showing high tide.
        val showHighTide = tideStateMap[request.complicationInstanceId] ?: true

        val text: String
        val title: String
        val icon: Icon

        if (showHighTide) {
            text = tideData.highTideTime.format(timeFormatter)
            title = "%.1f".format(tideData.highTideHeight)
            icon = Icon.createWithResource(this, R.drawable.tide_high)
        } else {
            text = tideData.lowTideTime.format(timeFormatter)
            title = "%.1f".format(tideData.lowTideHeight)
            icon = Icon.createWithResource(this, R.drawable.tide_low)
        }

        return RangedValueComplicationData.Builder(
            value = tideData.tideProgress,
            min = 0f,
            max = 100f,
            contentDescription = PlainComplicationText.Builder("Tide Information").build()
        )
            .setText(PlainComplicationText.Builder(text).build())
            .setTitle(PlainComplicationText.Builder(title).build())
            .setMonochromaticImage(MonochromaticImage.Builder(icon).build())
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
            value = 75f,
            min = 0f,
            max = 100f,
            contentDescription = PlainComplicationText.Builder("Tide Information").build()
        )
            .setText(PlainComplicationText.Builder("14:52").build())
            .setTitle(PlainComplicationText.Builder("9.1").build())
            .build()
    }

    /**
     * Creates a PendingIntent that will be triggered when the complication is tapped.
     * This intent will be handled by the [TideComplicationToggleReceiver].
     */
    private fun createTapIntent(context: Context, complicationInstanceId: Int): PendingIntent {
        val intent = TideComplicationToggleReceiver.getToggleIntent(context, complicationInstanceId)
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
            contentDescription = PlainComplicationText.Builder("Tide Information").build()
        )
            .setText(PlainComplicationText.Builder("NOT_CONFIGURED").build())
            .build()
    }

    companion object {
        // A simple in-memory map to store the toggle state (show high vs. low tide)
        // for each complication instance. In a real app, this should be persisted.
        val tideStateMap = mutableMapOf<Int, Boolean>()
    }
}

/**
 * A BroadcastReceiver that handles tap events on the complication.
 */
class TideComplicationToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val complicationInstanceId = intent.getIntExtra(EXTRA_COMPLICATION_ID, -1)
        if (complicationInstanceId == -1) return

        val currentState = TideComplicationService.tideStateMap[complicationInstanceId] ?: true
        TideComplicationService.tideStateMap[complicationInstanceId] = !currentState

        // Create a requester to trigger an update for the complication.
        val requester = ComplicationDataSourceUpdateRequester.create(
            context = context,
            complicationDataSourceComponent = ComponentName(
                context,
                TideComplicationService::class.java
            )
        )
        // Request an immediate update for the specific complication instance.
        requester.requestUpdate(complicationInstanceId)
    }

    companion object {
        private const val EXTRA_COMPLICATION_ID = "com.example.surf.tide.COMPLICATION_ID"

        fun getToggleIntent(context: Context, complicationInstanceId: Int): Intent {
            return Intent(context, TideComplicationToggleReceiver::class.java).apply {
                putExtra(EXTRA_COMPLICATION_ID, complicationInstanceId)
            }
        }
    }
}
