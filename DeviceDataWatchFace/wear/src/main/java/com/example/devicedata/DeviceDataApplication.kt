package com.example.devicedata

import android.app.Application
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.devicedata.data.StatusManager

const val TAG = "DeviceDataWatchFace"

class DeviceDataApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManager(this) }
}
