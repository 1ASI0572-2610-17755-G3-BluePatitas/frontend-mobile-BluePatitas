package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.ShelterRepository
import javax.inject.Inject

class ObserveShelterUseCase @Inject constructor(
    private val shelterRepository: ShelterRepository
) {
    operator fun invoke() = shelterRepository.shelter
}
