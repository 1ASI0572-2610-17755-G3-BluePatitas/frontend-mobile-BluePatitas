package com.bluepatitas.mobile.domain.model

data class ShelterProfile(
    val id: String,
    val name: String,
    val taxId: String,
    val institutionalEmail: String,
    val contactPhone: String,
    val address: String,
    val reference: String,
    val district: String,
    val city: String
)
