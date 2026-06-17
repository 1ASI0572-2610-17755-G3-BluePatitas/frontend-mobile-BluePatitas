package com.bluepatitas.mobile.domain.repository

import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.model.RegisterAdminForm

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): AuthResult
    suspend fun registerAdmin(form: RegisterAdminForm): AuthResult
    suspend fun acceptInvitation(form: InvitationForm): AuthResult
}
