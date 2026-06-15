package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.domain.model.AppLanguage
import com.bluepatitas.mobile.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val preferences: Flow<AppPreferences>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDemoModeEnabled(enabled: Boolean)
}
