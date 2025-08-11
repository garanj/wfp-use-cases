package com.example.devicedata.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navDeepLink
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.devicedata.viewmodel.ConfigUiState
import com.example.devicedata.viewmodel.ConfigViewModel

@Composable
fun DeviceDataNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = viewModel<ConfigViewModel>(factory = ConfigViewModel.Factory)
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

                    ConfigScreen(
                        loading = false,
                        serviceEnabled = loadedState.isEnabled,
                        onServiceChecked = { enabled ->
                            if (enabled) {
                                viewModel.enableService(context)
                            } else {
                                viewModel.disableService(context)
                            }
                        },
                        activeWatchFaceClick = onWatchFaceClick,
                        isActiveWatchFace = loadedState.isActive,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                } else {
                    ConfigScreen(
                        loading = true,
                        serviceEnabled = false,
                        onServiceChecked = { enabled ->
                            if (enabled) {
                                viewModel.enableService(context)
                            } else {
                                viewModel.disableService(context)
                            }
                        },
                        activeWatchFaceClick = { },
                        isActiveWatchFace = false,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                }
            }
            composable("education") {
                EducationScreen()
            }
            composable(
                route = "analysis",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "devicedata://analysis/screen"
                })
            ) {
                AnalysisScreen()
            }
        }
    }
}

