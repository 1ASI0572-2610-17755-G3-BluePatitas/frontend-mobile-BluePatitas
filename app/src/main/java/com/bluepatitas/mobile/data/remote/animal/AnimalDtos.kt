package com.bluepatitas.mobile.data.remote.animal

data class AnimalDto(
    val id: String?,
    val name: String?,
    val photoUrl: String?,
    val shelterId: String?,
    val species: String?,
    val breed: String?,
    val speciesDetails: SpeciesDetailsDto?,
    val healthCondition: String?,
    val weightKg: Double?,
    val assignedPerimeterId: String?
)

data class SpeciesDetailsDto(
    val species: String?,
    val breed: String?,
    val estimatedAgeMonths: Int?
)
