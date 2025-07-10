package com.example.devicedata.data

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.devicedata.service.StatusComplicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_service")

/**
 * Storage for various app settings.
 */
class StatusManager(val context: Context) {
    private val serviceEnabledKey = booleanPreferencesKey("enabled")
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")

    /**
     * Sets the [DeviceData] - the trivial data stored to represent data from a connected bluetooth
     * device
     */
    suspend fun setDeviceData(deviceData: DeviceData) {
        context.deviceDataStore.updateData { deviceData }
        updateComplications()
    }

    /**
     * Retrieves the [DeviceData] - the trivial data stored to represent data from a connected
     * device.
     */
    val deviceData: Flow<DeviceData> = context.deviceDataStore.data

    /**
     * Sets whether the user has enabled the data service.
     */
    suspend fun setServiceEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[serviceEnabledKey] = value
        }
        updateComplications()
    }

    /**
     * Indicates whether the user has enabled the data service, or null if not set
     */
    val serviceEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[serviceEnabledKey] == true
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

    private fun updateComplications() {
        val component = ComponentName(context, StatusComplicationService::class.java)
        val requester = ComplicationDataSourceUpdateRequester.create(context, component)
        requester.requestUpdateAll()
    }
}