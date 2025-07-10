package com.example.football

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

/**
 * Represents the team data that will be synchronized with the Wear OS device.
 */
data class Team(
    val primaryColor: Int,
    val secondaryColor: Int,
    val tertiaryColor: Int,
    val name: String,
    val logo: ByteArray? = null // Or String, if a logo is always expected
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        if (primaryColor != other.primaryColor) return false
        if (secondaryColor != other.secondaryColor) return false
        if (tertiaryColor != other.tertiaryColor) return false
        if (name != other.name) return false
        if (logo != null) {
            if (other.logo == null) return false
            if (!logo.contentEquals(other.logo)) return false
        } else if (other.logo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = primaryColor
        result = 31 * result + secondaryColor
        result = 31 * result + tertiaryColor
        result = 31 * result + name.hashCode()
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        fun create(
            context: Context,
            primaryColor: Int,
            secondaryColor: Int,
            tertiaryColor: Int,
            name: String,
            logoResourceId: Int // e.g., R.drawable.trophy
        ): Team {
            val drawable = ContextCompat.getDrawable(context, logoResourceId)
            val logoByteArray: ByteArray? = drawable?.let {
                val bitmap: Bitmap = if (it is BitmapDrawable) {
                    it.bitmap
                } else {
                    // Create a Bitmap from other Drawable types
                    val bmp = Bitmap.createBitmap(
                        it.intrinsicWidth.coerceAtLeast(1),
                        it.intrinsicHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)
                    bmp
                }
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
            return Team(primaryColor, secondaryColor, tertiaryColor, name, logoByteArray)
        }
    }
}
