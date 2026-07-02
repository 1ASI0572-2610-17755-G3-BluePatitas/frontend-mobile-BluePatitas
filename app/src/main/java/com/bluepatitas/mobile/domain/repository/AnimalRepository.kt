package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.RegisterAnimalForm

interface AnimalRepository {
    suspend fun getAnimals(): BluePatitasResult<List<AnimalSummary>>
    suspend fun getAnimal(id: String): BluePatitasResult<AnimalSummary>
    suspend fun registerAnimal(form: RegisterAnimalForm): BluePatitasResult<Unit>
}
