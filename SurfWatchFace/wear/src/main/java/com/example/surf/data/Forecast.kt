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
data class Forecast(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val windSpeed: Double = 0.0,
    val windDirection: Double = 0.0,
    val swellDirection: Double = 0.0,
    val swellHeight: Double = 0.0,
    val swellPeriod: Double = 0.0,
    val tideHeightLow: Double = 0.0,
    val tideHeightHigh: Double = 0.0,
    val tideTimeLow: Long = 0,
    val tideTimeHigh: Long = 0,
    val waterTemp: Double = 0.0,
    val firstLight: Int = 0,
    val lastLight: Int = 0,
    val forecastTime: Long = 0
)

@OptIn(ExperimentalSerializationApi::class)
object ForecastSerializer : Serializer<Forecast> {
    override val defaultValue = Forecast()

    override suspend fun readFrom(input: InputStream): Forecast {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read DeviceData", serialization)
        }
    }

    override suspend fun writeTo(t: Forecast, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t)
        )
    }
}

val Context.forecastDataStore: DataStore<Forecast> by dataStore(
    fileName = "forecast_data_store",
    serializer = ForecastSerializer
)