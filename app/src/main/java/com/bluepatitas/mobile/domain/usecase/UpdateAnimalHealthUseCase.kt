package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.AnimalRepository
import javax.inject.Inject

class UpdateAnimalHealthUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(id: String, healthCondition: String) =
        animalRepository.updateHealthCondition(id, healthCondition)
}
