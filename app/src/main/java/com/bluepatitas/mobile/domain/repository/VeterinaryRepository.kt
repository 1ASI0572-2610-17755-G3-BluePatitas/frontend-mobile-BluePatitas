package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.VeterinaryAnimal
import com.bluepatitas.mobile.domain.model.VeterinaryDashboard

interface VeterinaryRepository {
    suspend fun getDashboard(): BluePatitasResult<VeterinaryDashboard>
    suspend fun getAnimals(): BluePatitasResult<List<VeterinaryAnimal>>
}
