package com.bluepatitas.mobile.data.repository

import android.content.Context
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.core.notifications.LocalAlertNotifier
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.monitoring.MonitoringZoneDto
import com.bluepatitas.mobile.data.remote.monitoring.PerimeterAlertDto
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Singleton
class RealMonitoringRepository @Inject constructor(
    private val api: BluePatitasApi,
    private val localAlertNotifier: LocalAlertNotifier,
    @param:ApplicationContext private val context: Context
) : MonitoringRepository {
    private val _localAlerts = MutableStateFlow<List<MonitoringAlert>>(emptyList())
    override val localAlerts: StateFlow<List<MonitoringAlert>> = _localAlerts

    override suspend fun getZones(): BluePatitasResult<List<MonitoringZone>> =
        try {
            BluePatitasResult.Success(api.getMonitoringZones().map { it.toDomain() })
        } catch (exception: IOException) {
            BluePatitasResult.Error(exception)
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }

    override suspend fun getAlerts(): BluePatitasResult<List<MonitoringAlert>> =
        try {
            BluePatitasResult.Success(api.getMonitoringAlerts().map { it.toDomain() })
        } catch (exception: IOException) {
            BluePatitasResult.Error(exception)
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }

    override suspend fun simulateBreach(zone: MonitoringZone, shelterName: String?) {
        val alert = MonitoringAlert(
            id = "local-${System.currentTimeMillis()}",
            targetId = zone.targetId,
            zoneName = zone.name,
            message = context.getString(R.string.pet_left_safe_zone),
            isBreachConfirmed = true,
            trackingActive = true,
            latitude = -12.0464,
            longitude = -77.0428,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            isLocal = true
        )
        _localAlerts.update { current -> listOf(alert) + current.filterNot { it.targetId == zone.targetId } }
        localAlertNotifier.notifySafeZoneBreach(shelterName)
    }

    override suspend fun resolveLocalAlert(alertId: String) {
        _localAlerts.update { alerts -> alerts.filterNot { it.id == alertId } }
    }

    override suspend fun clearLocalAlerts() {
        _localAlerts.value = emptyList()
    }
}

private fun MonitoringZoneDto.toDomain(): MonitoringZone =
    MonitoringZone(
        id = id.orEmpty(),
        shelterId = shelterId,
        targetId = targetId ?: id.orEmpty(),
        name = name.orEmpty(),
        temperatureC = temperatureC,
        humidity = humidity,
        minTemperatureC = minTemperatureC,
        maxTemperatureC = maxTemperatureC,
        status = status.orEmpty().ifBlank { "Normal" },
        animalCount = animalCount ?: 0,
        cameraEnabled = cameraEnabled == true,
        imageUrl = imageUrl
    )

private fun PerimeterAlertDto.toDomain(): MonitoringAlert =
    MonitoringAlert(
        id = id.orEmpty(),
        targetId = targetId.orEmpty(),
        zoneName = zoneName.orEmpty().ifBlank { targetId.orEmpty() },
        message = message.orEmpty().ifBlank { "Pet left the safe zone" },
        isBreachConfirmed = isBreachConfirmed == true,
        trackingActive = trackingActive == true,
        latitude = currentCoordinates?.latitude,
        longitude = currentCoordinates?.longitude,
        createdAt = createdAt.orEmpty(),
        isLocal = false
    )
