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
            val session = user.toSession()
            sessionRepository.startSession(session)
            AuthResult.Success(session)
        } catch (exception: HttpException) {
            if (exception.code() == 401 || exception.code() == 403) {
                AuthResult.InvalidCredentials
            } else {
                AuthResult.ConnectionError(exception.message())
            }
        } catch (exception: IOException) {
            Log.w(
                "BluePatitasNetwork",
                "Login connection failed. Backend base URL: ${NetworkModule.BackendBaseUrl}",
                exception
            )
            AuthResult.ConnectionError(exception.localizedMessage)
        } catch (exception: IllegalArgumentException) {
            AuthResult.ConnectionError(exception.localizedMessage)
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
