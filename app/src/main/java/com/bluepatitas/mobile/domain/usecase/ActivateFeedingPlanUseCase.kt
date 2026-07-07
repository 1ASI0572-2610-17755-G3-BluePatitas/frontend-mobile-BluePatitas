package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.FeedingRepository
import javax.inject.Inject

class ActivateFeedingPlanUseCase @Inject constructor(
    private val feedingRepository: FeedingRepository
) {
    suspend operator fun invoke(id: String) = feedingRepository.activatePlan(id)
}
