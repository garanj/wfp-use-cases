package com.example.devicedata.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.devicedata.DeviceDataApplication
import com.example.devicedata.data.StatusManager
import com.example.devicedata.service.DataService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ConfigViewModel(
    val statusManager: StatusManager,
    val watchFacePushManager: WatchFacePushManager
) : ViewModel() {
    private val _serviceEnabled = statusManager.serviceEnabled
    private val _activeWatchFaceApiUsed = statusManager.activeWatchFaceApiUsed

    private val _isActiveWatchFace = MutableStateFlow<Boolean?>(null)
    val uiState = combine(_serviceEnabled, _activeWatchFaceApiUsed, _isActiveWatchFace, ::updateStatus)
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConfigUiState.Loading)

    fun enableService(context: Context) {
        val fgsIntent = Intent(context, DataService::class.java)
        context.startForegroundService(fgsIntent)
        viewModelScope.launch {
            statusManager.setServiceEnabled(true)
        }
    }

    fun disableService(context: Context) {
        val fgsIntent = Intent(context, DataService::class.java)
        context.stopService(fgsIntent)
        viewModelScope.launch {
            statusManager.setServiceEnabled(false)
        }
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

    private fun updateStatus(isEnabled: Boolean?, isApiUsed: Boolean?, isActive: Boolean?) = if (isEnabled == null || isApiUsed == null || isActive == null) {
            ConfigUiState.Loading
        } else {
            ConfigUiState.Loaded(isEnabled, isApiUsed, isActive)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as DeviceDataApplication)
                ConfigViewModel(
                    statusManager = app.statusManager,
                    watchFacePushManager = app.watchFacePushManager
                )
            }
        }
    }
}

sealed class ConfigUiState {
    data class Loaded(val isEnabled: Boolean, val isApiUsed: Boolean, val isActive: Boolean): ConfigUiState()
    object Loading : ConfigUiState()
}