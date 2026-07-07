package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.FeedingPlan
import com.bluepatitas.mobile.domain.model.FeedingPlanForm

interface FeedingRepository {
    suspend fun getPlans(): BluePatitasResult<List<FeedingPlan>>
    suspend fun getPlansByAnimal(animalId: String): BluePatitasResult<List<FeedingPlan>>
    suspend fun createPlan(form: FeedingPlanForm): BluePatitasResult<Unit>
    suspend fun updatePlan(id: String, form: FeedingPlanForm): BluePatitasResult<Unit>
    suspend fun activatePlan(id: String): BluePatitasResult<Unit>
    suspend fun deactivatePlan(id: String): BluePatitasResult<Unit>
}
