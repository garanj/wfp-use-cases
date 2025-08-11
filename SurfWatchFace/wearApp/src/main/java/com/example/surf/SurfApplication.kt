package com.example.surf

import android.app.Application
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import com.example.surf.data.StatusManager

const val TAG = "SurfWatchFace"

class SurfApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManagerFactory.createWatchFacePushManager(this) }
}
