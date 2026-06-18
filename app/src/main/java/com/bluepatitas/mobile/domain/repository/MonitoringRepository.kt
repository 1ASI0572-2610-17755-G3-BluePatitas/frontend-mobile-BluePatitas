package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.MonitoringAlert
import com.bluepatitas.mobile.domain.model.MonitoringZone
import kotlinx.coroutines.flow.StateFlow

interface MonitoringRepository {
    val localAlerts: StateFlow<List<MonitoringAlert>>
    suspend fun getZones(): BluePatitasResult<List<MonitoringZone>>
    suspend fun getAlerts(): BluePatitasResult<List<MonitoringAlert>>
    suspend fun simulateBreach(zone: MonitoringZone, shelterName: String?)
    suspend fun resolveLocalAlert(alertId: String)
    suspend fun clearLocalAlerts()
}
