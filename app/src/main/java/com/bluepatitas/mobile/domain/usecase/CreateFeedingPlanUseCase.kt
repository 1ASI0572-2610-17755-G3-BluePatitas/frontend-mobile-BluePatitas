package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.FeedingPlanForm
import com.bluepatitas.mobile.domain.repository.FeedingRepository
import javax.inject.Inject

class CreateFeedingPlanUseCase @Inject constructor(
    private val feedingRepository: FeedingRepository
) {
    suspend operator fun invoke(form: FeedingPlanForm) = feedingRepository.createPlan(form)
}
