package com.example.surf.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.surf.SurfApplication
import com.example.surf.data.Location
import com.example.surf.data.StatusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SurfViewModel(
    val statusManager: StatusManager,
    val watchFacePushManager: WatchFacePushManager
) : ViewModel() {
    private val _surfEnabled = statusManager.surfEnabled
    private val _activeWatchFaceApiUsed = statusManager.activeWatchFaceApiUsed

    private val _isActiveWatchFace = MutableStateFlow<Boolean?>(null)
    val uiState = combine(_surfEnabled, _activeWatchFaceApiUsed, _isActiveWatchFace, ::updateStatus)
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConfigUiState.Loading)

    fun enableSurf(context: Context) {
        viewModelScope.launch {
            statusManager.setSurfEnabled(true)
            // Just use an example location for now as opposed to choosing one.
            statusManager.setLocation(
                Location(49.211246, -2.230202)
            )

            // TODO: Add an immediate job to download the surf
            // Add a periodic job to download the surf using a service such as stormglass.io
            // The code to do this has not been included
        }
    }

    fun disableSurf(context: Context) {
        viewModelScope.launch {
            statusManager.setSurfEnabled(false)
        }
        // TODO: Remove the periodic downloader
    }

    fun setWatchFaceAsActive() {
        viewModelScope.launch {
            val slotId = watchFacePushManager.listWatchFaces()
                .installedWatchFaceDetails.firstOrNull()?.slotId
            slotId?.let {
                watchFacePushManager.setWatchFaceAsActive(it)
                statusManager.setActiveWatchFaceApiUsedKey(true)
            }
        }
    }

    fun updateActiveStatus() {
        viewModelScope.launch {
            val watchFace =
                watchFacePushManager.listWatchFaces().installedWatchFaceDetails.firstOrNull()
            watchFace?.let {
                _isActiveWatchFace.value = watchFacePushManager.isWatchFaceActive(it.packageName)
            }
        }
    }

    private fun updateStatus(isEnabled: Boolean?, isApiUsed: Boolean?, isActive: Boolean?) =
        if (isEnabled == null || isApiUsed == null || isActive == null) {
            ConfigUiState.Loading
        } else {
            ConfigUiState.Loaded(isEnabled, isApiUsed, isActive)
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as SurfApplication)
                SurfViewModel(
                    statusManager = app.statusManager,
                    watchFacePushManager = app.watchFacePushManager
                )
            }
        }
    }
}

sealed class ConfigUiState {
    data class Loaded(val isEnabled: Boolean, val isApiUsed: Boolean, val isActive: Boolean) :
        ConfigUiState()

    object Loading : ConfigUiState()
}