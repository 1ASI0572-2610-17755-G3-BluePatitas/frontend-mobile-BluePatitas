package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.RegisterAnimalForm
import com.bluepatitas.mobile.domain.repository.AnimalRepository
import javax.inject.Inject

class CreateAnimalUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(form: RegisterAnimalForm) = animalRepository.registerAnimal(form)
}
