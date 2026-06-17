package com.bluepatitas.mobile.domain.model

sealed interface AuthResult {
    data class Success(val session: AppSession) : AuthResult
    data object InvalidCredentials : AuthResult
    data object InvalidInvitationCode : AuthResult
}
