package com.example.surf.data

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.surf.TAG
import com.example.surf.complication.SwellComplicationService
import com.example.surf.complication.TideComplicationService
import com.example.surf.complication.WaterTempComplicationService
import com.example.surf.complication.WindComplicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "surf")

/**
 * Storage for various app settings.
 */
class StatusManager(val context: Context) {
    private val surfEnabledKey = booleanPreferencesKey("enabled")
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")

    suspend fun setForecast(forecast: Forecast) {
        context.forecastDataStore.updateData { forecast }
        updateComplications(context)
    }

    val forecast: Flow<Forecast> = context.forecastDataStore.data

    suspend fun setLocation(location: Location) {
        context.locationDataStore.updateData { location }
        updateComplications(context)
    }

    val location: Flow<Location> = context.locationDataStore.data

    val surfEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[surfEnabledKey] == true
        }

    /**
     * Sets whether the user has enabled the data service.
     */
    suspend fun setSurfEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[surfEnabledKey] = value
        }
        updateComplications(context)
    }

    /**
     * The SET_PUSHED_WATCH_FACE_AS_ACTIVE API is a single-shot API - after one once it will not
     * work again. This indicates whether the API has already been used.
     */
    val activeWatchFaceApiUsed: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[activeWatchFaceApiUsedKey] == true
        }

    /**
     * Marks that the SET_PUSHED_WATCH_FACE_AS_ACTIVE API call has already been used.
     */
    suspend fun setActiveWatchFaceApiUsedKey(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[activeWatchFaceApiUsedKey] = value
        }
    }
}

fun updateComplications(context: Context) {
    Log.i(TAG, "updateComplications")

    val dataSources = listOf(
        ComponentName(context, TideComplicationService::class.java),
        ComponentName(context, WindComplicationService::class.java),
        ComponentName(context, SwellComplicationService::class.java),
        ComponentName(context, WaterTempComplicationService::class.java)
    )

    dataSources.forEach {
        val requester = ComplicationDataSourceUpdateRequester.create(context, it)
        requester.requestUpdateAll()
    }
}