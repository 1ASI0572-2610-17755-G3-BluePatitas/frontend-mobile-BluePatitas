package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val session: Flow<AppSession?>
    suspend fun startDemoSession(role: UserRole)
    suspend fun clearSession()
}
