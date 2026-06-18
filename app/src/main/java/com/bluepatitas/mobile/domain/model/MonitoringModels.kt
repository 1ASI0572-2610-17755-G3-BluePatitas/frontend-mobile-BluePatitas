package com.bluepatitas.mobile.domain.model

data class MonitoringZone(
    val id: String,
    val shelterId: String?,
    val targetId: String,
    val name: String,
    val temperatureC: Double?,
    val humidity: Double?,
    val minTemperatureC: Double?,
    val maxTemperatureC: Double?,
    val status: String,
    val animalCount: Int,
    val cameraEnabled: Boolean,
    val imageUrl: String?
)

data class MonitoringAlert(
    val id: String,
    val targetId: String,
    val zoneName: String,
    val message: String,
    val isBreachConfirmed: Boolean,
    val trackingActive: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val isLocal: Boolean
)
