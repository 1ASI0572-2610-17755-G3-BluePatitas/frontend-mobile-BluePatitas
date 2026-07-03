package com.bluepatitas.mobile.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.animal.AnimalDto
import com.bluepatitas.mobile.data.remote.animal.RegisterAnimalRequestDto
import com.bluepatitas.mobile.data.remote.animal.UpdateHealthRequestDto
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
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class RealAnimalRepository @Inject constructor(
    private val api: BluePatitasApi,
    @param:ApplicationContext private val context: Context
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

    override suspend fun uploadAnimalImage(imageUri: String): BluePatitasResult<String> =
        runAnimalRequest("POST /api/v1/media/upload") {
            val uri = Uri.parse(imageUri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw AnimalRepositoryException(
                    AuthFailureReason.BadRequest,
                    "Could not read selected image."
                )
            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            val fileName = context.displayName(uri) ?: "animal-photo"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = api.uploadMedia(filePart)
            if (response.isSuccessful) {
                val body = response.body()
                val uploadedUrl = body?.secureUrl?.takeIf { it.isNotBlank() }
                    ?: body?.url?.takeIf { it.isNotBlank() }
                    ?: throw AnimalRepositoryException(
                        AuthFailureReason.Serialization,
                        "Media upload response did not include a URL."
                    )
                BluePatitasResult.Success(uploadedUrl)
            } else {
                throw response.toAnimalException()
            }
        }

    override suspend fun updateHealthCondition(
        id: String,
        healthCondition: String
    ): BluePatitasResult<Unit> =
        runAnimalRequest("PUT /api/animals/$id/health") {
            val response = api.updateAnimalHealth(
                id = id,
                request = UpdateHealthRequestDto(healthCondition = healthCondition)
            )
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

private fun Response<*>.toAnimalException(): AnimalRepositoryException {
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
        photoUrl = photoUrl?.takeIf { it.isNotBlank() },
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

private fun Context.displayName(uri: Uri): String? =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
