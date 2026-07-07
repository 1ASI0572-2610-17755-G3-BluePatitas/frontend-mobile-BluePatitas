package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.FeedingPlanForm
import com.bluepatitas.mobile.domain.repository.FeedingRepository
import javax.inject.Inject

class UpdateFeedingPlanUseCase @Inject constructor(
    private val feedingRepository: FeedingRepository
) {
    suspend operator fun invoke(id: String, form: FeedingPlanForm) = feedingRepository.updatePlan(id, form)
}
