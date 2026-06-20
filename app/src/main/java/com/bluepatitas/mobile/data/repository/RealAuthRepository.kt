package com.bluepatitas.mobile.data.repository

import android.util.Log
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.auth.AuthenticatedUserDto
import com.bluepatitas.mobile.data.remote.auth.SignInRequest
import com.bluepatitas.mobile.data.remote.auth.SignUpRequestDto
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.model.RegisterAdminForm
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.repository.AuthRepository
import com.bluepatitas.mobile.domain.repository.SessionRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import retrofit2.HttpException

@Singleton
class RealAuthRepository @Inject constructor(
    private val api: BluePatitasApi,
    private val sessionRepository: SessionRepository,
    private val fakeAuthRepository: FakeAuthRepository
) : AuthRepository {

    override suspend fun login(credentials: LoginCredentials): AuthResult =
        try {
            val user = api.signIn(
                SignInRequest(
                    email = credentials.email.trim(),
                    password = credentials.password
                )
            )

            val loginRoles = buildList {
                user.role?.takeIf { it.isNotBlank() }?.let(::add)
                addAll(user.roles.orEmpty().filter { it.isNotBlank() })
            }

            val roles = loginRoles.ifEmpty {
                try {
                    val userProfile = api.getUserById(user.id, "Bearer ${user.token}")
                    userProfile.roles.orEmpty()
                } catch (exception: Exception) {
                    Log.e("BluePatitasAuth", "Failed to fetch user roles for ID ${user.id}", exception)
                    emptyList()
                }
            }

            val session = user.copy(
                roles = roles,
                role = roles.firstOrNull()
            ).toSession()

            sessionRepository.startSession(session)
            AuthResult.Success(session)
        } catch (exception: HttpException) {
            val code = exception.code()
            val reason = when (code) {
                401, 403 -> "401/403 = invalid credentials or user without permissions."
                404 -> "404 = authentication endpoint not found."
                500 -> "500 = backend internal error."
                else -> "HTTP $code = backend error."
            }
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            if (code == 401 || code == 403) {
                AuthResult.InvalidCredentials
            } else {
                AuthResult.ConnectionError(
                    reason = when (code) {
                        404 -> AuthFailureReason.EndpointNotFound
                        500 -> AuthFailureReason.ServerError
                        else -> AuthFailureReason.Unknown
                    },
                    message = reason
                )
            }
        } catch (exception: MissingRoleException) {
            val reason = "Missing or unsupported role in authentication response."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.MissingRole, reason)
        } catch (exception: InvalidAuthPayloadException) {
            val reason = exception.message ?: "Invalid authentication payload."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Serialization, reason)
        } catch (exception: JsonParseException) {
            val reason = "JSON parse error = response does not match DTO."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Serialization, reason)
        } catch (exception: MalformedJsonException) {
            val reason = "Malformed JSON response from backend."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Serialization, reason)
        } catch (exception: SocketTimeoutException) {
            val reason = "Timeout = Render may be waking up or backend took too long."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Timeout, reason)
        } catch (exception: UnknownHostException) {
            val reason = "UnknownHost = no internet, DNS failure, or backend unreachable."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: SSLException) {
            val reason = "SSL = certificate or secure connection problem."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: IOException) {
            val reason = "IO = internet problem or backend unreachable."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: IllegalArgumentException) {
            val reason = "IllegalArgumentException: ${exception.message}"
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(AuthFailureReason.Unknown, reason)
        }

    override suspend fun registerAdmin(form: RegisterAdminForm): AuthResult =
        try {
            val response = api.signUp(
                SignUpRequestDto(
                    firstName = form.firstName.trim(),
                    lastName = form.lastName.trim(),
                    email = form.email.trim(),
                    phoneNumber = form.phone.trim(),
                    password = form.password,
                    role = ShelterAdminRole
                )
            )
            if (response.isSuccessful) {
                AuthResult.RegistrationSuccess
            } else {
                val reason = "Sign-up failed: HTTP ${response.code()}."
                Log.e("BluePatitasAuth", reason)
                AuthResult.ConnectionError(
                    reason = when (response.code()) {
                        400 -> AuthFailureReason.BadRequest
                        409 -> AuthFailureReason.Conflict
                        404 -> AuthFailureReason.EndpointNotFound
                        500 -> AuthFailureReason.ServerError
                        else -> AuthFailureReason.Unknown
                    },
                    message = reason
                )
            }
        } catch (exception: HttpException) {
            val code = exception.code()
            val reason = "Sign-up failed: HTTP $code."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(
                reason = when (code) {
                    400 -> AuthFailureReason.BadRequest
                    409 -> AuthFailureReason.Conflict
                    404 -> AuthFailureReason.EndpointNotFound
                    500 -> AuthFailureReason.ServerError
                    else -> AuthFailureReason.Unknown
                },
                message = reason
            )
        } catch (exception: JsonParseException) {
            val reason = "Sign-up JSON parse error = response does not match DTO."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Serialization, reason)
        } catch (exception: MalformedJsonException) {
            val reason = "Sign-up malformed JSON response from backend."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Serialization, reason)
        } catch (exception: SocketTimeoutException) {
            val reason = "Sign-up timeout = Render may be waking up or backend took too long."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Timeout, reason)
        } catch (exception: UnknownHostException) {
            val reason = "Sign-up UnknownHost = no internet, DNS failure, or backend unreachable."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: SSLException) {
            val reason = "Sign-up SSL = certificate or secure connection problem."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: IOException) {
            val reason = "Sign-up IO = internet problem or backend unreachable."
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Network, reason)
        } catch (exception: RuntimeException) {
            val reason = "Sign-up unexpected error: ${exception.message}"
            Log.e("BluePatitasAuth", reason, exception)
            AuthResult.ConnectionError(AuthFailureReason.Unknown, reason)
        }

    override suspend fun acceptInvitation(form: InvitationForm): AuthResult =
        fakeAuthRepository.acceptInvitation(form)
}

private fun AuthenticatedUserDto.toSession(): AppSession {
    if (token.isBlank()) {
        throw InvalidAuthPayloadException("Missing token in authentication response")
    }

    val resolvedRole = buildList {
        role?.let(::add)
        addAll(roles.orEmpty())
    }.firstNotNullOfOrNull { it.toUserRoleOrNull() }
        ?: throw MissingRoleException()

    val first = firstName.orEmpty()
    val last = lastName.orEmpty()
    val name = listOf(first, last)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { email }

    return AppSession(
        userId = id,
        firstName = first,
        lastName = last,
        displayName = name,
        email = email,
        token = token,
        role = resolvedRole,
        shelterId = shelterId,
        shelterName = shelterName,
        onboardingCompleted = onboardingCompleted == true
    )
}

private fun String.toUserRoleOrNull(): UserRole? {
    val normalized = trim()
        .removePrefix("ROLE_")
        .replace("-", "_")
        .replace(" ", "_")
        .uppercase()
    return when (normalized) {
        "SHELTER_ADMIN", "ADMIN", "ADMINISTRATOR", "SHELTERADMIN" -> UserRole.SHELTER_ADMIN
        "VETERINARIAN", "VET", "VETERINARY" -> UserRole.VETERINARIAN
        else -> null
    }
}

private class MissingRoleException : IllegalArgumentException("Missing or unsupported user role")

private class InvalidAuthPayloadException(message: String) : IllegalArgumentException(message)

private const val ShelterAdminRole = "ROLE_SHELTER_ADMIN"
