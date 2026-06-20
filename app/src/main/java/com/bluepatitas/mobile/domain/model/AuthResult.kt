package com.bluepatitas.mobile.domain.model

sealed interface AuthResult {
    data class Success(val session: AppSession) : AuthResult
    data object InvalidCredentials : AuthResult
    data object InvalidInvitationCode : AuthResult
    data class ConnectionError(
        val reason: AuthFailureReason = AuthFailureReason.Unknown,
        val message: String? = null
    ) : AuthResult
}

enum class AuthFailureReason {
    EndpointNotFound,
    ServerError,
    Timeout,
    Network,
    Serialization,
    MissingRole,
    Unknown
}
