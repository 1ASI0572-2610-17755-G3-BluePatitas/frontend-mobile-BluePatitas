package com.bluepatitas.mobile.data.repository

import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.model.RegisterAdminForm
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.repository.AuthRepository
import com.bluepatitas.mobile.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor(
    private val sessionRepository: SessionRepository
) : AuthRepository {

    override suspend fun login(credentials: LoginCredentials): AuthResult {
        val normalizedEmail = credentials.email.trim().lowercase()
        val session = when {
            normalizedEmail == "admin@bluepatitas.com" && credentials.password == "admin123" ->
                AppSession(
                    userId = "demo-admin",
                    displayName = "Marina Herrera",
                    email = "admin@bluepatitas.com",
                    role = UserRole.SHELTER_ADMIN,
                    shelterId = "shelter-wuf",
                    shelterName = "WUF Shelter",
                    onboardingCompleted = true
                )

            normalizedEmail == "vet@bluepatitas.com" && credentials.password == "vet123" ->
                AppSession(
                    userId = "demo-vet",
                    displayName = "Elena Ramos",
                    email = "vet@bluepatitas.com",
                    role = UserRole.VETERINARIAN,
                    shelterId = "shelter-wuf",
                    shelterName = "WUF Shelter",
                    onboardingCompleted = true
                )

            else -> null
        } ?: return AuthResult.InvalidCredentials

        sessionRepository.startSession(session)
        return AuthResult.Success(session)
    }

    override suspend fun registerAdmin(form: RegisterAdminForm): AuthResult =
        AuthResult.RegistrationSuccess

    override suspend fun acceptInvitation(form: InvitationForm): AuthResult {
        if (form.code.trim().uppercase() != "VET-BP-2026") {
            return AuthResult.InvalidInvitationCode
        }

        val session = AppSession(
            userId = "invited-vet",
            displayName = "Elena Ramos",
            email = form.email.trim().ifBlank { "vet@bluepatitas.com" },
            role = UserRole.VETERINARIAN,
            shelterId = "shelter-wuf",
            shelterName = "WUF Shelter",
            onboardingCompleted = true
        )
        sessionRepository.startSession(session)
        return AuthResult.Success(session)
    }
}
