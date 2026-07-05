package com.bluepatitas.mobile.data.repository

import android.content.Context
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.core.notifications.LocalAlertNotifier
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.monitoring.EnableTrackingRequestDto
import com.bluepatitas.mobile.data.remote.monitoring.MonitoringZoneDto
import com.bluepatitas.mobile.data.remote.monitoring.PerimeterAlertDto
import com.bluepatitas.mobile.data.remote.monitoring.TelemetryRecordDto
import com.bluepatitas.mobile.data.remote.monitoring.ZoneRequestDto
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.CreateMonitoringZoneForm
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.TelemetryRecord
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class RealMonitoringRepository @Inject constructor(
    private val api: BluePatitasApi,
    private val localAlertNotifier: LocalAlertNotifier,
    @param:ApplicationContext private val context: Context
) : MonitoringRepository {
    private val _localAlerts = MutableStateFlow<List<MonitoringAlert>>(emptyList())
    override val localAlerts: StateFlow<List<MonitoringAlert>> = _localAlerts

    override suspend fun getZones(): BluePatitasResult<List<MonitoringZone>> =
        runMonitoringRequest("GET /api/monitoring/zones") {
            BluePatitasResult.Success(api.getMonitoringZones().map { it.toDomain() })
        }

    override suspend fun createZone(form: CreateMonitoringZoneForm): BluePatitasResult<Unit> =
        runMonitoringRequest("POST /api/monitoring/zones") {
            val response = api.createMonitoringZone(form.toRequest())
            if (response.isSuccessful) {
                BluePatitasResult.Success(Unit)
            } else {
                throw response.toMonitoringException()
            }
        }

    override suspend fun getAlerts(): BluePatitasResult<List<MonitoringAlert>> =
        runMonitoringRequest("GET /api/monitoring/alerts") {
            BluePatitasResult.Success(api.getMonitoringAlerts().map { it.toDomain() })
        }

    override suspend fun resolveAlert(alertId: String): BluePatitasResult<Unit> =
        if (alertId.startsWith("local-")) {
            resolveLocalAlert(alertId)
            BluePatitasResult.Success(Unit)
        } else {
            runMonitoringRequest("PUT /api/monitoring/alerts/$alertId/resolve") {
                val response = api.resolveMonitoringAlert(alertId)
                if (response.isSuccessful) {
                    BluePatitasResult.Success(Unit)
                } else {
                    throw response.toMonitoringException()
                }
            }
        }

    override suspend fun enableTracking(targetId: String, alertId: String): BluePatitasResult<Unit> =
        runMonitoringRequest("POST /api/monitoring/alerts/$targetId/tracking") {
            val response = api.enableAlertTracking(
                targetId = targetId,
                request = EnableTrackingRequestDto(alertId = alertId)
            )
            if (response.isSuccessful) {
                BluePatitasResult.Success(Unit)
            } else {
                throw response.toMonitoringException()
            }
        }

    override suspend fun getTelemetry(targetId: String): BluePatitasResult<List<TelemetryRecord>> =
        runMonitoringRequest("GET /api/monitoring/telemetry/$targetId") {
            BluePatitasResult.Success(api.getTelemetry(targetId).map { it.toDomain() })
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

    private inline fun <T> runMonitoringRequest(
        operation: String,
        block: () -> BluePatitasResult<T>
    ): BluePatitasResult<T> =
        try {
            block()
        } catch (exception: HttpException) {
            BluePatitasResult.Error(exception.toMonitoringException())
        } catch (exception: JsonParseException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Serialization, "$operation response does not match DTO.", exception)
            )
        } catch (exception: MalformedJsonException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Serialization, "$operation response has malformed JSON.", exception)
            )
        } catch (exception: SocketTimeoutException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Timeout, "$operation timed out.", exception)
            )
        } catch (exception: UnknownHostException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Network, "$operation could not resolve host.", exception)
            )
        } catch (exception: SSLException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Network, "$operation failed due to SSL.", exception)
            )
        } catch (exception: IOException) {
            BluePatitasResult.Error(
                MonitoringRepositoryException(AuthFailureReason.Network, "$operation failed due to IO.", exception)
            )
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }
}

class MonitoringRepositoryException(
    val reason: AuthFailureReason,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

private fun Response<*>.toMonitoringException(): MonitoringRepositoryException {
    val code = code()
    val errorText = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
    return MonitoringRepositoryException(
        reason = code.toFailureReason(),
        message = "Monitoring request failed: HTTP $code. $errorText".trim()
    )
}

private fun HttpException.toMonitoringException(): MonitoringRepositoryException =
    MonitoringRepositoryException(
        reason = code().toFailureReason(),
        message = "Monitoring request failed: HTTP ${code()}.",
        cause = this
    )

private fun Int.toFailureReason(): AuthFailureReason =
    when (this) {
        400 -> AuthFailureReason.BadRequest
        401, 403 -> AuthFailureReason.SessionExpired
        404 -> AuthFailureReason.EndpointNotFound
        409 -> AuthFailureReason.Conflict
        500 -> AuthFailureReason.ServerError
        else -> AuthFailureReason.Unknown
    }

private fun CreateMonitoringZoneForm.toRequest(): ZoneRequestDto =
    ZoneRequestDto(
        targetId = targetId?.takeIf { it.isNotBlank() },
        name = name.trim(),
        temperatureC = temperatureC,
        humidity = humidity,
        status = status.trim().ifBlank { "ACTIVE" },
        animalCount = animalCount,
        cameraEnabled = cameraEnabled,
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        minTemperatureC = minTemperatureC,
        maxTemperatureC = maxTemperatureC
    )

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
        zoneName = zoneName.orEmpty().ifBlank { targetId.orEmpty().take(8) },
        message = message.orEmpty().ifBlank { "Perimeter alert" },
        isBreachConfirmed = isBreachConfirmed == true,
        trackingActive = trackingActive == true,
        latitude = currentCoordinates?.latitude,
        longitude = currentCoordinates?.longitude,
        createdAt = createdAt.orEmpty(),
        isLocal = false
    )

private fun TelemetryRecordDto.toDomain(): TelemetryRecord =
    TelemetryRecord(
        id = id.orEmpty(),
        targetId = targetId.orEmpty(),
        ambientTemperature = ambientTemperature,
        ambientHumidity = ambientHumidity,
        visualData = visualData,
        recordedAt = recordedAt
    )
