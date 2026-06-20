package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.ShelterRepository
import javax.inject.Inject

class SyncShelterFromBackendUseCase @Inject constructor(
    private val shelterRepository: ShelterRepository
) {
    suspend operator fun invoke() = shelterRepository.syncShelterFromBackend()
}
