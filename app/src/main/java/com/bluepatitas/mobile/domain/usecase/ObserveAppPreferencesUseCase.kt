package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class ObserveAppPreferencesUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository
) {
    operator fun invoke() = appPreferencesRepository.preferences
}
