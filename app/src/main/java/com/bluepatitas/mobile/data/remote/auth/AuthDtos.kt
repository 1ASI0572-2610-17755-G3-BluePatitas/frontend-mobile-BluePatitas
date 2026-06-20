package com.bluepatitas.mobile.data.remote.auth

import com.google.gson.annotations.SerializedName

data class SignInRequest(
    val email: String,
    val password: String
)

data class AuthenticatedUserDto(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    @SerializedName(value = "token", alternate = ["accessToken", "jwt"])
    val token: String,
    val shelterId: String?,
    val role: String?,
    val roles: List<String>?,
    val shelterName: String?,
    @SerializedName("onboardingCompleted")
    val onboardingCompleted: Boolean?
)

data class UserDto(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val phoneNumber: String?,
    val roles: List<String>?
)
