package com.bluepatitas.mobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.domain.model.AppLanguage
import com.bluepatitas.mobile.domain.model.AppPreferences
import com.bluepatitas.mobile.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferencesRepository {

    override val preferences: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(
            language = AppLanguage.fromTag(preferences[PreferenceKeys.Language] ?: AppLanguage.ENGLISH.tag),
            isDemoModeEnabled = preferences[PreferenceKeys.DemoMode] ?: true
        )
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.Language] = language.tag
        }
    }

    override suspend fun setDemoModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.DemoMode] = enabled
        }
    }
}
