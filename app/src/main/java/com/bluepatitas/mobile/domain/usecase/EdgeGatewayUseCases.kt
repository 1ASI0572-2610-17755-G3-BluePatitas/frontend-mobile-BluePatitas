package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.EdgeGatewayRepository
import com.bluepatitas.mobile.domain.repository.EdgeGatewaySettingsRepository
import javax.inject.Inject

class ObserveEdgeGatewayUrlUseCase @Inject constructor(
    private val repository: EdgeGatewaySettingsRepository
) {
    operator fun invoke() = repository.edgeGatewayUrl
}

class SaveEdgeGatewayUrlUseCase @Inject constructor(
    private val repository: EdgeGatewaySettingsRepository
) {
    suspend operator fun invoke(url: String) = repository.saveEdgeGatewayUrl(url)
}

class ResetEdgeGatewayUrlUseCase @Inject constructor(
    private val repository: EdgeGatewaySettingsRepository
) {
    suspend operator fun invoke() = repository.resetEdgeGatewayUrl()
}

class GetEdgeGatewaySnapshotUseCase @Inject constructor(
    private val repository: EdgeGatewayRepository
) {
    suspend operator fun invoke(baseUrl: String) = repository.getSnapshot(baseUrl)
}

class ForceDispenserFeedUseCase @Inject constructor(
    private val repository: EdgeGatewayRepository
) {
    suspend operator fun invoke(baseUrl: String) = repository.forceFeed(baseUrl)
}

class ConfigureDispenserScheduleUseCase @Inject constructor(
    private val repository: EdgeGatewayRepository
) {
    suspend operator fun invoke(baseUrl: String, active: Boolean, interval: String) =
        repository.configureSchedule(baseUrl, active, interval)
}
