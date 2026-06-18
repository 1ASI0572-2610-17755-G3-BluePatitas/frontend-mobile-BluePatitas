package com.bluepatitas.mobile.domain.model

data class AnimalSummary(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val species: String,
    val breed: String?,
    val estimatedAgeMonths: Int?,
    val healthCondition: String?,
    val weightKg: Double?,
    val zoneName: String?
)
