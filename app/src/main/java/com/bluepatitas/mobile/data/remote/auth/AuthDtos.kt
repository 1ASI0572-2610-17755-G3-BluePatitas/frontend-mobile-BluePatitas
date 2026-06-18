package com.bluepatitas.mobile.data.remote.auth

import com.google.gson.annotations.SerializedName

data class SignInRequest(
    val email: String,
    val password: String
)

data class AuthenticatedUserDto(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val token: String,
    val shelterId: String?,
    val role: String?,
    val roles: List<String>?,
    val shelterName: String?,
    @SerializedName("onboardingCompleted")
    val onboardingCompleted: Boolean?
)
