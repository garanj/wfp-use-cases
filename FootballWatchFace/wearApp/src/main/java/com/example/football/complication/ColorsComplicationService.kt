package com.example.football.complication

import androidx.core.graphics.toColorInt
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.football.R
import com.example.football.data.StatusManager
import com.example.football.data.Team
import kotlinx.coroutines.flow.first


class ColorsComplicationService() : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(applicationContext) }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val hasOpened = statusManager.hasOpenedOnce.first()
        var team = statusManager.team.first()

        if (team.name.isEmpty()) {
            team = Team.create(
                context = this,
                primaryColor = "#FF0000".toColorInt(),
                secondaryColor = "#00FF00".toColorInt(),
                tertiaryColor = "#0000FF".toColorInt(),
                name = "My Team",
                logoResourceId = R.drawable.android_bitmap
            )
        }

        return createColorsComplication(team, hasOpened)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val team = Team.create(
            context = this,
            primaryColor = "#FF0000".toColorInt(),
            secondaryColor = "#00FF00".toColorInt(),
            tertiaryColor = "#0000FF".toColorInt(),
            name = "My Team",
            logoResourceId = R.drawable.android_bitmap
        )
        return createColorsComplication(team, true)
    }

    private fun createColorsComplication(
        team: Team,
        hasOpened: Boolean
    ): ShortTextComplicationData {
        val status = if (hasOpened) {
            "CONFIGURED"
        } else {
            "NOT_CONFIGURED"
        }
        val builder = ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Status").build()
        )
            .setTitle(PlainComplicationText.Builder(team.toColorList()).build())
        return builder.build()
    }
}

fun colorToHexString(color: Int) = String.format("#%06X", 0xFFFFFF and color)
