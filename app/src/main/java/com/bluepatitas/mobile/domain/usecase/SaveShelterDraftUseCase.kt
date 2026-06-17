package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.ShelterDraft
import com.bluepatitas.mobile.domain.repository.ShelterRepository
import javax.inject.Inject

class SaveShelterDraftUseCase @Inject constructor(
    private val shelterRepository: ShelterRepository
) {
    suspend operator fun invoke(draft: ShelterDraft) {
        shelterRepository.saveDraft(draft)
    }
}
