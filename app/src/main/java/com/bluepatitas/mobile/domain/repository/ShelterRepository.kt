package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.domain.model.ShelterDraft
import com.bluepatitas.mobile.domain.model.ShelterProfile
import kotlinx.coroutines.flow.Flow

interface ShelterRepository {
    val shelter: Flow<ShelterProfile?>
    suspend fun saveDraft(draft: ShelterDraft)
    suspend fun createShelter(draft: ShelterDraft): ShelterProfile
    suspend fun syncShelterFromBackend(): ShelterProfile?
}
