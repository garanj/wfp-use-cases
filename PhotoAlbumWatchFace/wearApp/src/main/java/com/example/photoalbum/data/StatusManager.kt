package com.example.photoalbum.data

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.photoalbum.TAG
import com.example.photoalbum.complication.PhotosComplicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "photos")

/**
 * Storage for various app settings.
 */
class StatusManager(val context: Context) {
    private val photosEnabledKey = booleanPreferencesKey("enabled")
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")


    val photosEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[photosEnabledKey] == true
        }

    /**
     * Sets whether the user has enabled the data service.
     */
    suspend fun setPhotosEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[photosEnabledKey] = value
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
    val component = ComponentName(context, PhotosComplicationService::class.java)
    val requester = ComplicationDataSourceUpdateRequester.create(context, component)
    requester.requestUpdateAll()
}