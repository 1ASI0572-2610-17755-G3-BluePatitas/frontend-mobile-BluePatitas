package com.bluepatitas.mobile.domain.model

data class AppSession(
    val userId: String,
    val firstName: String = "",
    val lastName: String = "",
    val displayName: String,
    val email: String,
    val token: String? = null,
    val role: UserRole,
    val shelterId: String?,
    val shelterName: String? = null,
    val onboardingCompleted: Boolean = false
)
