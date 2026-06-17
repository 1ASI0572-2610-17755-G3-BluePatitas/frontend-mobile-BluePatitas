package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.RegisterAdminForm
import com.bluepatitas.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterAdminUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(form: RegisterAdminForm) =
        authRepository.registerAdmin(form)
}
