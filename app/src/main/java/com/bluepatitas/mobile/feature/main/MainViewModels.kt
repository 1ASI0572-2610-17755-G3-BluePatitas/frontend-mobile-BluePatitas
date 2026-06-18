package com.bluepatitas.mobile.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import com.bluepatitas.mobile.domain.usecase.GetAnimalsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringAlertsUseCase
import com.bluepatitas.mobile.domain.usecase.GetMonitoringZonesUseCase
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
    val errorMessageVisible: Boolean = false,
    val geofenceStatus: GeofenceStatus = GeofenceStatus.InsideSafeZone,
    val cameraPermissionDenied: Boolean = false,
    val notificationPermissionDenied: Boolean = false
)

enum class GeofenceStatus {
    InsideSafeZone,
    OutsideSafeZone
}

@HiltViewModel
class MainDataViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
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
            remoteState.update { it.copy(isLoading = true, errorMessageVisible = false) }
            val animals = when (val result = getAnimalsUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> fallbackAnimals()
            }
            val zones = when (val result = getMonitoringZonesUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> fallbackZones()
            }
            val alerts = when (val result = getMonitoringAlertsUseCase()) {
                is BluePatitasResult.Success -> result.value
                is BluePatitasResult.Error -> emptyList()
            }
            val animalFallback = animals === fallbackAnimalsReference
            val monitoringFallback = zones === fallbackZonesReference
            remoteState.update {
                it.copy(
                    isLoading = false,
                    animals = animals,
                    zones = zones,
                    alerts = alerts,
                    selectedZone = it.selectedZone ?: zones.firstOrNull(),
                    usingFallbackAnimals = animalFallback,
                    usingFallbackMonitoring = monitoringFallback,
                    errorMessageVisible = animalFallback || monitoringFallback
                )
            }
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
