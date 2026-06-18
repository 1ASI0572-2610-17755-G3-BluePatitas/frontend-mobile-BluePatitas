package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import javax.inject.Inject

class GetMonitoringZonesUseCase @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) {
    suspend operator fun invoke() = monitoringRepository.getZones()
}
