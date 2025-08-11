package com.example.football

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon // Added for Icon composable
import androidx.compose.ui.res.painterResource // Added for painterResource
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlin.math.roundToInt

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamConfigScreen() {
    // --- State Management ---
    val context = LocalContext.current
    val dataClient = remember { Wearable.getDataClient(context) }

    // --- UI Layout ---
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {

            Text(
                text = "Select Your Team",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // --- Blue Team Button ---
            Button(
                onClick = {
                    val blueTeam = Team.create(
                        context = context,
                        name = "Blue Team",
                        primaryColor = Color(0xFF2196F3).toArgb(), // Blue
                        secondaryColor = Color(0xFFFFEB3B).toArgb(), // Yellow
                        tertiaryColor = Color(0xFF2196F3).toArgb(), // Blue
                        logoResourceId = R.drawable.trophy_bitmap 
                    )
                    sendTeamData(blueTeam, dataClient)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Increased height
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trophy_bitmap),
                    contentDescription = "Blue Team Logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified // Use original bitmap colors
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Blue Team", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Red Team Button ---
            Button(
                onClick = {
                    val redTeam = Team.create(
                        context = context,
                        name = "Red Team",
                        primaryColor = Color(0xFFF44336).toArgb(), // Red
                        secondaryColor = Color.White.toArgb(),
                        tertiaryColor = Color(0xFFF44336).toArgb(), // Red
                        logoResourceId = R.drawable.football_bitmap
                    )
                    sendTeamData(redTeam, dataClient)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Increased height
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.football_bitmap),
                    contentDescription = "Red Team Logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified // Use original bitmap colors
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Red Team", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

/**
 * Creates a Team object and sends it to the watch via the Data Layer.
 */
private fun sendTeamData(team: Team, dataClient: com.google.android.gms.wearable.DataClient) {
    val dataMapRequest = PutDataMapRequest.create("/team_data")
    dataMapRequest.dataMap.apply {
        putInt("primaryColor", team.primaryColor)
        putInt("secondaryColor", team.secondaryColor)
        putInt("tertiaryColor", team.tertiaryColor)
        putString("name", team.name)
        team.logo?.let { putByteArray("logo", it) } // Add logo if not null
    }

    val request = dataMapRequest.asPutDataRequest().setUrgent()

    dataClient.putDataItem(request)
        .addOnSuccessListener {
            Log.d("TeamDataSender", "Successfully sent data item: $it")
        }
        .addOnFailureListener { e ->
            Log.e("TeamDataSender", "Failed to send data item", e)
        }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        TeamConfigScreen()
    }
}
