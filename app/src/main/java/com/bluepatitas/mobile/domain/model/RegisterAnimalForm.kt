package com.bluepatitas.mobile.domain.model

data class RegisterAnimalForm(
    val name: String,
    val species: String,
    val breed: String,
    val estimatedAgeMonths: Int,
    val weightKg: Double,
    val photoUrl: String? = null
)
