package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.CreateMonitoringZoneForm
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import com.bluepatitas.mobile.domain.model.TelemetryRecord
import kotlinx.coroutines.flow.StateFlow

interface MonitoringRepository {
    val localAlerts: StateFlow<List<MonitoringAlert>>
    suspend fun getZones(): BluePatitasResult<List<MonitoringZone>>
    suspend fun createZone(form: CreateMonitoringZoneForm): BluePatitasResult<Unit>
    suspend fun getAlerts(): BluePatitasResult<List<MonitoringAlert>>
    suspend fun resolveAlert(alertId: String): BluePatitasResult<Unit>
    suspend fun enableTracking(targetId: String, alertId: String): BluePatitasResult<Unit>
    suspend fun getTelemetry(targetId: String): BluePatitasResult<List<TelemetryRecord>>
    suspend fun simulateBreach(zone: MonitoringZone, shelterName: String?)
    suspend fun resolveLocalAlert(alertId: String)
    suspend fun clearLocalAlerts()
}
