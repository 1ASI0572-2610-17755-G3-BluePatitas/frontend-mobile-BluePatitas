package com.bluepatitas.mobile.data.repository

import android.util.Log
import com.bluepatitas.mobile.core.network.NetworkModule
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.auth.AuthenticatedUserDto
import com.bluepatitas.mobile.data.remote.auth.SignInRequest
import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.AuthResult
import com.bluepatitas.mobile.domain.model.InvitationForm
import com.bluepatitas.mobile.domain.model.LoginCredentials
import com.bluepatitas.mobile.domain.model.RegisterAdminForm
import com.bluepatitas.mobile.domain.model.UserRole
import com.bluepatitas.mobile.domain.repository.AuthRepository
import com.bluepatitas.mobile.domain.repository.SessionRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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

            val roles = if (!user.role.isNullOrBlank()) {
                listOf(user.role)
            } else if (!user.roles.isNullOrEmpty()) {
                user.roles
            } else {
                try {
                    val userProfile = api.getUserById(user.id, "Bearer ${user.token}")
                    userProfile.roles.orEmpty()
                } catch (e: Exception) {
                    Log.e("BluePatitasAuth", "Failed to fetch user roles for ID ${user.id}", e)
                    emptyList()
                }
            }

            val updatedUser = user.copy(
                roles = roles,
                role = roles.firstOrNull()
            )

            val session = updatedUser.toSession()
            sessionRepository.startSession(session)
            AuthResult.Success(session)
        } catch (exception: HttpException) {
            val code = exception.code()
            val reason = when (code) {
                401, 403 -> "401/403 = credenciales inválidas o usuario no existe en Render."
                404 -> "404 = endpoint incorrecto."
                500 -> "500 = error interno del backend."
                else -> "HTTP $code = error de servidor."
            }
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            if (code == 401 || code == 403) {
                AuthResult.InvalidCredentials
            } else {
                AuthResult.ConnectionError(reason)
            }
        } catch (exception: com.google.gson.JsonSyntaxException) {
            val reason = "JSON parse error = DTO incorrecto."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(reason)
        } catch (exception: java.net.SocketTimeoutException) {
            val reason = "timeout = Render dormido o problema de red."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(reason)
        } catch (exception: IOException) {
            val reason = "timeout = Render dormido o problema de red."
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(reason)
        } catch (exception: IllegalArgumentException) {
            val reason = "IllegalArgumentException: ${exception.message}"
            Log.e("BluePatitasAuth", "Login failed: $reason", exception)
            AuthResult.ConnectionError(reason)
        }

    override suspend fun registerAdmin(form: RegisterAdminForm): AuthResult =
        fakeAuthRepository.registerAdmin(form)

    override suspend fun acceptInvitation(form: InvitationForm): AuthResult =
        fakeAuthRepository.acceptInvitation(form)
}

private fun AuthenticatedUserDto.toSession(): AppSession {
    val resolvedRole = role
        ?: roles.orEmpty().firstOrNull()
        ?: throw IllegalArgumentException("Missing user role")
    val first = firstName.orEmpty()
    val last = lastName.orEmpty()
    val name = listOf(first, last)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { email }

    return AppSession(
        userId = id.toString(),
        firstName = first,
        lastName = last,
        displayName = name,
        email = email,
        token = token,
        role = UserRole.valueOf(resolvedRole.removePrefix("ROLE_").uppercase()),
        shelterId = shelterId,
        shelterName = shelterName,
        onboardingCompleted = onboardingCompleted == true
    )
}
