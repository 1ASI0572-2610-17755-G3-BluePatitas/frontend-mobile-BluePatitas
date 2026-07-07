package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.FeedingRepository
import javax.inject.Inject

class GetFeedingPlansByAnimalUseCase @Inject constructor(
    private val feedingRepository: FeedingRepository
) {
    suspend operator fun invoke(animalId: String) = feedingRepository.getPlansByAnimal(animalId)
}
