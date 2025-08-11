package com.example.devicedata.data

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

/**
 * A trivial class to represent readings from a connected bluetooth device. This is used simply as
 * an example in this sample.
 */
@Serializable
data class DeviceData(
    // The timestamp of the when the last reading was received from the device.
    val timestamp: Long = 0,
    // Readings taken from the connected bluetooth device.
    val readings: List<Double> = listOf()
)

@OptIn(ExperimentalSerializationApi::class)
object DeviceDataSerializer : Serializer<DeviceData> {
    override val defaultValue = DeviceData()

    override suspend fun readFrom(input: InputStream): DeviceData {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read DeviceData", serialization)
        }
    }

    override suspend fun writeTo(t: DeviceData, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t)
        )
    }
}

val Context.deviceDataStore: DataStore<DeviceData> by dataStore(
    fileName = "device_data_store",
    serializer = DeviceDataSerializer
)