package com.bluepatitas.mobile.domain.model

data class VeterinaryAnimal(
    val id: String,
    val photoUrl: String?,
    val name: String,
    val species: String,
    val breed: String?,
    val healthCondition: String?,
    val weightKg: Double?
)
