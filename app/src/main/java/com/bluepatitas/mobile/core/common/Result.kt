package com.bluepatitas.mobile.core.common

sealed interface BluePatitasResult<out T> {
    data class Success<T>(val value: T) : BluePatitasResult<T>
    data class Error(val throwable: Throwable) : BluePatitasResult<Nothing>
}
