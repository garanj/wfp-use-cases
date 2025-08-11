package com.example.surf.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.surf.viewmodel.ConfigUiState
import com.example.surf.viewmodel.SurfViewModel

@Composable
fun SurfNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = viewModel<SurfViewModel>(factory = SurfViewModel.Factory)
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "watch_faces_list"
        ) {
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

                    SurfScreen(
                        loading = false,
                        forecastEnabled = loadedState.isEnabled,
                        onForecastChecked = { enabled ->
                            if (enabled) {
                                viewModel.enableSurf(context)
                            } else {
                                viewModel.disableSurf(context)
                            }
                        },
                        activeWatchFaceClick = onWatchFaceClick,
                        isActiveWatchFace = loadedState.isActive,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                } else {
                    SurfScreen(
                        loading = true,
                        forecastEnabled = false,
                        onForecastChecked = { enabled ->
                            if (enabled) {
                                viewModel.enableSurf(context)
                            } else {
                                viewModel.disableSurf(context)
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

