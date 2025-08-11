package com.example.football.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.rememberPlaceholderState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FootballScreen(
    loading: Boolean,
    isActiveWatchFace: Boolean,
    loadActiveWatchFaceStatus: () -> Unit,
    activeWatchFaceClick: () -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    val buttonPlaceholderState = rememberPlaceholderState(loading)
    val activePermission =
        rememberPermissionState("com.google.wear.permission.SET_PUSHED_WATCH_FACE_AS_ACTIVE") { granted ->
            if (granted) {
                activeWatchFaceClick()
            }
        }

    LaunchedEffect(Unit) {
        loadActiveWatchFaceStatus()
    }

    ScreenScaffold(
        scrollState = listState,
        contentPadding = rememberResponsiveColumnPadding(
            first = ColumnItemType.Button,
            last = ColumnItemType.Button
        )
    ) { contentPadding ->

        /*
         * TransformingLazyColumn takes care of the horizontal and vertical
         * padding for the list and handles scrolling.
         */
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState
        ) {
            item {
                if (activePermission.status.isGranted) {
                    SetActiveChip(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActiveWatchFace,
                        onClick = activeWatchFaceClick,
                        placeholderState = buttonPlaceholderState
                    )
                } else {
                    SetActiveChip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            activePermission.launchPermissionRequest()
                        },
                        enabled = !isActiveWatchFace,
                        placeholderState = buttonPlaceholderState
                    )
                }
            }
        }
    }
}