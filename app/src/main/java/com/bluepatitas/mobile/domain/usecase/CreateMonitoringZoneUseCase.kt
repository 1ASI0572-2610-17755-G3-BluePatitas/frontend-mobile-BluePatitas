package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.CreateMonitoringZoneForm
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import javax.inject.Inject

class CreateMonitoringZoneUseCase @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) {
    suspend operator fun invoke(form: CreateMonitoringZoneForm) = monitoringRepository.createZone(form)
}
