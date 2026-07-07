package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.EdgeGatewaySnapshot

interface EdgeGatewayRepository {
    suspend fun getSnapshot(baseUrl: String): BluePatitasResult<EdgeGatewaySnapshot>
    suspend fun forceFeed(baseUrl: String): BluePatitasResult<Unit>
    suspend fun configureSchedule(
        baseUrl: String,
        active: Boolean,
        interval: String
    ): BluePatitasResult<Unit>
}
