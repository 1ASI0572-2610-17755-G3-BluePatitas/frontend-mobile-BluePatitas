package com.bluepatitas.mobile.data.remote.shelter

data class ShelterRequestDto(
    val name: String,
    val city: String,
    val address: String,
    val administrator: String,
    val phone: String,
    val email: String
)

data class ShelterDto(
    val id: String?,
    val name: String?,
    val city: String?,
    val address: String?,
    val administrator: String?,
    val phone: String?,
    val email: String?
)
