package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import javax.inject.Inject

class ResolveMonitoringAlertUseCase @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) {
    suspend operator fun invoke(alertId: String) = monitoringRepository.resolveAlert(alertId)
}
