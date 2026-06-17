package com.bluepatitas.mobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.data.mock.DemoAccounts
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    override val session: Flow<AppSession?> = dataStore.data.map { preferences ->
        val userId = preferences[PreferenceKeys.UserId] ?: return@map null
        val displayName = preferences[PreferenceKeys.DisplayName] ?: return@map null
        val email = preferences[PreferenceKeys.Email] ?: return@map null
        val role = preferences[PreferenceKeys.Role]?.let(UserRole::valueOf) ?: return@map null

        AppSession(
            userId = userId,
            displayName = displayName,
            email = email,
            role = role,
            shelterId = preferences[PreferenceKeys.ShelterId]
        )
    }

    override suspend fun startSession(session: AppSession) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.UserId] = session.userId
            preferences[PreferenceKeys.DisplayName] = session.displayName
            preferences[PreferenceKeys.Email] = session.email
            preferences[PreferenceKeys.Role] = session.role.name
            session.shelterId?.let { preferences[PreferenceKeys.ShelterId] = it }
                ?: preferences.remove(PreferenceKeys.ShelterId)
            preferences[PreferenceKeys.DemoMode] = true
        }
    }

    override suspend fun startDemoSession(role: UserRole) {
        val session = DemoAccounts.sessionFor(role)
        startSession(session)
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.UserId)
            preferences.remove(PreferenceKeys.DisplayName)
            preferences.remove(PreferenceKeys.Email)
            preferences.remove(PreferenceKeys.Role)
            preferences.remove(PreferenceKeys.ShelterId)
        }
    }
}
