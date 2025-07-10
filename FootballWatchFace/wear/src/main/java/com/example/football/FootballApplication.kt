package com.example.football

import android.app.Application
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.football.data.StatusManager

const val TAG = "FootballWatchFace"

class FootballApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManager(this) }
}
