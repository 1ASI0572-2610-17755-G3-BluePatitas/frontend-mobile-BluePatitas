package com.bluepatitas.mobile.data.remote.monitoring

data class ZoneRequestDto(
    val targetId: String?,
    val name: String,
    val temperatureC: Double,
    val humidity: Double,
    val status: String,
    val animalCount: Int,
    val cameraEnabled: Boolean,
    val imageUrl: String?,
    val minTemperatureC: Double,
    val maxTemperatureC: Double
)

data class MonitoringZoneDto(
    val id: String?,
    val shelterId: String?,
    val targetId: String?,
    val name: String?,
    val temperatureC: Double?,
    val humidity: Double?,
    val minTemperatureC: Double?,
    val maxTemperatureC: Double?,
    val status: String?,
    val animalCount: Int?,
    val cameraEnabled: Boolean?,
    val imageUrl: String?
)

data class PerimeterAlertDto(
    val id: String?,
    val targetId: String?,
    val isBreachConfirmed: Boolean?,
    val currentCoordinates: LocationContextDto?,
    val trackingActive: Boolean?,
    val zoneName: String? = null,
    val message: String? = null,
    val createdAt: String? = null,
    val status: String? = null
)

data class LocationContextDto(
    val latitude: Double?,
    val longitude: Double?
)

data class EnableTrackingRequestDto(
    val alertId: String
)

data class TelemetryRecordDto(
    val id: String?,
    val targetId: String?,
    val ambientTemperature: Double?,
    val ambientHumidity: Double?,
    val visualData: String?,
    val recordedAt: String?
)
