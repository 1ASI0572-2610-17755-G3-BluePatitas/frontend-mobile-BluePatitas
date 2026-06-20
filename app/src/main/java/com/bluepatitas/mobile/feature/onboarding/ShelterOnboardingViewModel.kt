package com.bluepatitas.mobile.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.util.digitsOnly
import com.bluepatitas.mobile.core.util.hasDigitLengthInRange
import com.bluepatitas.mobile.core.util.isValidEmail
import com.bluepatitas.mobile.data.repository.ShelterRepositoryException
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.ShelterDraft
import com.bluepatitas.mobile.domain.model.ShelterProfile
import com.bluepatitas.mobile.domain.usecase.CreateShelterUseCase
import com.bluepatitas.mobile.domain.usecase.SaveShelterDraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShelterOnboardingUiState(
    val draft: ShelterDraft = ShelterDraft(),
    val errors: Map<String, AuthFieldErrorBridge> = emptyMap(),
    val formError: AuthFieldErrorBridge? = null,
    val isSubmitting: Boolean = false,
    val createdShelter: ShelterProfile? = null
)

enum class AuthFieldErrorBridge {
    Required,
    InvalidEmail,
    InvalidPhoneLength,
    InvalidTaxIdLength,
    InvalidShelterData,
    ShelterAlreadyExists,
    SessionExpired,
    EndpointNotFound,
    ServerError,
    Timeout,
    Network,
    ResponseFormat,
    Unknown
}

@HiltViewModel
class ShelterOnboardingViewModel @Inject constructor(
    private val saveShelterDraftUseCase: SaveShelterDraftUseCase,
    private val createShelterUseCase: CreateShelterUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShelterOnboardingUiState())
    val uiState: StateFlow<ShelterOnboardingUiState> = _uiState

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            val draft = state.draft
            val sanitizedValue = when (field) {
                "taxId" -> digitsOnly(value, maxLength = 11)
                "contactPhone" -> digitsOnly(value, maxLength = 15)
                else -> value
            }
            state.copy(
                draft = when (field) {
                    "name" -> draft.copy(name = sanitizedValue)
                    "taxId" -> draft.copy(taxId = sanitizedValue)
                    "institutionalEmail" -> draft.copy(institutionalEmail = sanitizedValue)
                    "contactPhone" -> draft.copy(contactPhone = sanitizedValue)
                    "address" -> draft.copy(address = sanitizedValue)
                    "reference" -> draft.copy(reference = sanitizedValue)
                    "district" -> draft.copy(district = sanitizedValue)
                    "city" -> draft.copy(city = sanitizedValue)
                    else -> draft
                },
                errors = state.errors - field,
                formError = null
            )
        }
    }

    fun validateBasicInfo(): Boolean {
        val draft = _uiState.value.draft
        val errors = buildMap {
            if (draft.name.isBlank()) put("name", AuthFieldErrorBridge.Required)
            when {
                draft.taxId.isBlank() -> put("taxId", AuthFieldErrorBridge.Required)
                !hasDigitLengthInRange(draft.taxId, minLength = 8, maxLength = 11) -> {
                    put("taxId", AuthFieldErrorBridge.InvalidTaxIdLength)
                }
            }
            when {
                draft.institutionalEmail.isBlank() -> put("institutionalEmail", AuthFieldErrorBridge.Required)
                !isValidEmail(draft.institutionalEmail) -> put("institutionalEmail", AuthFieldErrorBridge.InvalidEmail)
            }
            when {
                draft.contactPhone.isBlank() -> put("contactPhone", AuthFieldErrorBridge.Required)
                !hasDigitLengthInRange(draft.contactPhone, minLength = 7, maxLength = 15) -> {
                    put("contactPhone", AuthFieldErrorBridge.InvalidPhoneLength)
                }
            }
        }
        _uiState.update { it.copy(errors = errors, formError = null) }
        if (errors.isEmpty()) {
            viewModelScope.launch { saveShelterDraftUseCase(draft) }
        }
        return errors.isEmpty()
    }

    fun validateLocation(): Boolean {
        val draft = _uiState.value.draft
        val errors = buildMap {
            if (draft.address.isBlank()) put("address", AuthFieldErrorBridge.Required)
            if (draft.district.isBlank()) put("district", AuthFieldErrorBridge.Required)
            if (draft.city.isBlank()) put("city", AuthFieldErrorBridge.Required)
        }
        _uiState.update { it.copy(errors = errors, formError = null) }
        return errors.isEmpty()
    }

    fun createShelter() {
        if (!validateLocation()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, formError = null) }
            runCatching { createShelterUseCase(_uiState.value.draft) }
                .onSuccess { shelter ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdShelter = shelter,
                            formError = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            formError = error.toFieldError()
                        )
                    }
                }
        }
    }
}

private fun Throwable.toFieldError(): AuthFieldErrorBridge =
    when ((this as? ShelterRepositoryException)?.reason) {
        AuthFailureReason.BadRequest -> AuthFieldErrorBridge.InvalidShelterData
        AuthFailureReason.Conflict -> AuthFieldErrorBridge.ShelterAlreadyExists
        AuthFailureReason.SessionExpired -> AuthFieldErrorBridge.SessionExpired
        AuthFailureReason.EndpointNotFound -> AuthFieldErrorBridge.EndpointNotFound
        AuthFailureReason.ServerError -> AuthFieldErrorBridge.ServerError
        AuthFailureReason.Timeout -> AuthFieldErrorBridge.Timeout
        AuthFailureReason.Network -> AuthFieldErrorBridge.Network
        AuthFailureReason.Serialization -> AuthFieldErrorBridge.ResponseFormat
        AuthFailureReason.MissingRole,
        AuthFailureReason.Unknown,
        null -> AuthFieldErrorBridge.Unknown
    }
