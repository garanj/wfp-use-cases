package com.example.surf.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.surf.data.StatusManager
import kotlinx.coroutines.flow.first


private fun getCurrentWaterTemp(): Double {
    // Replace this with your actual data fetching logic
    return 18.2345
}

class WaterTempComplicationService : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(this) }

    /**
     * Called when the watch face needs data for the complication. This function
     * is executed on a background thread.
     *
     * @param request The request details, including the complication ID and type.
     * @return The [ComplicationData] to be displayed, or null if the request can't be met.
     */
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) {
            return null
        }

        if (!statusManager.surfEnabled.first()) {
            return notConfigured()
        }

        val temp = getCurrentWaterTemp()
        val tempText = String.format("%.1f°", temp)

        val text = PlainComplicationText.Builder(text = tempText).build()
        val contentDescription = PlainComplicationText.Builder(
            text = "Current water temperature is $tempText"
        ).build()

        return ShortTextComplicationData.Builder(
            text = text,
            contentDescription = contentDescription
        )
            .setTitle(PlainComplicationText.Builder("Water").build()) // Optional title
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {

        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }

        val previewText = "18.2°"
        val text = PlainComplicationText.Builder(text = previewText).build()
        val contentDescription = PlainComplicationText.Builder(
            text = "Water temperature"
        ).build()

        // Build and return the preview data.
        return ShortTextComplicationData.Builder(
            text = text,
            contentDescription = contentDescription
        )
            .setTitle(PlainComplicationText.Builder("Water").build())
            .build()
    }

    private fun notConfigured(): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("NOT_CONFIGURED").build(),
            contentDescription = PlainComplicationText.Builder("Tide Information").build()
        )
            .build()
    }
}