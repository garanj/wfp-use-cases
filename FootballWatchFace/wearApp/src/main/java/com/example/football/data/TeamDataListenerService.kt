package com.example.football.data

import android.content.ComponentName
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.football.complication.ColorsComplicationService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

class TeamDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/team_data") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val team = Team(
                        primaryColor = dataMap.getInt("primaryColor"),
                        secondaryColor = dataMap.getInt("secondaryColor"),
                        tertiaryColor = dataMap.getInt("tertiaryColor"),
                        name = dataMap.getString("name", ""),
                        logo = dataMap.getByteArray("logo")
                    )

                    // Now you have the 'team' object and can use it as needed
                    // For example, you could broadcast it to an Activity or update a complication.
                    Log.d("TeamDataListener", "Received team: ${team.name}")
                    runBlocking {
                        val statusManager = StatusManager(applicationContext)
                        statusManager.setTeam(team)
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
                ColorsComplicationService::class.java
            )
        )
        requester.requestUpdateAll()
    }
}