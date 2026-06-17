package com.bluepatitas.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.util.isValidEmail
import com.bluepatitas.mobile.domain.model.RegisterAdminForm
import com.bluepatitas.mobile.domain.usecase.RegisterAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val errors: Map<String, AuthFieldError> = emptyMap(),
    val isSubmitting: Boolean = false,
    val completed: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerAdminUseCase: RegisterAdminUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            state.copy(
                firstName = if (field == "firstName") value else state.firstName,
                lastName = if (field == "lastName") value else state.lastName,
                email = if (field == "email") value else state.email,
                phone = if (field == "phone") value else state.phone,
                password = if (field == "password") value else state.password,
                confirmPassword = if (field == "confirmPassword") value else state.confirmPassword,
                errors = state.errors - field
            )
        }
    }

    fun setPasswordVisible(visible: Boolean) {
        _uiState.update { it.copy(passwordVisible = visible) }
    }

    fun setConfirmPasswordVisible(visible: Boolean) {
        _uiState.update { it.copy(confirmPasswordVisible = visible) }
    }

    fun setAcceptedTerms(accepted: Boolean) {
        _uiState.update { it.copy(acceptedTerms = accepted, errors = it.errors - "terms") }
    }

    fun clearCompleted() {
        _uiState.update { it.copy(completed = false) }
    }

    fun submit() {
        val state = _uiState.value
        val errors = buildMap {
            if (state.firstName.isBlank()) put("firstName", AuthFieldError.Required)
            if (state.lastName.isBlank()) put("lastName", AuthFieldError.Required)
            when {
                state.email.isBlank() -> put("email", AuthFieldError.Required)
                !isValidEmail(state.email) -> put("email", AuthFieldError.InvalidEmail)
            }
            if (state.phone.isBlank()) put("phone", AuthFieldError.Required)
            when {
                state.password.isBlank() -> put("password", AuthFieldError.PasswordRequired)
                state.password.length < 6 -> put("password", AuthFieldError.PasswordTooShort)
            }
            if (state.confirmPassword != state.password) put("confirmPassword", AuthFieldError.PasswordsDoNotMatch)
            if (!state.acceptedTerms) put("terms", AuthFieldError.TermsRequired)
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            registerAdminUseCase(
                RegisterAdminForm(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    phone = state.phone,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    acceptedTerms = state.acceptedTerms
                )
            )
            _uiState.update { it.copy(isSubmitting = false, completed = true) }
        }
    }
}
