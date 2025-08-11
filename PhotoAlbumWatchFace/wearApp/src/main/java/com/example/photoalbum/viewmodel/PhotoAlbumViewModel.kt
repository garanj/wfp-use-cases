package com.example.photoalbum.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.watchfacepush.WatchFacePushManager
import com.example.photoalbum.PhotoAlbumApplication
import com.example.photoalbum.data.StatusManager
import com.example.photoalbum.photos.queueImmediateWorker
import com.example.photoalbum.photos.removePeriodicWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class PhotoAlbumViewModel(
    val statusManager: StatusManager,
    val watchFacePushManager: WatchFacePushManager
) : ViewModel() {
    private val _photosEnabled = statusManager.photosEnabled
    private val _activeWatchFaceApiUsed = statusManager.activeWatchFaceApiUsed

    private val _isActiveWatchFace = MutableStateFlow<Boolean?>(null)
    val uiState =
        combine(_photosEnabled, _activeWatchFaceApiUsed, _isActiveWatchFace, ::updateStatus)
            .stateIn(viewModelScope, SharingStarted.Eagerly, ConfigUiState.Loading)

    fun enablePhotos(context: Context) {
        viewModelScope.launch {
            statusManager.setPhotosEnabled(true)
        }
        queueImmediateWorker(context)
    }

    fun disablePhotos(context: Context) {
        viewModelScope.launch {
            statusManager.setPhotosEnabled(false)
        }
        removePeriodicWorker(context)
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
                val app = (this[APPLICATION_KEY] as PhotoAlbumApplication)
                PhotoAlbumViewModel(
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