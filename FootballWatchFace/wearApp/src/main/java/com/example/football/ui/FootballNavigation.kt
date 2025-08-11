package com.example.football.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.football.viewmodel.ConfigUiState
import com.example.football.viewmodel.FootballViewModel

@Composable
fun FootballNavigation() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel = viewModel<FootballViewModel>(factory = FootballViewModel.Factory)
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "watch_faces_list"
        ) {
            composable("education") {
                EducationScreen()
            }
            composable("watch_faces_list") {
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

                    FootballScreen(
                        loading = false,
                        activeWatchFaceClick = onWatchFaceClick,
                        isActiveWatchFace = loadedState.isActive,
                        loadActiveWatchFaceStatus = { viewModel.updateActiveStatus() }
                    )
                } else {
                    FootballScreen(
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

