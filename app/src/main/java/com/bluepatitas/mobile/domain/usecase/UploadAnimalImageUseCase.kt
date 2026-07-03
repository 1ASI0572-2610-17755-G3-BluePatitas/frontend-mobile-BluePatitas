package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.AnimalRepository
import javax.inject.Inject

class UploadAnimalImageUseCase @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(imageUri: String) = animalRepository.uploadAnimalImage(imageUri)
}
