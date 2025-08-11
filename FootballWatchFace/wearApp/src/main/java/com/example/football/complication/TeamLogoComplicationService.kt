package com.example.football.complication

import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.football.R
import com.example.football.data.StatusManager
import com.example.football.data.Team
import kotlinx.coroutines.flow.first


class TeamLogoComplicationService() : SuspendingComplicationDataSourceService() {
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

        return createTeamLogoComplication(team, hasOpened)
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
        return createTeamLogoComplication(team, true)
    }

    private fun createTeamLogoComplication(
        team: Team,
        hasOpened: Boolean
    ): ShortTextComplicationData {
        val status = if (hasOpened) {
            "CONFIGURED"
        } else {
            "NOT_CONFIGURED"
        }
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Status").build()
        )
            .apply {
                if (!hasOpened) {
                    val blankBitmap = createBitmap(1, 1)
                    val monochromaticImage =
                        MonochromaticImage.Builder(Icon.createWithBitmap(blankBitmap))
                    setMonochromaticImage(monochromaticImage.build())
                } else {
                    team.logo?.let { byteArray ->
                        if (byteArray.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            if (bitmap != null) {
                                Log.d("TeamComplicationService", "Loaded bitmap: $bitmap")
                                val monochromaticImage =
                                    MonochromaticImage.Builder(Icon.createWithBitmap(bitmap))
                                setMonochromaticImage(monochromaticImage.build())
                            }
                        }
                    }
                }
            }
            .build()
    }
}