package com.example.photoalbum

import android.app.Application
import androidx.wear.watchface.push.WatchFacePushManager
import com.example.photoalbum.data.StatusManager

const val TAG = "PhotoAlbumWatchFace"

class PhotoAlbumApplication : Application() {
    val statusManager by lazy { StatusManager(this) }
    val watchFacePushManager by lazy { WatchFacePushManager(this) }
}
