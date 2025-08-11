package com.example.football.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.football.complication.colorToHexString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class Team(
    val primaryColor: Int = 0,
    val secondaryColor: Int = 0,
    val tertiaryColor: Int = 0,
    val name: String = "",
    val logo: ByteArray? = null
) {
    fun toColorList() = listOf<Int>(
        primaryColor,
        secondaryColor,
        tertiaryColor
    ).joinToString(" ") { colorToHexString(it) }

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
}

@OptIn(ExperimentalSerializationApi::class)
object TeamSerializer : Serializer<Team> {
    override val defaultValue = Team()

    override suspend fun readFrom(input: InputStream): Team {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Team", serialization)
        }
    }

    override suspend fun writeTo(t: Team, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t)
        )
    }
}

val Context.teamDataStore: DataStore<Team> by dataStore(
    fileName = "team_data_store",
    serializer = TeamSerializer
)
