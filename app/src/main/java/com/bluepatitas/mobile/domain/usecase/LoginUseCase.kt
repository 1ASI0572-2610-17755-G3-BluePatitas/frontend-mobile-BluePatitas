package com.bluepatitas.mobile.domain.usecase

import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credentials: LoginCredentials) =
        authRepository.login(credentials)
}
