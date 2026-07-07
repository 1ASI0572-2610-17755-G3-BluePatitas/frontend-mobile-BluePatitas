package com.bluepatitas.mobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.domain.model.DEFAULT_EDGE_GATEWAY_URL
import com.bluepatitas.mobile.domain.repository.EdgeGatewaySettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreEdgeGatewaySettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : EdgeGatewaySettingsRepository {
    override val edgeGatewayUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.EdgeGatewayUrl]
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_EDGE_GATEWAY_URL
    }

    override suspend fun saveEdgeGatewayUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.EdgeGatewayUrl] = url.trim().trimEnd('/')
        }
    }

    override suspend fun resetEdgeGatewayUrl() {
        dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.EdgeGatewayUrl)
        }
    }
}
