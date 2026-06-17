package com.bluepatitas.mobile.feature.auth

import androidx.lifecycle.ViewModel
import com.bluepatitas.mobile.core.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: AuthFieldError? = null,
    val success: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, success = false) }
    }

    fun submit() {
        val state = _uiState.value
        val error = when {
            state.email.isBlank() -> AuthFieldError.Required
            !isValidEmail(state.email) -> AuthFieldError.InvalidEmail
            else -> null
        }
        _uiState.update { it.copy(emailError = error, success = error == null) }
    }
}
