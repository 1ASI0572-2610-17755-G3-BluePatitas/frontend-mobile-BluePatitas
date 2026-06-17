package com.bluepatitas.mobile.domain.model

data class RegisterAdminForm(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String,
    val acceptedTerms: Boolean
)
