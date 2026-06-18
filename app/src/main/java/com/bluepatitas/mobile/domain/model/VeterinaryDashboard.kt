package com.bluepatitas.mobile.domain.model

data class VeterinaryDashboard(
    val veterinarianName: String,
    val shelterName: String,
    val animalsUnderCare: Int,
    val pendingObservations: Int,
    val activeAlerts: Int,
    val recentObservationsCount: Int,
    val recentObservations: List<RecentObservation>
)

data class RecentObservation(
    val id: String,
    val animalName: String,
    val description: String,
    val createdAt: String
)
