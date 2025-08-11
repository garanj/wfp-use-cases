package com.example.palette.data

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.palette.complication.colorToHexString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class Palette(
    val primaryColor: Int = "#FF0000".toColorInt(),
    val secondaryColor: Int = "#00FF00".toColorInt(),
    val tertiaryColor: Int = "#0000FF".toColorInt()
) {
    fun toColorList() = listOf<Int>(
        primaryColor,
        secondaryColor,
        tertiaryColor
    ).joinToString(" ") { colorToHexString(it) }
}

@OptIn(ExperimentalSerializationApi::class)
object PaletteSerializer : Serializer<Palette> {
    override val defaultValue = Palette()

    override suspend fun readFrom(input: InputStream): Palette {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Palette", serialization)
        }
    }

    override suspend fun writeTo(t: Palette, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t)
        )
    }
}

val Context.paletteDataStore: DataStore<Palette> by dataStore(
    fileName = "palette_data_store",
    serializer = PaletteSerializer
)