package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.SessionRepository
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke() = sessionRepository.session
}
