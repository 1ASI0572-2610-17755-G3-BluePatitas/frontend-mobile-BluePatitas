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
                    shelterId = "shelter-wuf"
                )

            normalizedEmail == "vet@bluepatitas.com" && credentials.password == "vet123" ->
                AppSession(
                    userId = "demo-vet",
                    displayName = "Elena Ramos",
                    email = "vet@bluepatitas.com",
                    role = UserRole.VETERINARIAN,
                    shelterId = "shelter-wuf"
                )

            else -> null
        } ?: return AuthResult.InvalidCredentials

        sessionRepository.startSession(session)
        return AuthResult.Success(session)
    }

    override suspend fun registerAdmin(form: RegisterAdminForm): AuthResult {
        val session = AppSession(
            userId = "registered-admin",
            displayName = "${form.firstName.trim()} ${form.lastName.trim()}".trim(),
            email = form.email.trim(),
            role = UserRole.SHELTER_ADMIN,
            shelterId = "shelter-wuf"
        )
        sessionRepository.startSession(session)
        return AuthResult.Success(session)
    }

    override suspend fun acceptInvitation(form: InvitationForm): AuthResult {
        if (form.code.trim().uppercase() != "VET-BP-2026") {
            return AuthResult.InvalidInvitationCode
        }

        val session = AppSession(
            userId = "invited-vet",
            displayName = "Elena Ramos",
            email = form.email.trim().ifBlank { "vet@bluepatitas.com" },
            role = UserRole.VETERINARIAN,
            shelterId = "shelter-wuf"
        )
        sessionRepository.startSession(session)
        return AuthResult.Success(session)
    }
}
