package com.example.palette.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.palette.viewmodel.ConfigUiState
import com.example.palette.viewmodel.PaletteViewModel

@Composable
fun PaletteNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = viewModel<PaletteViewModel>(factory = PaletteViewModel.Factory)
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "main_screen"
        ) {
            composable("education") {
                EducationScreen()
            }
            composable("main_screen") {
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

                    PaletteScreen(
                        loading = false,
                        activeWatchFaceClick = onWatchFaceClick,
                        isActiveWatchFace = loadedState.isActive,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                } else {
                    PaletteScreen(
                        loading = true,
                        activeWatchFaceClick = { },
                        isActiveWatchFace = false,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                }
            }
        }
    }
}

