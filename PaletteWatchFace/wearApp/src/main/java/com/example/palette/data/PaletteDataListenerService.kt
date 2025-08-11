package com.example.palette.data

import android.content.ComponentName
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.palette.complication.PaletteComplicationService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

class PaletteDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/palette_data") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val palette = Palette(
                        primaryColor = dataMap.getInt("primaryColor"),
                        secondaryColor = dataMap.getInt("secondaryColor"),
                        tertiaryColor = dataMap.getInt("tertiaryColor")
                    )

                    runBlocking {
                        val statusManager = StatusManager(applicationContext)
                        statusManager.setPalette(palette)
                    }
                    updateComplications()
                }
            }
        }
    }

    private fun updateComplications() {
        val requester = ComplicationDataSourceUpdateRequester.create(
            context = applicationContext,
            complicationDataSourceComponent = ComponentName(
                applicationContext,
                PaletteComplicationService::class.java
            )
        )
        requester.requestUpdateAll()
    }
}