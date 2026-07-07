package com.bluepatitas.mobile.domain.repository

import kotlinx.coroutines.flow.Flow

interface EdgeGatewaySettingsRepository {
    val edgeGatewayUrl: Flow<String>
    suspend fun saveEdgeGatewayUrl(url: String)
    suspend fun resetEdgeGatewayUrl()
}
