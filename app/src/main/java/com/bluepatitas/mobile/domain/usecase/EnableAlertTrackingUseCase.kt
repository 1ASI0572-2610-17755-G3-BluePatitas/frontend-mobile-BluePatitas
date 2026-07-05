package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import javax.inject.Inject

class EnableAlertTrackingUseCase @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) {
    suspend operator fun invoke(targetId: String, alertId: String) =
        monitoringRepository.enableTracking(targetId = targetId, alertId = alertId)
}
