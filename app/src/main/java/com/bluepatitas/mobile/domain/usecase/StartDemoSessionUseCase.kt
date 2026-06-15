package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.repository.SessionRepository
import javax.inject.Inject

class StartDemoSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(role: UserRole) {
        sessionRepository.startDemoSession(role)
    }
}
