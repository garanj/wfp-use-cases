package com.example.palette.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.palette.data.Palette
import com.example.palette.data.StatusManager
import kotlinx.coroutines.flow.first

/**
 * A Complication data source that provides the color palette information used to theme the watch
 * face.
 */
class PaletteComplicationService() : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(applicationContext) }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val hasOpened = statusManager.hasOpenedOnce.first()
        val palette = statusManager.palette.first()

        return createPaletteComplication(palette, hasOpened)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createPaletteComplication(Palette(), true)
    }

    /**
     * Uses SHORT_TEXT to supply the palette information. The following fields are populated:
     *
     * - TEXT: Contains a space-delimited list of the primary, secondary, and tertiary colors, e.g.
     *   "#FF0000 #00FF00 #0000FF"
     * - TITLE: Indicates whether the app has been launched before (CONFIGURED) or not (NOT_CONFIGURED).
     *   This is needed as the watch face should prompt the user to open the app once if they
     *   haven't - the app needs to have run in order to receive MY_PACKAGE_UPDATED broadcasts.
     */
    private fun createPaletteComplication(
        palette: Palette,
        hasOpened: Boolean
    ): ShortTextComplicationData {
        val status = if (hasOpened) {
            "CONFIGURED"
        } else {
            "NOT_CONFIGURED"
        }
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(palette.toColorList()).build(),
            contentDescription = PlainComplicationText.Builder("Team theme").build()
        )
            .setTitle(PlainComplicationText.Builder(status).build())
            .build()
    }
}

fun colorToHexString(color: Int) = String.format("#%06X", 0xFFFFFF and color)