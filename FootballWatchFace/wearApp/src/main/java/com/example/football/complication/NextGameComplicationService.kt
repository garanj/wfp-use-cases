package com.example.football.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.football.data.StatusManager
import kotlinx.coroutines.flow.first
import kotlin.random.Random


class NextGameComplicationService() : SuspendingComplicationDataSourceService() {
    val statusManager by lazy { StatusManager(applicationContext) }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val hasOpened = statusManager.hasOpenedOnce.first()
        return createNextGameComplication(hasOpened)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createNextGameComplication(true)
    }

    private fun createNextGameComplication(hasOpened: Boolean): ShortTextComplicationData {
        val status = if (hasOpened) {
            "CONFIGURED"
        } else {
            "NOT_CONFIGURED"
        }
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Status").build()
        )
            .setTitle(PlainComplicationText.Builder(getRandomDayAndTime()).build())
            .build()
    }

    fun getRandomDayAndTime(): String {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val times = mutableListOf<String>()
        for (hour in 18..20) {
            for (minute in 0..45 step 15) {
                times.add(String.format("%02d:%02d", hour, minute))
            }
        }
        times.add("21:00")

        val randomDay = days[Random.nextInt(days.size)]
        val randomTime = times[Random.nextInt(times.size)]

        return "$randomDay, $randomTime"
    }
}