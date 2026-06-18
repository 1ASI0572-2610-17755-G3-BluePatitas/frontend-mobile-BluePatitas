package com.bluepatitas.mobile.data.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.animal.AnimalDto
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.repository.AnimalRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAnimalRepository @Inject constructor(
    private val api: BluePatitasApi
) : AnimalRepository {
    override suspend fun getAnimals(): BluePatitasResult<List<AnimalSummary>> =
        try {
            BluePatitasResult.Success(api.getAnimals().map { it.toDomain() })
        } catch (exception: IOException) {
            BluePatitasResult.Error(exception)
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }
}

private fun AnimalDto.toDomain(): AnimalSummary =
    AnimalSummary(
        id = id.orEmpty(),
        name = name.orEmpty(),
        photoUrl = photoUrl,
        species = species ?: speciesDetails?.species.orEmpty(),
        breed = breed ?: speciesDetails?.breed,
        estimatedAgeMonths = speciesDetails?.estimatedAgeMonths,
        healthCondition = healthCondition,
        weightKg = weightKg,
        zoneName = assignedPerimeterId
    )
