package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.VeterinaryRepository
import javax.inject.Inject

class GetVeterinaryDashboardUseCase @Inject constructor(
    private val veterinaryRepository: VeterinaryRepository
) {
    suspend operator fun invoke() = veterinaryRepository.getDashboard()
}
