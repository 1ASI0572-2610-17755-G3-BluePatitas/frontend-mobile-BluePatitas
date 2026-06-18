package com.bluepatitas.mobile.data.remote.monitoring

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
    val zoneName: String?,
    val message: String?,
    val isBreachConfirmed: Boolean?,
    val trackingActive: Boolean?,
    val currentCoordinates: CoordinatesDto?,
    val createdAt: String?,
    val status: String?
)

data class CoordinatesDto(
    val latitude: Double?,
    val longitude: Double?
)
