package com.bluepatitas.mobile.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BearerAuthInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.data.map { it[PreferenceKeys.Token] }.first()
        }
        val hasToken = !token.isNullOrBlank()
        val request = if (!hasToken) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        val response = chain.proceed(request)
        if (hasToken && response.code == 401) {
            runBlocking { clearStoredSession() }
        }
        return response
    }

    private suspend fun clearStoredSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.UserId)
            preferences.remove(PreferenceKeys.FirstName)
            preferences.remove(PreferenceKeys.LastName)
            preferences.remove(PreferenceKeys.DisplayName)
            preferences.remove(PreferenceKeys.Email)
            preferences.remove(PreferenceKeys.Token)
            preferences.remove(PreferenceKeys.Role)
            preferences.remove(PreferenceKeys.ShelterId)
            preferences.remove(PreferenceKeys.ShelterSessionName)
            preferences.remove(PreferenceKeys.OnboardingCompleted)
        }
    }
}
