package com.bluepatitas.mobile.core.util

private val EmailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

fun isValidEmail(value: String): Boolean = EmailRegex.matches(value.trim())

fun digitsOnly(value: String, maxLength: Int): String =
    value.filter(Char::isDigit).take(maxLength)

fun hasDigitLengthInRange(value: String, minLength: Int, maxLength: Int): Boolean =
    value.length in minLength..maxLength
