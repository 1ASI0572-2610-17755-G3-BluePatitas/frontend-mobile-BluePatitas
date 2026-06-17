package com.bluepatitas.mobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.domain.model.ShelterDraft
import com.bluepatitas.mobile.domain.model.ShelterProfile
import com.bluepatitas.mobile.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreShelterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ShelterRepository {

    override val shelter: Flow<ShelterProfile?> = dataStore.data.map { preferences ->
        if (preferences[PreferenceKeys.ShelterCreated] != true) return@map null
        ShelterProfile(
            id = "shelter-wuf",
            name = preferences[PreferenceKeys.ShelterName].orEmpty(),
            taxId = preferences[PreferenceKeys.ShelterTaxId].orEmpty(),
            institutionalEmail = preferences[PreferenceKeys.ShelterEmail].orEmpty(),
            contactPhone = preferences[PreferenceKeys.ShelterPhone].orEmpty(),
            address = preferences[PreferenceKeys.ShelterAddress].orEmpty(),
            reference = preferences[PreferenceKeys.ShelterReference].orEmpty(),
            district = preferences[PreferenceKeys.ShelterDistrict].orEmpty(),
            city = preferences[PreferenceKeys.ShelterCity].orEmpty()
        )
    }

    override suspend fun saveDraft(draft: ShelterDraft) {
        dataStore.edit { preferences ->
            writeShelter(preferences, draft, created = false)
        }
    }

    override suspend fun createShelter(draft: ShelterDraft): ShelterProfile {
        dataStore.edit { preferences ->
            writeShelter(preferences, draft, created = true)
        }
        return ShelterProfile(
            id = "shelter-wuf",
            name = draft.name.trim(),
            taxId = draft.taxId.trim(),
            institutionalEmail = draft.institutionalEmail.trim(),
            contactPhone = draft.contactPhone.trim(),
            address = draft.address.trim(),
            reference = draft.reference.trim(),
            district = draft.district.trim(),
            city = draft.city.trim()
        )
    }

    private fun writeShelter(
        preferences: MutablePreferences,
        draft: ShelterDraft,
        created: Boolean
    ) {
        preferences[PreferenceKeys.ShelterCreated] = created
        preferences[PreferenceKeys.ShelterName] = draft.name.trim()
        preferences[PreferenceKeys.ShelterTaxId] = draft.taxId.trim()
        preferences[PreferenceKeys.ShelterEmail] = draft.institutionalEmail.trim()
        preferences[PreferenceKeys.ShelterPhone] = draft.contactPhone.trim()
        preferences[PreferenceKeys.ShelterAddress] = draft.address.trim()
        preferences[PreferenceKeys.ShelterReference] = draft.reference.trim()
        preferences[PreferenceKeys.ShelterDistrict] = draft.district.trim()
        preferences[PreferenceKeys.ShelterCity] = draft.city.trim()
    }
}
