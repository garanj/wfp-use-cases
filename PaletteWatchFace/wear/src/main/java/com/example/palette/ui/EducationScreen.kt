package com.example.palette.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

@Composable
fun EducationScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(0.7f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Education moment on permissions ...",
                textAlign = TextAlign.Center
            )
        }
    }
}

@WearPreviewDevices
@Composable
private fun EducationScreenPreview() {
    EducationScreen()
}