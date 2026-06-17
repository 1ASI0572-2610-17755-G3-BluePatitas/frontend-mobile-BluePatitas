package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class AcceptInvitationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(form: InvitationForm) =
        authRepository.acceptInvitation(form)
}
