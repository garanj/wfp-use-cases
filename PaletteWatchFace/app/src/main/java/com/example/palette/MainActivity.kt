package com.example.palette

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                TeamConfigScreen()
            }
        }
    }
}

@Composable
fun TeamConfigScreen() {
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var primaryColor by remember { mutableStateOf(Color.Transparent) }
    var secondaryColor by remember { mutableStateOf(Color.Transparent) }
    var tertiaryColor by remember { mutableStateOf(Color.Transparent) }

    val context = LocalContext.current
    val dataClient = remember { Wearable.getDataClient(context) }

    val generatePalette: (Bitmap) -> Unit = { bitmap ->
        Palette.from(bitmap).generate { palette ->
            palette?.let {
                val defaultColor = 0x000000
                primaryColor =
                    Color(it.vibrantSwatch?.rgb ?: it.dominantSwatch?.rgb ?: defaultColor)
                secondaryColor =
                    Color(it.lightVibrantSwatch?.rgb ?: it.lightMutedSwatch?.rgb ?: defaultColor)
                tertiaryColor =
                    Color(it.darkVibrantSwatch?.rgb ?: it.darkMutedSwatch?.rgb ?: defaultColor)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                imageBitmap = it
                generatePalette(it)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageContainer(
                bitmap = imageBitmap,
                onTakePhotoClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (imageBitmap != null) {
                ExtractedColorsRow(primaryColor, secondaryColor, tertiaryColor)
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val palette = Palette(
                        primaryColor = primaryColor.toArgb(),
                        secondaryColor = secondaryColor.toArgb(),
                        tertiaryColor = tertiaryColor.toArgb()
                    )
                    sendTeamData(palette, dataClient)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = imageBitmap != null, // Enable button only after photo is taken
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Send to Watch", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ImageContainer(bitmap: Bitmap?, onTakePhotoClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.LightGray, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Button(onClick = onTakePhotoClick) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Camera Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }
        }
    }
}

@Composable
fun ExtractedColorsRow(primary: Color, secondary: Color, tertiary: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Extracted Colors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorSwatch("Primary", primary)
            ColorSwatch("Secondary", secondary)
            ColorSwatch("Tertiary", tertiary)
        }
    }
}

@Composable
fun ColorSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.DarkGray, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

private fun sendTeamData(palette: com.example.palette.Palette, dataClient: com.google.android.gms.wearable.DataClient) {
    val dataMapRequest = PutDataMapRequest.create("/palette_data")
    dataMapRequest.dataMap.apply {
        putInt("primaryColor", palette.primaryColor)
        putInt("secondaryColor", palette.secondaryColor)
        putInt("tertiaryColor", palette.tertiaryColor)
    }
    val request = dataMapRequest.asPutDataRequest().setUrgent()
    dataClient.putDataItem(request)
        .addOnSuccessListener { Log.d("TeamDataSender", "Successfully sent data item: $it") }
        .addOnFailureListener { e -> Log.e("TeamDataSender", "Failed to send data item", e) }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        TeamConfigScreen()
    }
}