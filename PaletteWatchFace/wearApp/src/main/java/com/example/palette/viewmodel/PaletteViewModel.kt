package com.example.palette.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.watchfacepush.WatchFacePushManager
import com.example.palette.PaletteApplication
import com.example.palette.data.StatusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class PaletteViewModel(
    val statusManager: StatusManager,
    val watchFacePushManager: WatchFacePushManager
) : ViewModel() {
    private val _activeWatchFaceApiUsed = statusManager.activeWatchFaceApiUsed

    private val _isActiveWatchFace = MutableStateFlow<Boolean?>(null)
    val uiState =
        combine(_activeWatchFaceApiUsed, _isActiveWatchFace, ::updateStatus)
            .stateIn(viewModelScope, SharingStarted.Eagerly, ConfigUiState.Loading)

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

    private fun updateStatus(isApiUsed: Boolean?, isActive: Boolean?) =
        if (isApiUsed == null || isActive == null) {
            ConfigUiState.Loading
        } else {
            ConfigUiState.Loaded(isApiUsed, isActive)
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as PaletteApplication)
                PaletteViewModel(
                    statusManager = app.statusManager,
                    watchFacePushManager = app.watchFacePushManager
                )
            }
        }
    }
}

sealed class ConfigUiState {
    data class Loaded(val isApiUsed: Boolean, val isActive: Boolean) :
        ConfigUiState()

    object Loading : ConfigUiState()
}