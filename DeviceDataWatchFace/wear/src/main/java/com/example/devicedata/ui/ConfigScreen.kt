package com.example.devicedata.ui

import android.Manifest
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConfigScreen(
    loading: Boolean,
    isActiveWatchFace: Boolean,
    loadActiveWatchFaceStatus: () -> Unit,
    serviceEnabled: Boolean,
    activeWatchFaceClick: () -> Unit,
    onServiceChecked: (Boolean) -> Unit = {}
) {
    val listState = rememberTransformingLazyColumnState()
    val buttonPlaceholderState = rememberPlaceholderState(loading)

    LaunchedEffect(Unit) {
        loadActiveWatchFaceStatus()
    }

    ScreenScaffold(
        scrollState = listState,
        contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.Button
            )
    ) { contentPadding ->
        val bluetoothPermission = rememberPermissionState(Manifest.permission.BLUETOOTH_CONNECT)
        val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        val activePermission =
            rememberPermissionState("com.google.wear.permission.SET_PUSHED_WATCH_FACE_AS_ACTIVE") { granted ->
                if (granted) {
                    activeWatchFaceClick()
                }
            }
        /*
         * TransformingLazyColumn takes care of the horizontal and vertical
         * padding for the list and handles scrolling.
         */
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState
        ) {
            item {
                ListHeader(modifier = Modifier.fillMaxWidth()) { Text(text = "Config") }
            }
            item {
                PermissionsChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholderShimmer(buttonPlaceholderState),
                    isGranted = bluetoothPermission.status.isGranted,
                    permissionLabel = "Bluetooth",
                    grantClick = { bluetoothPermission.launchPermissionRequest() },
                    placeholderState = buttonPlaceholderState
                )
            }
            item {
                PermissionsChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholderShimmer(buttonPlaceholderState),
                    isGranted = notificationPermission.status.isGranted,
                    permissionLabel = "Notifications",
                    grantClick = { notificationPermission.launchPermissionRequest() },
                    placeholderState = buttonPlaceholderState
                )
            }
            item {
                SwitchButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholderShimmer(buttonPlaceholderState),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Granted",
                        )
                    },
                    label = {
                        Text(
                            text = "Data Service",
                            modifier = Modifier.placeholder(buttonPlaceholderState)
                        )
                    },
                    enabled = bluetoothPermission.status.isGranted &&
                            notificationPermission.status.isGranted,
                    checked = serviceEnabled,
                    onCheckedChange = onServiceChecked
                )
            }
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