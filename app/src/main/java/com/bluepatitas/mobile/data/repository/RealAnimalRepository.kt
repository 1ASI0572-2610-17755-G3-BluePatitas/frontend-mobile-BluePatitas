package com.bluepatitas.mobile.data.repository

import android.util.Log
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.animal.AnimalDto
import com.bluepatitas.mobile.data.remote.animal.RegisterAnimalRequestDto
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.AnimalSummary
import com.bluepatitas.mobile.domain.model.RegisterAnimalForm
import com.bluepatitas.mobile.domain.repository.AnimalRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class RealAnimalRepository @Inject constructor(
    private val api: BluePatitasApi
) : AnimalRepository {
    override suspend fun getAnimals(): BluePatitasResult<List<AnimalSummary>> =
        runAnimalRequest("GET /api/animals") {
            BluePatitasResult.Success(api.getAnimals().map { it.toDomain() })
        }

    override suspend fun getAnimal(id: String): BluePatitasResult<AnimalSummary> =
        runAnimalRequest("GET /api/animals/$id") {
            BluePatitasResult.Success(api.getAnimal(id).toDomain())
        }

    override suspend fun registerAnimal(form: RegisterAnimalForm): BluePatitasResult<Unit> =
        runAnimalRequest("POST /api/animals") {
            val response = api.createAnimal(form.toRequest())
            if (response.isSuccessful) {
                BluePatitasResult.Success(Unit)
            } else {
                throw response.toAnimalException()
            }
        }

    private inline fun <T> runAnimalRequest(
        operation: String,
        block: () -> BluePatitasResult<T>
    ): BluePatitasResult<T> =
        try {
            block()
        } catch (exception: HttpException) {
            Log.e("BluePatitasAnimals", "$operation failed with HTTP ${exception.code()}.", exception)
            BluePatitasResult.Error(exception.toAnimalException())
        } catch (exception: JsonParseException) {
            Log.e("BluePatitasAnimals", "$operation response does not match DTO.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Serialization, "$operation response does not match DTO.", exception)
            )
        } catch (exception: MalformedJsonException) {
            Log.e("BluePatitasAnimals", "$operation response has malformed JSON.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Serialization, "$operation response has malformed JSON.", exception)
            )
        } catch (exception: SocketTimeoutException) {
            Log.e("BluePatitasAnimals", "$operation timed out.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Timeout, "Render may be waking up or backend took too long.", exception)
            )
        } catch (exception: UnknownHostException) {
            Log.e("BluePatitasAnimals", "$operation could not resolve host.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Network, "No internet, DNS failure, or backend unreachable.", exception)
            )
        } catch (exception: SSLException) {
            Log.e("BluePatitasAnimals", "$operation failed due to SSL.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Network, "Certificate or secure connection problem.", exception)
            )
        } catch (exception: IOException) {
            Log.e("BluePatitasAnimals", "$operation failed due to IO.", exception)
            BluePatitasResult.Error(
                AnimalRepositoryException(AuthFailureReason.Network, "Internet problem or backend unreachable.", exception)
            )
        } catch (exception: RuntimeException) {
            Log.e("BluePatitasAnimals", "$operation failed unexpectedly.", exception)
            BluePatitasResult.Error(exception)
        }
}

class AnimalRepositoryException(
    val reason: AuthFailureReason,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

private fun Response<AnimalDto>.toAnimalException(): AnimalRepositoryException {
    val code = code()
    val errorText = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
    Log.e("BluePatitasAnimals", "Animal request failed: HTTP $code. $errorText")
    return AnimalRepositoryException(
        reason = code.toFailureReason(),
        message = "Animal request failed: HTTP $code. $errorText".trim()
    )
}

private fun HttpException.toAnimalException(): AnimalRepositoryException =
    AnimalRepositoryException(
        reason = code().toFailureReason(),
        message = "Animal request failed: HTTP ${code()}.",
        cause = this
    )

private fun Int.toFailureReason(): AuthFailureReason =
    when (this) {
        400 -> AuthFailureReason.BadRequest
        401, 403 -> AuthFailureReason.SessionExpired
        404 -> AuthFailureReason.EndpointNotFound
        409 -> AuthFailureReason.Conflict
        500 -> AuthFailureReason.ServerError
        else -> AuthFailureReason.Unknown
    }

private fun RegisterAnimalForm.toRequest(): RegisterAnimalRequestDto =
    RegisterAnimalRequestDto(
        name = name.trim(),
        species = species.trim(),
        breed = breed.trim(),
        estimatedAgeMonths = estimatedAgeMonths,
        assignedPerimeterId = null,
        photoUrl = null,
        weightKg = weightKg
    )

private fun AnimalDto.toDomain(): AnimalSummary =
    AnimalSummary(
        id = id.orEmpty(),
        name = name.orEmpty(),
        photoUrl = photoUrl,
        species = species ?: speciesDetails?.species.orEmpty(),
        breed = breed ?: speciesDetails?.breed,
        estimatedAgeMonths = speciesDetails?.estimatedAgeMonths,
        healthCondition = healthCondition,
        weightKg = weightKg,
        zoneName = assignedPerimeterId
    )
