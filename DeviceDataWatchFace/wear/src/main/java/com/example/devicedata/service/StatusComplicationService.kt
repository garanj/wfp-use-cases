package com.example.devicedata.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.core.net.toUri
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.devicedata.MainActivity
import com.example.devicedata.data.StatusManager
import com.example.devicedata.graph.createGraph
import kotlinx.coroutines.flow.first

enum class DataServiceStatus {
    ENABLED,
    DISABLED
}

class StatusComplicationService() : SuspendingComplicationDataSourceService() {
    private val GRAPH_SIZE = 250

    val statusManager by lazy { StatusManager(this) }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val enabled = statusManager.serviceEnabled.first()
        if (enabled) {
            val deviceData = statusManager.deviceData.first()
            val image = createGraph(GRAPH_SIZE, GRAPH_SIZE, deviceData.readings)

            val icon = Icon.createWithBitmap(image)
            return deviceDataComplication(
                status = DataServiceStatus.ENABLED.name,
                lastUpdated = deviceData.timestamp,
                graph = icon
            )
        } else {
            return deviceDataComplication(
                status = DataServiceStatus.DISABLED.name,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    private fun deviceDataComplication(
        status: String,
        lastUpdated: Long,
        graph: Icon? = null
    ): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(lastUpdated.toString()).build(),
            contentDescription = PlainComplicationText.Builder(lastUpdated.toString()).build()
        )
            .setTitle(PlainComplicationText.Builder(status).build())
            .apply {
                if (graph != null) {
                    val image = MonochromaticImage.Builder(graph).build()
                    setMonochromaticImage(image)
                }
                if (status == DataServiceStatus.ENABLED.name) {
                    setTapAction(analysisTapIntent())
                } else {
                    setTapAction(configTapIntent())
                }
            }
            .build()
    }

    private fun analysisTapIntent(): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, "devicedata://analysis/screen".toUri())
        return PendingIntent.getActivity(
            this@StatusComplicationService,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun configTapIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this@StatusComplicationService,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val image = Bitmap.createBitmap(intArrayOf(Color.BLACK), 1, 1, Bitmap.Config.ARGB_8888)
        val icon = Icon.createWithBitmap(image)
        return deviceDataComplication(
            status = "ENABLED",
            graph = icon,
            lastUpdated = System.currentTimeMillis()
        )
    }
}