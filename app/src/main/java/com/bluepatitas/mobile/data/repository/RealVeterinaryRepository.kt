package com.bluepatitas.mobile.data.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.veterinary.RecentObservationDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryAnimalDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryDashboardDto
import com.bluepatitas.mobile.domain.model.RecentObservation
import com.bluepatitas.mobile.domain.model.VeterinaryAnimal
import com.bluepatitas.mobile.domain.model.VeterinaryDashboard
import com.bluepatitas.mobile.domain.repository.VeterinaryRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealVeterinaryRepository @Inject constructor(
    private val api: BluePatitasApi
) : VeterinaryRepository {

    override suspend fun getDashboard(): BluePatitasResult<VeterinaryDashboard> =
        try {
            BluePatitasResult.Success(api.getVeterinaryDashboard().toDomain())
        } catch (exception: IOException) {
            BluePatitasResult.Error(exception)
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }

    override suspend fun getAnimals(): BluePatitasResult<List<VeterinaryAnimal>> =
        try {
            BluePatitasResult.Success(api.getVeterinaryAnimals().map { it.toDomain() })
        } catch (exception: IOException) {
            BluePatitasResult.Error(exception)
        } catch (exception: RuntimeException) {
            BluePatitasResult.Error(exception)
        }
}

private fun VeterinaryDashboardDto.toDomain(): VeterinaryDashboard =
    VeterinaryDashboard(
        veterinarianName = veterinarianName.orEmpty(),
        shelterName = shelterName.orEmpty(),
        animalsUnderCare = animalsUnderCare ?: 0,
        pendingObservations = pendingObservations ?: 0,
        activeAlerts = activeAlerts ?: 0,
        recentObservationsCount = recentObservationsCount ?: recentObservations.orEmpty().size,
        recentObservations = recentObservations.orEmpty().map { it.toDomain() }
    )

private fun RecentObservationDto.toDomain(): RecentObservation =
    RecentObservation(
        id = id.orEmpty(),
        animalName = animalName.orEmpty(),
        description = observation ?: note.orEmpty(),
        createdAt = createdAt.orEmpty()
    )

private fun VeterinaryAnimalDto.toDomain(): VeterinaryAnimal =
    VeterinaryAnimal(
        id = id.orEmpty(),
        photoUrl = photoUrl,
        name = name.orEmpty(),
        species = species.orEmpty(),
        breed = breed,
        healthCondition = healthCondition,
        weightKg = weightKg
    )
