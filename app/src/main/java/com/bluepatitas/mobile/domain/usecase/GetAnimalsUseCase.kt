package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.AnimalRepository
import javax.inject.Inject

class GetAnimalsUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke() = animalRepository.getAnimals()
}
