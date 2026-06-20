package com.bluepatitas.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.util.isValidEmail
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.usecase.LoginUseCase
import com.bluepatitas.mobile.domain.usecase.ObserveShelterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: AuthFieldError? = null,
    val passwordError: AuthFieldError? = null,
    val formError: AuthFieldError? = null,
    val isSubmitting: Boolean = false,
    val destination: LoginDestination? = null
)

enum class AuthFieldError {
    Required,
    InvalidEmail,
    PasswordRequired,
    PasswordTooShort,
    PasswordsDoNotMatch,
    TermsRequired,
    InvalidCredentials,
    InvalidInvitationCode,
    InvalidPhoneLength,
    InvalidRegistrationData,
    EmailAlreadyRegistered,
    EndpointNotFound,
    ServerError,
    Timeout,
    Network,
    ResponseFormat,
    MissingRole,
    ConnectionError
}

sealed interface LoginDestination {
    data object AdminOnboarding : LoginDestination
    data object AdminMain : LoginDestination
    data object VeterinarianMain : LoginDestination
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val observeShelterUseCase: ObserveShelterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }
    }

    fun onPasswordVisibilityChange(visible: Boolean) {
        _uiState.update { it.copy(passwordVisible = visible) }
    }

    fun clearDestination() {
        _uiState.update { it.copy(destination = null) }
    }

    fun submit() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> AuthFieldError.Required
            !isValidEmail(state.email) -> AuthFieldError.InvalidEmail
            else -> null
        }
        val passwordError = if (state.password.isBlank()) AuthFieldError.PasswordRequired else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = loginUseCase(LoginCredentials(state.email, state.password))) {
                AuthResult.InvalidCredentials -> _uiState.update {
                    it.copy(isSubmitting = false, formError = AuthFieldError.InvalidCredentials)
                }

                is AuthResult.Success -> {
                    val destination = when (result.session.role) {
                        UserRole.VETERINARIAN -> LoginDestination.VeterinarianMain
                        UserRole.SHELTER_ADMIN -> {
                            val shelter = observeShelterUseCase().first()
                            val isBackendSession = !result.session.token.isNullOrBlank()
                            if (result.session.onboardingCompleted || (!isBackendSession && shelter != null)) {
                                LoginDestination.AdminMain
                            } else {
                                LoginDestination.AdminOnboarding
                            }
                        }
                    }
                    _uiState.update { it.copy(isSubmitting = false, destination = destination) }
                }

                AuthResult.RegistrationSuccess -> Unit
                AuthResult.InvalidInvitationCode -> Unit
                is AuthResult.ConnectionError -> _uiState.update {
                    it.copy(isSubmitting = false, formError = result.toFieldError())
                }
            }
        }
    }
}

private fun AuthResult.ConnectionError.toFieldError(): AuthFieldError =
    when (reason) {
        AuthFailureReason.BadRequest -> AuthFieldError.ConnectionError
        AuthFailureReason.Conflict -> AuthFieldError.ConnectionError
        AuthFailureReason.EndpointNotFound -> AuthFieldError.EndpointNotFound
        AuthFailureReason.ServerError -> AuthFieldError.ServerError
        AuthFailureReason.Timeout -> AuthFieldError.Timeout
        AuthFailureReason.Network -> AuthFieldError.Network
        AuthFailureReason.Serialization -> AuthFieldError.ResponseFormat
        AuthFailureReason.MissingRole -> AuthFieldError.MissingRole
        AuthFailureReason.Unknown -> AuthFieldError.ConnectionError
    }
