package com.bluepatitas.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.util.isValidEmail
import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.usecase.AcceptInvitationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitationUiState(
    val code: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errors: Map<String, AuthFieldError> = emptyMap(),
    val isSubmitting: Boolean = false,
    val completed: Boolean = false
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val acceptInvitationUseCase: AcceptInvitationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            state.copy(
                code = if (field == "code") value else state.code,
                email = if (field == "email") value else state.email,
                password = if (field == "password") value else state.password,
                errors = state.errors - field
            )
        }
    }

    fun setPasswordVisible(visible: Boolean) {
        _uiState.update { it.copy(passwordVisible = visible) }
    }

    fun clearCompleted() {
        _uiState.update { it.copy(completed = false) }
    }

    fun submit() {
        val state = _uiState.value
        val errors = buildMap {
            if (state.code.isBlank()) put("code", AuthFieldError.Required)
            when {
                state.email.isBlank() -> put("email", AuthFieldError.Required)
                !isValidEmail(state.email) -> put("email", AuthFieldError.InvalidEmail)
            }
            if (state.password.isBlank()) put("password", AuthFieldError.PasswordRequired)
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (acceptInvitationUseCase(InvitationForm(state.code, state.email, state.password))) {
                AuthResult.InvalidInvitationCode -> _uiState.update {
                    it.copy(isSubmitting = false, errors = mapOf("code" to AuthFieldError.InvalidInvitationCode))
                }

                is AuthResult.Success -> _uiState.update { it.copy(isSubmitting = false, completed = true) }
                AuthResult.InvalidCredentials -> Unit
            }
        }
    }
}
