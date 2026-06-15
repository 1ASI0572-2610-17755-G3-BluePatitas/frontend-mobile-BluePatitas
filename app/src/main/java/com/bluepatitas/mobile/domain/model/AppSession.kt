package com.bluepatitas.mobile.domain.model

data class AppSession(
    val userId: String,
    val displayName: String,
    val email: String,
    val role: UserRole,
    val shelterId: String?
)
