package com.example.football.data

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.football.TAG
import com.example.football.complication.ColorsComplicationService
import com.example.football.complication.NextGameComplicationService
import com.example.football.complication.TeamLogoComplicationService
import com.example.football.complication.TeamNameComplicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "football")

/**
 * Storage for various app settings.
 */
class StatusManager(val context: Context) {
    private val appOpenedOnceKey = booleanPreferencesKey("opened_once")
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")

    suspend fun setTeam(team: Team) {
        context.teamDataStore.updateData { team }
        updateComplications(context)
    }

    val team: Flow<Team> = context.teamDataStore.data

    /**
     * The user ideally needs to open the app once - this ensures that package update broadcasts
     * will be received by the app.
     */
    val hasOpenedOnce: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[appOpenedOnceKey] == true
        }

    suspend fun setOpenedOnce(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[appOpenedOnceKey] = value
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
        ColorsComplicationService::class.java,
        NextGameComplicationService::class.java,
        TeamNameComplicationService::class.java,
        TeamLogoComplicationService::class.java
    )

    dataSources.forEach { source ->
        val component = ComponentName(context, source)
        val requester = ComplicationDataSourceUpdateRequester.create(context, component)
        requester.requestUpdateAll()
    }
}