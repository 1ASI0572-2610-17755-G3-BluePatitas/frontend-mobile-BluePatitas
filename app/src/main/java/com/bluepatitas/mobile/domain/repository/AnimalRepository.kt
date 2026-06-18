package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.AnimalSummary

interface AnimalRepository {
    suspend fun getAnimals(): BluePatitasResult<List<AnimalSummary>>
}
