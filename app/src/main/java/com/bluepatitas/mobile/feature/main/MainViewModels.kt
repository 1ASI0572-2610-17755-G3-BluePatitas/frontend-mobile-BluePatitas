package com.bluepatitas.mobile.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.repository.AnimalRepositoryException
import com.bluepatitas.mobile.data.repository.MonitoringRepositoryException
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.CreateMonitoringZoneForm
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.RegisterAnimalForm
import com.bluepatitas.mobile.domain.model.TelemetryRecord
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import com.bluepatitas.mobile.domain.usecase.AssignAnimalToPerimeterUseCase
import com.bluepatitas.mobile.domain.usecase.CreateAnimalUseCase
import com.bluepatitas.mobile.domain.usecase.CreateMonitoringZoneUseCase
import com.bluepatitas.mobile.domain.usecase.EnableAlertTrackingUseCase
import com.bluepatitas.mobile.domain.usecase.GetAnimalDetailUseCase
import com.bluepatitas.mobile.domain.usecase.GetAnimalsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringAlertsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringZonesUseCase
import com.bluepatitas.mobile.domain.usecase.GetTelemetryUseCase
import com.bluepatitas.mobile.domain.usecase.ResolveMonitoringAlertUseCase
import com.bluepatitas.mobile.domain.usecase.UpdateAnimalHealthUseCase
import com.bluepatitas.mobile.domain.usecase.UploadAnimalImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MainDataUiState(
    val isLoading: Boolean = true,
    val animals: List<AnimalSummary> = emptyList(),
    val zones: List<MonitoringZone> = emptyList(),
    val alerts: List<MonitoringAlert> = emptyList(),
    val selectedZone: MonitoringZone? = null,
    val usingFallbackAnimals: Boolean = false,
    val usingFallbackMonitoring: Boolean = false,
    val animalLoadError: AnimalActionError? = null,
    val monitoringLoadError: AnimalActionError? = null,
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
    val showZoneAssignmentEditor: Boolean = false,
    val selectedPerimeterId: String? = null,
    val isAssigningAnimalZone: Boolean = false,
    val animalZoneAssignmentError: AnimalActionError? = null,
    val animalZoneAssignedMessageVisible: Boolean = false,
    val showCreateZoneForm: Boolean = false,
    val zoneForm: MonitoringZoneFormUiState = MonitoringZoneFormUiState(),
    val zoneFormErrors: Map<String, MonitoringFieldError> = emptyMap(),
    val isSavingZone: Boolean = false,
    val monitoringActionError: AnimalActionError? = null,
    val zoneCreatedMessageVisible: Boolean = false,
    val selectedZoneTelemetry: List<TelemetryRecord> = emptyList(),
    val isLoadingTelemetry: Boolean = false,
    val telemetryError: AnimalActionError? = null,
    val isRefreshingMonitoring: Boolean = false,
    val isRefreshingAlerts: Boolean = false,
    val lastMonitoringRefreshMillis: Long? = null,
    val lastAlertsRefreshMillis: Long? = null,
    val isResolvingAlertId: String? = null,
    val isEnablingTrackingAlertId: String? = null,
    val geofenceStatus: GeofenceStatus = GeofenceStatus.InsideSafeZone,
    val cameraPermissionDenied: Boolean = false,
    val notificationPermissionDenied: Boolean = false
)

data class MonitoringZoneFormUiState(
    val targetId: String = "",
    val name: String = "",
    val temperatureC: String = "",
    val humidity: String = "",
    val status: String = "ACTIVE",
    val animalCount: String = "0",
    val cameraEnabled: Boolean = true,
    val imageUrl: String = "",
    val minTemperatureC: String = "",
    val maxTemperatureC: String = ""
)

data class AnimalFormUiState(
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val estimatedAgeMonths: String = "",
    val weightKg: String = "",
    val selectedImageUri: String? = null
)

enum class AnimalFieldError {
    Required,
    InvalidAge,
    InvalidWeight
}

enum class MonitoringFieldError {
    Required,
    InvalidNumber,
    InvalidCount,
    InvalidRange
}

enum class AnimalActionError {
    BadRequest,
    SessionExpired,
    EndpointNotFound,
    ServerError,
    Timeout,
    Network,
    ResponseFormat,
    InvalidImageUpload,
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
    private val assignAnimalToPerimeterUseCase: AssignAnimalToPerimeterUseCase,
    private val uploadAnimalImageUseCase: UploadAnimalImageUseCase,
    private val getMonitoringZonesUseCase: GetMonitoringZonesUseCase,
    private val getMonitoringAlertsUseCase: GetMonitoringAlertsUseCase,
    private val createMonitoringZoneUseCase: CreateMonitoringZoneUseCase,
    private val resolveMonitoringAlertUseCase: ResolveMonitoringAlertUseCase,
    private val enableAlertTrackingUseCase: EnableAlertTrackingUseCase,
    private val getTelemetryUseCase: GetTelemetryUseCase,
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
                    monitoringLoadError = null,
                    animalActionError = null,
                    monitoringActionError = null
                )
            }
            var animalLoadError: AnimalActionError? = null
            var monitoringLoadError: AnimalActionError? = null
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
                is BluePatitasResult.Error -> {
                    val error = result.throwable.toMonitoringActionError()
                    monitoringLoadError = error
                    if (error.allowsDemoFallback()) fallbackZones() else emptyList()
                }
            }
            var alertsFailed = false
            val alerts = when (val result = getMonitoringAlertsUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> {
                    val error = result.throwable.toMonitoringActionError()
                    monitoringLoadError = monitoringLoadError ?: error
                    alertsFailed = error.allowsDemoFallback()
                    emptyList()
                }
            }
            val animalFallback = animals === fallbackAnimalsReference
            val monitoringFallback = zones === fallbackZonesReference || alertsFailed
            val selectedZone = itSelectedZoneOrFirst(remoteState.value.selectedZone, zones)
            remoteState.update {
                it.copy(
                    isLoading = false,
                    animals = animals,
                    zones = zones,
                    alerts = alerts,
                    selectedZone = selectedZone,
                    usingFallbackAnimals = animalFallback,
                    usingFallbackMonitoring = monitoringFallback,
                    animalLoadError = animalLoadError,
                    monitoringLoadError = monitoringLoadError,
                    errorMessageVisible = animalFallback || monitoringFallback
                )
            }
            selectedZone?.targetId?.takeIf { it.isNotBlank() }?.let(::loadTelemetry)
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

    fun selectAnimalImage(imageUri: String?) {
        remoteState.update {
            it.copy(
                animalForm = it.animalForm.copy(selectedImageUri = imageUri),
                animalActionError = null
            )
        }
    }

    fun clearAnimalImage() {
        remoteState.update {
            it.copy(
                animalForm = it.animalForm.copy(selectedImageUri = null),
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
            val photoUrl = state.animalForm.selectedImageUri?.let { imageUri ->
                when (val uploadResult = uploadAnimalImageUseCase(imageUri)) {
                    is BluePatitasResult.Success -> uploadResult.value
                    is BluePatitasResult.Error -> {
                        remoteState.update {
                            it.copy(
                                isSavingAnimal = false,
                                animalActionError = uploadResult.throwable.toAnimalActionError(forImageUpload = true)
                            )
                        }
                        return@launch
                    }
                }
            }
            when (val result = createAnimalUseCase(form.copy(photoUrl = photoUrl))) {
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
                animalHealthUpdatedMessageVisible = false,
                showZoneAssignmentEditor = false,
                selectedPerimeterId = animal.assignedPerimeterId,
                animalZoneAssignmentError = null,
                animalZoneAssignedMessageVisible = false
            )
        }
        viewModelScope.launch {
            when (val result = getAnimalDetailUseCase(animal.id)) {
                is BluePatitasResult.Success -> remoteState.update {
                    it.copy(
                        selectedAnimalDetail = result.value,
                        selectedHealthCondition = AnimalHealthOption.fromApiValue(result.value.healthCondition)?.apiValue,
                        selectedPerimeterId = result.value.assignedPerimeterId,
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

    fun showZoneAssignmentEditor() {
        remoteState.update { state ->
            state.copy(
                showZoneAssignmentEditor = true,
                selectedPerimeterId = state.selectedAnimalDetail?.assignedPerimeterId,
                animalZoneAssignmentError = null,
                animalZoneAssignedMessageVisible = false
            )
        }
    }

    fun hideZoneAssignmentEditor() {
        remoteState.update {
            it.copy(
                showZoneAssignmentEditor = false,
                selectedPerimeterId = it.selectedAnimalDetail?.assignedPerimeterId,
                animalZoneAssignmentError = null,
                isAssigningAnimalZone = false
            )
        }
    }

    fun selectPerimeter(perimeterId: String) {
        remoteState.update {
            it.copy(
                selectedPerimeterId = perimeterId,
                animalZoneAssignmentError = null,
                animalZoneAssignedMessageVisible = false
            )
        }
    }

    fun dismissAnimalZoneAssignedMessage() {
        remoteState.update { it.copy(animalZoneAssignedMessageVisible = false) }
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

    fun assignAnimalToSelectedZone() {
        val state = remoteState.value
        val animal = state.selectedAnimalDetail ?: return
        val perimeterId = state.selectedPerimeterId ?: return
        assignAnimalToPerimeter(animal.id, perimeterId)
    }

    fun removeAnimalZoneAssignment() {
        val animal = remoteState.value.selectedAnimalDetail ?: return
        assignAnimalToPerimeter(animal.id, null)
    }

    private fun assignAnimalToPerimeter(animalId: String, perimeterId: String?) {
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    isAssigningAnimalZone = true,
                    animalZoneAssignmentError = null,
                    animalZoneAssignedMessageVisible = false
                )
            }
            when (val result = assignAnimalToPerimeterUseCase(animalId, perimeterId)) {
                is BluePatitasResult.Success -> {
                    val updatedDetail = when (val detailResult = getAnimalDetailUseCase(animalId)) {
                        is BluePatitasResult.Success -> detailResult.value
                        is BluePatitasResult.Error -> remoteState.value.selectedAnimalDetail?.copy(assignedPerimeterId = perimeterId)
                    }
                    val animalsResult = getAnimalsUseCase()
                    val updatedAnimals = when (animalsResult) {
                        is BluePatitasResult.Success -> animalsResult.value
                        is BluePatitasResult.Error -> remoteState.value.animals.map {
                            if (it.id == animalId) it.copy(assignedPerimeterId = perimeterId) else it
                        }
                    }
                    val zonesResult = getMonitoringZonesUseCase()
                    val updatedZones = when (zonesResult) {
                        is BluePatitasResult.Success -> zonesResult.value
                        is BluePatitasResult.Error -> remoteState.value.zones
                    }
                    remoteState.update {
                        it.copy(
                            animals = updatedAnimals,
                            zones = updatedZones,
                            selectedAnimalDetail = updatedDetail,
                            selectedPerimeterId = updatedDetail?.assignedPerimeterId,
                            showZoneAssignmentEditor = false,
                            isAssigningAnimalZone = false,
                            animalZoneAssignmentError = null,
                            animalZoneAssignedMessageVisible = true,
                            usingFallbackAnimals = if (animalsResult is BluePatitasResult.Success) false else it.usingFallbackAnimals,
                            animalLoadError = if (animalsResult is BluePatitasResult.Success) null else it.animalLoadError,
                            usingFallbackMonitoring = if (zonesResult is BluePatitasResult.Success) false else it.usingFallbackMonitoring,
                            monitoringLoadError = if (zonesResult is BluePatitasResult.Success) null else it.monitoringLoadError
                        )
                    }
                }

                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isAssigningAnimalZone = false,
                        animalZoneAssignmentError = result.throwable.toAnimalActionError()
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
                animalHealthUpdatedMessageVisible = false,
                showZoneAssignmentEditor = false,
                selectedPerimeterId = null,
                isAssigningAnimalZone = false,
                animalZoneAssignmentError = null,
                animalZoneAssignedMessageVisible = false
            )
        }
    }

    fun selectZone(zone: MonitoringZone) {
        remoteState.update { it.copy(selectedZone = zone) }
        zone.targetId.takeIf { it.isNotBlank() }?.let(::loadTelemetry)
    }

    fun refreshMonitoring() {
        if (remoteState.value.isRefreshingMonitoring) return
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    isRefreshingMonitoring = true,
                    monitoringLoadError = null
                )
            }
            var updated = false
            val zones = when (val result = getMonitoringZonesUseCase()) {
                is BluePatitasResult.Success -> {
                    updated = true
                    result.value
                }
                is BluePatitasResult.Error -> {
                    remoteState.update {
                        it.copy(
                            monitoringLoadError = result.throwable.toMonitoringActionError(),
                            usingFallbackMonitoring = it.usingFallbackMonitoring || result.throwable.toMonitoringActionError().allowsDemoFallback()
                        )
                    }
                    null
                }
            }
            val selectedZone = zones?.let { freshZones ->
                itSelectedZoneOrFirst(remoteState.value.selectedZone, freshZones)
            } ?: remoteState.value.selectedZone
            if (zones != null) {
                remoteState.update {
                    it.copy(
                        zones = zones,
                        selectedZone = selectedZone,
                        usingFallbackMonitoring = false,
                        monitoringLoadError = null
                    )
                }
            }
            selectedZone?.targetId?.takeIf { it.isNotBlank() }?.let { targetId ->
                updated = fetchTelemetry(targetId, clearOnStart = false, clearOnError = false) || updated
            }
            remoteState.update {
                it.copy(
                    isRefreshingMonitoring = false,
                    lastMonitoringRefreshMillis = if (updated) System.currentTimeMillis() else it.lastMonitoringRefreshMillis
                )
            }
        }
    }

    fun refreshAlerts() {
        if (remoteState.value.isRefreshingAlerts) return
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    isRefreshingAlerts = true,
                    monitoringLoadError = null
                )
            }
            when (val result = getMonitoringAlertsUseCase()) {
                is BluePatitasResult.Success -> remoteState.update {
                    it.copy(
                        alerts = result.value,
                        isRefreshingAlerts = false,
                        monitoringLoadError = null,
                        lastAlertsRefreshMillis = System.currentTimeMillis()
                    )
                }
                is BluePatitasResult.Error -> remoteState.update {
                    val error = result.throwable.toMonitoringActionError()
                    it.copy(
                        isRefreshingAlerts = false,
                        monitoringLoadError = error,
                        usingFallbackMonitoring = it.usingFallbackMonitoring || error.allowsDemoFallback()
                    )
                }
            }
        }
    }

    fun showCreateZoneForm() {
        remoteState.update {
            it.copy(
                showCreateZoneForm = true,
                zoneForm = MonitoringZoneFormUiState(),
                zoneFormErrors = emptyMap(),
                monitoringActionError = null,
                zoneCreatedMessageVisible = false
            )
        }
    }

    fun hideCreateZoneForm() {
        remoteState.update {
            it.copy(
                showCreateZoneForm = false,
                zoneFormErrors = emptyMap(),
                monitoringActionError = null,
                isSavingZone = false
            )
        }
    }

    fun updateZoneForm(field: String, value: String) {
        remoteState.update { state ->
            val sanitizedValue = when (field) {
                "temperatureC", "humidity", "minTemperatureC", "maxTemperatureC" -> value.sanitizeSignedDecimalInput()
                "animalCount" -> value.filter { it.isDigit() }
                else -> value
            }
            val form = when (field) {
                "targetId" -> state.zoneForm.copy(targetId = sanitizedValue)
                "name" -> state.zoneForm.copy(name = sanitizedValue)
                "temperatureC" -> state.zoneForm.copy(temperatureC = sanitizedValue)
                "humidity" -> state.zoneForm.copy(humidity = sanitizedValue)
                "status" -> state.zoneForm.copy(status = sanitizedValue)
                "animalCount" -> state.zoneForm.copy(animalCount = sanitizedValue)
                "imageUrl" -> state.zoneForm.copy(imageUrl = sanitizedValue)
                "minTemperatureC" -> state.zoneForm.copy(minTemperatureC = sanitizedValue)
                "maxTemperatureC" -> state.zoneForm.copy(maxTemperatureC = sanitizedValue)
                else -> state.zoneForm
            }
            state.copy(
                zoneForm = form,
                zoneFormErrors = state.zoneFormErrors - field,
                monitoringActionError = null
            )
        }
    }

    fun updateZoneCameraEnabled(enabled: Boolean) {
        remoteState.update {
            it.copy(
                zoneForm = it.zoneForm.copy(cameraEnabled = enabled),
                monitoringActionError = null
            )
        }
    }

    fun createMonitoringZone() {
        val state = remoteState.value
        val errors = validateZoneForm(state.zoneForm)
        if (errors.isNotEmpty()) {
            remoteState.update { it.copy(zoneFormErrors = errors) }
            return
        }
        val submitForm = state.zoneForm.withGeneratedDeviceId()
        val form = submitForm.toDomainForm() ?: return
        viewModelScope.launch {
            remoteState.update {
                it.copy(
                    zoneForm = submitForm,
                    isSavingZone = true,
                    monitoringActionError = null
                )
            }
            when (val result = createMonitoringZoneUseCase(form)) {
                is BluePatitasResult.Success -> {
                    remoteState.update {
                        it.copy(
                            isSavingZone = false,
                            showCreateZoneForm = false,
                            zoneForm = MonitoringZoneFormUiState(),
                            zoneFormErrors = emptyMap(),
                            zoneCreatedMessageVisible = true
                        )
                    }
                    refreshMonitoring()
                }

                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isSavingZone = false,
                        monitoringActionError = result.throwable.toMonitoringActionError()
                    )
                }
            }
        }
    }

    fun dismissZoneCreatedMessage() {
        remoteState.update { it.copy(zoneCreatedMessageVisible = false) }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            remoteState.update { it.copy(isResolvingAlertId = alertId, monitoringActionError = null) }
            when (val result = resolveMonitoringAlertUseCase(alertId)) {
                is BluePatitasResult.Success -> {
                    remoteState.update {
                        it.copy(
                            isResolvingAlertId = null,
                            alerts = it.alerts.filterNot { alert -> alert.id == alertId }
                        )
                    }
                    refreshAlerts()
                }

                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isResolvingAlertId = null,
                        monitoringActionError = result.throwable.toMonitoringActionError()
                    )
                }
            }
        }
    }

    fun enableAlertTracking(alert: MonitoringAlert) {
        if (alert.targetId.isBlank()) return
        viewModelScope.launch {
            remoteState.update { it.copy(isEnablingTrackingAlertId = alert.id, monitoringActionError = null) }
            when (val result = enableAlertTrackingUseCase(alert.targetId, alert.id)) {
                is BluePatitasResult.Success -> {
                    remoteState.update { it.copy(isEnablingTrackingAlertId = null) }
                    refreshAlerts()
                }

                is BluePatitasResult.Error -> remoteState.update {
                    it.copy(
                        isEnablingTrackingAlertId = null,
                        monitoringActionError = result.throwable.toMonitoringActionError()
                    )
                }
            }
        }
    }

    private fun loadTelemetry(targetId: String) {
        viewModelScope.launch {
            fetchTelemetry(targetId, clearOnStart = true, clearOnError = true)
        }
    }

    private suspend fun fetchTelemetry(
        targetId: String,
        clearOnStart: Boolean,
        clearOnError: Boolean
    ): Boolean {
        remoteState.update {
            it.copy(
                isLoadingTelemetry = true,
                telemetryError = null,
                selectedZoneTelemetry = if (clearOnStart) emptyList() else it.selectedZoneTelemetry
            )
        }
        return when (val result = getTelemetryUseCase(targetId)) {
            is BluePatitasResult.Success -> {
                remoteState.update {
                    it.copy(
                        selectedZoneTelemetry = result.value.sortedByDescending { record -> record.recordedAt.orEmpty() },
                        isLoadingTelemetry = false,
                        telemetryError = null
                    )
                }
                true
            }

            is BluePatitasResult.Error -> {
                remoteState.update {
                    it.copy(
                        selectedZoneTelemetry = if (clearOnError) emptyList() else it.selectedZoneTelemetry,
                        isLoadingTelemetry = false,
                        telemetryError = result.throwable.toMonitoringActionError()
                    )
                }
                false
            }
        }
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

private fun validateZoneForm(form: MonitoringZoneFormUiState): Map<String, MonitoringFieldError> =
    buildMap {
        if (form.name.isBlank()) put("name", MonitoringFieldError.Required)
        if (form.status.isBlank()) put("status", MonitoringFieldError.Required)
        val temperature = form.temperatureC.toDoubleOrNull()
        if (temperature == null) put("temperatureC", MonitoringFieldError.InvalidNumber)
        val humidity = form.humidity.toDoubleOrNull()
        if (humidity == null) put("humidity", MonitoringFieldError.InvalidNumber)
        val animalCount = form.animalCount.toIntOrNull()
        if (animalCount == null || animalCount < 0) put("animalCount", MonitoringFieldError.InvalidCount)
        val minTemperature = form.minTemperatureC.toDoubleOrNull()
        if (minTemperature == null) put("minTemperatureC", MonitoringFieldError.InvalidNumber)
        val maxTemperature = form.maxTemperatureC.toDoubleOrNull()
        if (maxTemperature == null) put("maxTemperatureC", MonitoringFieldError.InvalidNumber)
        if (minTemperature != null && maxTemperature != null && minTemperature > maxTemperature) {
            put("minTemperatureC", MonitoringFieldError.InvalidRange)
            put("maxTemperatureC", MonitoringFieldError.InvalidRange)
        }
    }

private fun MonitoringZoneFormUiState.withGeneratedDeviceId(): MonitoringZoneFormUiState =
    if (targetId.isBlank()) {
        copy(targetId = UUID.randomUUID().toString())
    } else {
        copy(targetId = targetId.trim())
    }

private fun MonitoringZoneFormUiState.toDomainForm(): CreateMonitoringZoneForm? {
    val temperature = temperatureC.toDoubleOrNull() ?: return null
    val humidityValue = humidity.toDoubleOrNull() ?: return null
    val count = animalCount.toIntOrNull() ?: return null
    val minTemperature = minTemperatureC.toDoubleOrNull() ?: return null
    val maxTemperature = maxTemperatureC.toDoubleOrNull() ?: return null
    return CreateMonitoringZoneForm(
        targetId = targetId.trim().ifBlank { null },
        name = name.trim(),
        temperatureC = temperature,
        humidity = humidityValue,
        status = status.trim().ifBlank { "ACTIVE" },
        animalCount = count,
        cameraEnabled = cameraEnabled,
        imageUrl = imageUrl.trim().ifBlank { null },
        minTemperatureC = minTemperature,
        maxTemperatureC = maxTemperature
    )
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

private fun itSelectedZoneOrFirst(selectedZone: MonitoringZone?, zones: List<MonitoringZone>): MonitoringZone? =
    selectedZone?.let { selected -> zones.firstOrNull { it.id == selected.id } } ?: zones.firstOrNull()

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

private fun String.sanitizeSignedDecimalInput(): String {
    val filtered = filterIndexed { index, char -> char.isDigit() || char == '.' || (char == '-' && index == 0) }
    val firstDotIndex = filtered.indexOf('.')
    if (firstDotIndex == -1) return filtered
    return buildString {
        filtered.forEachIndexed { index, char ->
            if (char != '.' || index == firstDotIndex) append(char)
        }
    }
}

private fun Throwable.toAnimalActionError(): AnimalActionError {
    return toAnimalActionError(forImageUpload = false)
}

private fun Throwable.toMonitoringActionError(): AnimalActionError {
    val reason = (this as? MonitoringRepositoryException)?.reason
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

private fun Throwable.toAnimalActionError(forImageUpload: Boolean): AnimalActionError {
    val reason = (this as? AnimalRepositoryException)?.reason
    return when (reason) {
        AuthFailureReason.BadRequest -> if (forImageUpload) AnimalActionError.InvalidImageUpload else AnimalActionError.BadRequest
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
        AnimalActionError.InvalidImageUpload,
        AnimalActionError.SessionExpired,
        AnimalActionError.EndpointNotFound,
        AnimalActionError.ResponseFormat,
        null -> false
    }
