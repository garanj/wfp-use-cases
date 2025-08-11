package com.example.photoalbum.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.photoalbum.viewmodel.ConfigUiState
import com.example.photoalbum.viewmodel.PhotoAlbumViewModel

@Composable
fun PhotoAlbumNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = viewModel<PhotoAlbumViewModel>(factory = PhotoAlbumViewModel.Factory)
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "watch_faces_list"
        ) {
            composable("education") {
                EducationScreen()
            }
            composable("watch_faces_list") {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsState()

                if (uiState is ConfigUiState.Loaded) {
                    val loadedState = uiState as ConfigUiState.Loaded
                    val onWatchFaceClick = if (loadedState.isApiUsed) {
                        {
                            navController.navigate("education")
                        }
                    } else {
                        {
                            viewModel.setWatchFaceAsActive()
                        }
                    }

                    PhotoAlbumScreen(
                        loading = false,
                        serviceEnabled = loadedState.isEnabled,
                        onServiceChecked = { enabled ->
                            if (enabled) {
                                viewModel.enablePhotos(context)
                            } else {
                                viewModel.disablePhotos(context)
                            }
                        },
                        activeWatchFaceClick = onWatchFaceClick,
                        isActiveWatchFace = loadedState.isActive,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                } else {
                    PhotoAlbumScreen(
                        loading = true,
                        serviceEnabled = false,
                        onServiceChecked = { enabled ->
                            if (enabled) {
                                viewModel.enablePhotos(context)
                            } else {
                                viewModel.disablePhotos(context)
                            }
                        },
                        activeWatchFaceClick = { },
                        isActiveWatchFace = false,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                }
            }
        }
    }
}

