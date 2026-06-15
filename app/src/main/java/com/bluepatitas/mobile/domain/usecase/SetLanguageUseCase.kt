package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.AppLanguage
import com.bluepatitas.mobile.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository
) {
    suspend operator fun invoke(language: AppLanguage) {
        appPreferencesRepository.setLanguage(language)
    }
}
