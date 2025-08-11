package com.example.devicedata.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState

@Composable
fun PermissionsChip(
    modifier: Modifier = Modifier,
    isGranted: Boolean,
    permissionLabel: String,
    grantClick: () -> Unit = {},
    placeholderState: PlaceholderState
) {
    if (isGranted) {
        Button(
            modifier = modifier.placeholderShimmer(placeholderState),
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Granted",
                    modifier = Modifier.placeholder(placeholderState)
                )
            },
            label = {
                Text(
                    text = permissionLabel,
                    modifier = modifier.placeholder(placeholderState)
                )
            },
            secondaryLabel = {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = modifier.placeholder(placeholderState)
                )
            }
        )
    } else {
        Button(
            modifier = modifier,
            onClick = grantClick,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Alert",
                )
            },
            colors = ButtonDefaults.outlinedButtonColors(),
            label = {
                Text(
                    text = permissionLabel,
                    modifier = modifier.placeholder(placeholderState)
                )
            },
            secondaryLabel = {
                Text(
                    text = "Tap to grant",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = modifier.placeholder(placeholderState)
                )
            }
        )
    }
}

@Preview
@Composable
fun PermissionsChipGrantedPreview() {
    PermissionsChip(
        isGranted = true,
        permissionLabel = "Notifications",
        placeholderState = rememberPlaceholderState(false)
    )
}

@Preview
@Composable
fun PermissionsChipNotGrantedPreview() {
    PermissionsChip(
        isGranted = false,
        permissionLabel = "Notifications",
        placeholderState = rememberPlaceholderState(false)
    )
}