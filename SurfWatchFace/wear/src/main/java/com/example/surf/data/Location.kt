package com.example.surf.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@OptIn(ExperimentalSerializationApi::class)
object LocationSerializer : Serializer<Location> {
    override val defaultValue = Location()

    override suspend fun readFrom(input: InputStream): Location {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read DeviceData", serialization)
        }
    }

    override suspend fun writeTo(t: Location, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t)
        )
    }
}

val Context.locationDataStore: DataStore<Location> by dataStore(
    fileName = "location_data_store",
    serializer = LocationSerializer
)