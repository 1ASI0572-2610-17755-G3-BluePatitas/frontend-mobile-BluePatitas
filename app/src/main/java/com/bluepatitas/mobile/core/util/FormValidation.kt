package com.bluepatitas.mobile.core.util

private val EmailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

fun isValidEmail(value: String): Boolean = EmailRegex.matches(value.trim())
