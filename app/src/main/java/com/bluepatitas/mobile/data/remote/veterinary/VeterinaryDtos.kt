package com.bluepatitas.mobile.data.remote.veterinary

import com.google.gson.annotations.SerializedName

data class VeterinaryDashboardDto(
    val veterinarianName: String?,
    val shelterName: String?,
    @SerializedName(value = "animalsUnderCare", alternate = ["assignedAnimalsCount"])
    val animalsUnderCare: Int?,
    @SerializedName(value = "pendingObservations", alternate = ["pendingObservationsCount"])
    val pendingObservations: Int?,
    @SerializedName(value = "activeAlerts", alternate = ["activeAlertsCount"])
    val activeAlerts: Int?,
    val recentObservationsCount: Int?,
    val recentObservations: List<RecentObservationDto>?
)

data class RecentObservationDto(
    val id: String?,
    val animalName: String?,
    val observation: String?,
    val note: String?,
    val createdAt: String?
)

data class VeterinaryAnimalDto(
    val id: String?,
    val photoUrl: String?,
    val name: String?,
    val species: String?,
    val breed: String?,
    val healthCondition: String?,
    val weightKg: Double?
)
