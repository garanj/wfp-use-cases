package com.example.palette

import android.app.Application
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import com.example.palette.data.StatusManager

const val TAG = "PaletteWatchFace"

class PaletteApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManagerFactory.createWatchFacePushManager(this) }
}
