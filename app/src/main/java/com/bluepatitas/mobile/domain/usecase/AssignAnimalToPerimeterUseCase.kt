package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.AnimalRepository
import javax.inject.Inject

class AssignAnimalToPerimeterUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(animalId: String, perimeterId: String?) =
        animalRepository.assignAnimalToPerimeter(animalId, perimeterId)
}
