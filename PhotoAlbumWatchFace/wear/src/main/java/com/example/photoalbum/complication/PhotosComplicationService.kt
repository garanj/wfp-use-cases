package com.example.photoalbum.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.photoalbum.R
import com.example.photoalbum.data.StatusManager
import com.example.photoalbum.photos.PhotosWorker.Companion.TARGET_SUBDIRECTORY
import kotlinx.coroutines.flow.first
import java.io.File

class PhotosComplicationService() : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val statusManager = StatusManager(applicationContext)
        val status = statusManager.photosEnabled.first()
        val bitmap = getNextPhotoFile()
        return createPhotoComplication(status, bitmap)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.photos_placeholder)!!
        return createPhotoComplication(true, bitmap)
    }

    private fun createPhotoComplication(
        enabled: Boolean,
        bitmap: Bitmap
    ): SmallImageComplicationData {
        // Use this as a flag to indicate whether the photo service is enabled or not
        val type = if (enabled) {
            SmallImageType.PHOTO
        } else {
            SmallImageType.ICON
        }
        val icon = Icon.createWithBitmap(bitmap)
        val smallImage = SmallImage.Builder(icon, type).build()

        return SmallImageComplicationData.Builder(
            smallImage = smallImage,
            contentDescription = PlainComplicationText.Builder("photos status").build()
        )
            .apply {
                if (enabled) {
                    val intent = Intent(this@PhotosComplicationService, TapReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@PhotosComplicationService,
                        1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    setTapAction(pendingIntent)
                }
            }
            .build()
    }

    private fun getNextPhotoFile(): Bitmap {
        val photosDir = File(applicationContext.filesDir, TARGET_SUBDIRECTORY)

        val selected = if (photosDir.exists()) {
            val photoFiles =
                photosDir.listFiles { file -> file.isFile && file.name.endsWith("jpg") }
            val photoFile = photoFiles?.random()
            photoFile?.let {
                BitmapFactory.decodeFile(it.absolutePath)
            }
        } else {
            null
        }

        if (selected == null) {
            return Bitmap.createBitmap(intArrayOf(Color.BLACK), 1, 1, Bitmap.Config.ARGB_8888)
        }
        return selected
    }


}