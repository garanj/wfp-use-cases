package com.example.photoalbum.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import com.example.photoalbum.R

@Composable
fun SetActiveChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    placeholderState: PlaceholderState
) {
    Button(
        modifier = modifier.placeholderShimmer(placeholderState),
        onClick = onClick,
        enabled = enabled,
        icon = {
            Icon(
                imageVector = Icons.Rounded.WatchLater,
                contentDescription = stringResource(R.string.set_as_active),
            )
        },
        label = {
            Text(
                text = stringResource(R.string.set_as_active),
                modifier = modifier.placeholder(placeholderState)
            )
        }
    )
}

@Preview
@Composable
fun SetActiveChipPreview() {
    SetActiveChip(
        placeholderState = rememberPlaceholderState(false)
    )
}
