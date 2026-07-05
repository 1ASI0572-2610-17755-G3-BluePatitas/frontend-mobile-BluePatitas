package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.RegisterAnimalForm

interface AnimalRepository {
    suspend fun getAnimals(): BluePatitasResult<List<AnimalSummary>>
    suspend fun getAnimal(id: String): BluePatitasResult<AnimalSummary>
    suspend fun registerAnimal(form: RegisterAnimalForm): BluePatitasResult<Unit>
    suspend fun updateHealthCondition(id: String, healthCondition: String): BluePatitasResult<Unit>
    suspend fun assignAnimalToPerimeter(id: String, perimeterId: String?): BluePatitasResult<Unit>
    suspend fun uploadAnimalImage(imageUri: String): BluePatitasResult<String>
}
