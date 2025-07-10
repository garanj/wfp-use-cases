package com.example.surf

import android.app.Application
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.surf.data.StatusManager

const val TAG = "SurfWatchFace"

class SurfApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManager(this) }
}
