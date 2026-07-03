package com.bluepatitas.mobile.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.repository.AnimalRepositoryException
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.RegisterAnimalForm
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import com.bluepatitas.mobile.domain.usecase.CreateAnimalUseCase
import com.bluepatitas.mobile.domain.usecase.GetAnimalDetailUseCase
import com.bluepatitas.mobile.domain.usecase.GetAnimalsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringAlertsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringZonesUseCase
import com.bluepatitas.mobile.domain.usecase.UpdateAnimalHealthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainDataUiState(
    val isLoading: Boolean = true,
    val animals: List<AnimalSummary> = emptyList(),
    val zones: List<MonitoringZone> = emptyList(),
    val alerts: List<MonitoringAlert> = emptyList(),
    val selectedZone: MonitoringZone? = null,
    val usingFallbackAnimals: Boolean = false,
    val usingFallbackMonitoring: Boolean = false,
    val animalLoadError: AnimalActionError? = null,
    val errorMessageVisible: Boolean = false,
    val showRegisterAnimalForm: Boolean = false,
    val animalForm: AnimalFormUiState = AnimalFormUiState(),
    val animalFormErrors: Map<String, AnimalFieldError> = emptyMap(),
    val isSavingAnimal: Boolean = false,
    val animalActionError: AnimalActionError? = null,
    val animalCreatedMessageVisible: Boolean = false,
    val selectedAnimalDetail: AnimalSummary? = null,
    val isLoadingAnimalDetail: Boolean = false,
    val animalDetailError: AnimalActionError? = null,
    val showHealthEditor: Boolean = false,
    val selectedHealthCondition: String? = null,
    val isUpdatingAnimalHealth: Boolean = false,
    val animalHealthUpdateError: AnimalActionError? = null,
    val animalHealthUpdatedMessageVisible: Boolean = false,
    val geofenceStatus: GeofenceStatus = GeofenceStatus.InsideSafeZone,
    val cameraPermissionDenied: Boolean = false,
    val notificationPermissionDenied: Boolean = false
)

data class AnimalFormUiState(
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val estimatedAgeMonths: String = "",
    val weightKg: String = ""
)

enum class AnimalFieldError {
    Required,
    InvalidAge,
    InvalidWeight
}

enum class AnimalActionError {
    BadRequest,
    SessionExpired,
    EndpointNotFound,
    ServerError,
    Timeout,
    Network,
    ResponseFormat,
    Unknown
}

enum class AnimalHealthOption(val apiValue: String) {
    Healthy("HEALTHY"),
    InTreatment("IN_TREATMENT"),
    Critical("CRITICAL"),
    UnderObservation("UNDER_OBSERVATION");

    companion object {
        fun fromApiValue(value: String?): AnimalHealthOption? =
            entries.firstOrNull { it.apiValue == value?.trim()?.uppercase() }
    }
}

enum class GeofenceStatus {
    InsideSafeZone,
    OutsideSafeZone
}

@HiltViewModel
class MainDataViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val getAnimalDetailUseCase: GetAnimalDetailUseCase,
    private val createAnimalUseCase: CreateAnimalUseCase,
    private val updateAnimalHealthUseCase: UpdateAnimalHealthUseCase,
    private val getMonitoringZonesUseCase: GetMonitoringZonesUseCase,
    private val getMonitoringAlertsUseCase: GetMonitoringAlertsUseCase,
    private val monitoringRepository: MonitoringRepository
) : ViewModel() {

    private val remoteState = MutableStateFlow(MainDataUiState())
    private val _uiState = MutableStateFlow(MainDataUiState())
    val uiState: StateFlow<MainDataUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(remoteState, monitoringRepository.localAlerts) { state, localAlerts ->
                state.copy(
                    alerts = localAlerts + state.alerts.filterNot { remote ->
                        localAlerts.any { it.id == remote.id }
                    },
                    geofenceStatus = if (localAlerts.isNotEmpty()) {
                        GeofenceStatus.OutsideSafeZone
                    } else {
                        state.geofenceStatus
                    }
                )
            }.collect { combined ->
                _uiState.value = combined
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    isLoading = true,
                    errorMessageVisible = false,
                    animalLoadError = null,
                    animalActionError = null
                )
            }
            var animalLoadError: AnimalActionError? = null
            val animals = when (val result = getAnimalsUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> {
                    animalLoadError = result.throwable.toAnimalActionError()
                    if (animalLoadError.allowsDemoFallback()) {
                        fallbackAnimals()
                    } else {
                        emptyList()
                    }
                }
            }
            val zones = when (val result = getMonitoringZonesUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> fallbackZones()
            }
            var alertsFailed = false
            val alerts = when (val result = getMonitoringAlertsUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> {
                    alertsFailed = true
                    emptyList()
                }
            }
            val animalFallback = animals === fallbackAnimalsReference
            val monitoringFallback = zones === fallbackZonesReference || alertsFailed
            remoteState.update {
                it.copy(
                    isLoading = false,
                    animals = animals,
                    zones = zones,
                    alerts = alerts,
                    selectedZone = it.selectedZone ?: zones.firstOrNull(),
                    usingFallbackAnimals = animalFallback,
                    usingFallbackMonitoring = monitoringFallback,
                    animalLoadError = animalLoadError,
                    errorMessageVisible = animalFallback || monitoringFallback
                )
            }
        }
    }

    fun showRegisterAnimalForm() {
        remoteState.update {
            it.copy(
                showRegisterAnimalForm = true,
                animalForm = AnimalFormUiState(),
                animalFormErrors = emptyMap(),
                animalActionError = null,
                animalCreatedMessageVisible = false
            )
        }
    }

    fun hideRegisterAnimalForm() {
        remoteState.update {
            it.copy(
                showRegisterAnimalForm = false,
                animalFormErrors = emptyMap(),
                animalActionError = null,
                isSavingAnimal = false
            )
        }
    }

    fun updateAnimalForm(field: String, value: String) {
        remoteState.update { state ->
            val sanitizedValue = when (field) {
                "estimatedAgeMonths" -> value.filter { it.isDigit() }
                "weightKg" -> value.sanitizeDecimalInput()
                else -> value
            }
            val form = when (field) {
                "name" -> state.animalForm.copy(name = sanitizedValue)
                "species" -> state.animalForm.copy(species = sanitizedValue)
                "breed" -> state.animalForm.copy(breed = sanitizedValue)
                "estimatedAgeMonths" -> state.animalForm.copy(estimatedAgeMonths = sanitizedValue)
                "weightKg" -> state.animalForm.copy(weightKg = sanitizedValue)
                else -> state.animalForm
            }
            state.copy(
                animalForm = form,
                animalFormErrors = state.animalFormErrors - field,
                animalActionError = null
            )
        }
    }

    fun createAnimal() {
        val state = remoteState.value
        val errors = validateAnimalForm(state.animalForm)
        if (errors.isNotEmpty()) {
            remoteState.update { it.copy(animalFormErrors = errors) }
            return
        }
        val form = state.animalForm.toDomainForm() ?: return
        viewModelScope.launch {
            remoteState.update { it.copy(isSavingAnimal = true, animalActionError = null) }
            when (val result = createAnimalUseCase(form)) {
                is BluePatitasResult.Success -> {
                    remoteState.update {
                        it.copy(
                            isSavingAnimal = false,
                            showRegisterAnimalForm = false,
                            animalForm = AnimalFormUiState(),
                            animalFormErrors = emptyMap(),
                            animalCreatedMessageVisible = true
                        )
                    }
                    refresh()
                }
                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isSavingAnimal = false,
                        animalActionError = result.throwable.toAnimalActionError()
                    )
                }
            }
        }
    }

    fun dismissAnimalCreatedMessage() {
        remoteState.update { it.copy(animalCreatedMessageVisible = false) }
    }

    fun openAnimalDetail(animal: AnimalSummary) {
        remoteState.update {
            it.copy(
                selectedAnimalDetail = animal,
                isLoadingAnimalDetail = true,
                animalDetailError = null,
                showHealthEditor = false,
                selectedHealthCondition = AnimalHealthOption.fromApiValue(animal.healthCondition)?.apiValue,
                animalHealthUpdateError = null,
                animalHealthUpdatedMessageVisible = false
            )
        }
        viewModelScope.launch {
            when (val result = getAnimalDetailUseCase(animal.id)) {
                is BluePatitasResult.Success -> remoteState.update {
                    it.copy(
                        selectedAnimalDetail = result.value,
                        selectedHealthCondition = AnimalHealthOption.fromApiValue(result.value.healthCondition)?.apiValue,
                        isLoadingAnimalDetail = false
                    )
                }
                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        selectedAnimalDetail = animal,
                        isLoadingAnimalDetail = false,
                        animalDetailError = result.throwable.toAnimalActionError()
                    )
                }
            }
        }
    }

    fun retryAnimalDetail() {
        remoteState.value.selectedAnimalDetail?.let(::openAnimalDetail)
    }

    fun showHealthEditor() {
        remoteState.update { state ->
            state.copy(
                showHealthEditor = true,
                selectedHealthCondition = state.selectedHealthCondition
                    ?: AnimalHealthOption.fromApiValue(state.selectedAnimalDetail?.healthCondition)?.apiValue
                    ?: AnimalHealthOption.Healthy.apiValue,
                animalHealthUpdateError = null,
                animalHealthUpdatedMessageVisible = false
            )
        }
    }

    fun hideHealthEditor() {
        remoteState.update {
            it.copy(
                showHealthEditor = false,
                animalHealthUpdateError = null,
                selectedHealthCondition = AnimalHealthOption.fromApiValue(it.selectedAnimalDetail?.healthCondition)?.apiValue
            )
        }
    }

    fun selectHealthCondition(healthCondition: String) {
        remoteState.update {
            it.copy(
                selectedHealthCondition = healthCondition,
                animalHealthUpdateError = null,
                animalHealthUpdatedMessageVisible = false
            )
        }
    }

    fun dismissAnimalHealthUpdatedMessage() {
        remoteState.update { it.copy(animalHealthUpdatedMessageVisible = false) }
    }

    fun updateAnimalHealth() {
        val state = remoteState.value
        val animal = state.selectedAnimalDetail ?: return
        val selectedHealth = state.selectedHealthCondition ?: return
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    isUpdatingAnimalHealth = true,
                    animalHealthUpdateError = null,
                    animalHealthUpdatedMessageVisible = false
                )
            }
            when (val result = updateAnimalHealthUseCase(animal.id, selectedHealth)) {
                is BluePatitasResult.Success -> {
                    val updatedDetail = when (val detailResult = getAnimalDetailUseCase(animal.id)) {
                        is BluePatitasResult.Success -> detailResult.value
                        is BluePatitasResult.Error -> animal.copy(healthCondition = selectedHealth)
                    }
                    val animalsResult = getAnimalsUseCase()
                    val updatedAnimals = when (animalsResult) {
                        is BluePatitasResult.Success -> animalsResult.value
                        is BluePatitasResult.Error -> remoteState.value.animals.map {
                            if (it.id == animal.id) it.copy(healthCondition = selectedHealth) else it
                        }
                    }
                    remoteState.update {
                        it.copy(
                            animals = updatedAnimals,
                            selectedAnimalDetail = updatedDetail,
                            selectedHealthCondition = AnimalHealthOption.fromApiValue(updatedDetail.healthCondition)?.apiValue,
                            usingFallbackAnimals = if (animalsResult is BluePatitasResult.Success) false else it.usingFallbackAnimals,
                            animalLoadError = if (animalsResult is BluePatitasResult.Success) null else it.animalLoadError,
                            isUpdatingAnimalHealth = false,
                            showHealthEditor = false,
                            animalHealthUpdatedMessageVisible = true,
                            animalHealthUpdateError = null
                        )
                    }
                }

                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isUpdatingAnimalHealth = false,
                        animalHealthUpdateError = result.throwable.toAnimalActionError()
                    )
                }
            }
        }
    }

    fun closeAnimalDetail() {
        remoteState.update {
            it.copy(
                selectedAnimalDetail = null,
                isLoadingAnimalDetail = false,
                animalDetailError = null,
                showHealthEditor = false,
                selectedHealthCondition = null,
                isUpdatingAnimalHealth = false,
                animalHealthUpdateError = null,
                animalHealthUpdatedMessageVisible = false
            )
        }
    }

    fun selectZone(zone: MonitoringZone) {
        remoteState.update { it.copy(selectedZone = zone) }
    }

    fun simulateBreach(session: AppSession) {
        val zone = _uiState.value.selectedZone ?: _uiState.value.zones.firstOrNull() ?: return
        viewModelScope.launch {
            monitoringRepository.simulateBreach(zone, session.shelterName)
            remoteState.update { it.copy(geofenceStatus = GeofenceStatus.OutsideSafeZone) }
        }
    }

    fun simulateSafePosition() {
        viewModelScope.launch {
            monitoringRepository.clearLocalAlerts()
            remoteState.update { it.copy(geofenceStatus = GeofenceStatus.InsideSafeZone) }
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            monitoringRepository.resolveLocalAlert(alertId)
        }
    }

    fun markCameraPermissionDenied() {
        remoteState.update { it.copy(cameraPermissionDenied = true) }
    }

    fun clearCameraPermissionDenied() {
        remoteState.update { it.copy(cameraPermissionDenied = false) }
    }

    fun markNotificationPermissionDenied() {
        remoteState.update { it.copy(notificationPermissionDenied = true) }
    }

    private companion object {
        val fallbackAnimalsReference = listOf(
            AnimalSummary("fallback-1", "Firulais", null, "Dog", "Mixed", 24, "HEALTHY", 14.5, "Patio principal"),
            AnimalSummary("fallback-2", "Luna", null, "Dog", "Mixed", 18, "OBSERVATION", 12.5, "Patio principal"),
            AnimalSummary("fallback-3", "Michi", null, "Cat", "Domestic Shorthair", 18, "HEALTHY", 4.2, "Zona tranquila")
        )
        val fallbackZonesReference = listOf(
            MonitoringZone(
                id = "fallback-zone",
                shelterId = null,
                targetId = "fallback-target",
                name = "Puppy Area",
                temperatureC = 27.0,
                humidity = 63.0,
                minTemperatureC = 20.0,
                maxTemperatureC = 30.0,
                status = "Normal",
                animalCount = 3,
                cameraEnabled = true,
                imageUrl = null
            )
        )

        fun fallbackAnimals() = fallbackAnimalsReference
        fun fallbackZones() = fallbackZonesReference
    }
}

private fun validateAnimalForm(form: AnimalFormUiState): Map<String, AnimalFieldError> =
    buildMap {
        if (form.name.isBlank()) put("name", AnimalFieldError.Required)
        if (form.species.isBlank()) put("species", AnimalFieldError.Required)
        val age = form.estimatedAgeMonths.toIntOrNull()
        if (age == null || age < 0) put("estimatedAgeMonths", AnimalFieldError.InvalidAge)
        val weight = form.weightKg.toDoubleOrNull()
        if (weight == null || weight <= 0.0) put("weightKg", AnimalFieldError.InvalidWeight)
    }

private fun AnimalFormUiState.toDomainForm(): RegisterAnimalForm? {
    val age = estimatedAgeMonths.toIntOrNull() ?: return null
    val weight = weightKg.toDoubleOrNull() ?: return null
    return RegisterAnimalForm(
        name = name.trim(),
        species = species.trim(),
        breed = breed.trim(),
        estimatedAgeMonths = age,
        weightKg = weight
    )
}

private fun String.sanitizeDecimalInput(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val firstDotIndex = filtered.indexOf('.')
    if (firstDotIndex == -1) return filtered
    return buildString {
        filtered.forEachIndexed { index, char ->
            if (char != '.' || index == firstDotIndex) append(char)
        }
    }
}

private fun Throwable.toAnimalActionError(): AnimalActionError {
    val reason = (this as? AnimalRepositoryException)?.reason
    return when (reason) {
        AuthFailureReason.BadRequest -> AnimalActionError.BadRequest
        AuthFailureReason.SessionExpired -> AnimalActionError.SessionExpired
        AuthFailureReason.EndpointNotFound -> AnimalActionError.EndpointNotFound
        AuthFailureReason.ServerError -> AnimalActionError.ServerError
        AuthFailureReason.Timeout -> AnimalActionError.Timeout
        AuthFailureReason.Network -> AnimalActionError.Network
        AuthFailureReason.Serialization -> AnimalActionError.ResponseFormat
        AuthFailureReason.Conflict, AuthFailureReason.MissingRole, AuthFailureReason.Unknown, null -> AnimalActionError.Unknown
    }
}

private fun AnimalActionError?.allowsDemoFallback(): Boolean =
    when (this) {
        AnimalActionError.ServerError,
        AnimalActionError.Timeout,
        AnimalActionError.Network,
        AnimalActionError.Unknown -> true
        AnimalActionError.BadRequest,
        AnimalActionError.SessionExpired,
        AnimalActionError.EndpointNotFound,
        AnimalActionError.ResponseFormat,
        null -> false
    }
