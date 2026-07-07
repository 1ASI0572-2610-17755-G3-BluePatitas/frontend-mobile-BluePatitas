package com.bluepatitas.mobile.data.repository

import android.util.Log
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.feeding.CreateFeedingPlanRequestDto
import com.bluepatitas.mobile.data.remote.feeding.FeedingPlanDto
import com.bluepatitas.mobile.data.remote.feeding.UpdateFeedingPlanRequestDto
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.DietType
import com.bluepatitas.mobile.domain.model.FeedingPlan
import com.bluepatitas.mobile.domain.model.FeedingPlanForm
import com.bluepatitas.mobile.domain.model.FeedingPlanStatus
import com.bluepatitas.mobile.domain.model.FeedingSchedule
import com.bluepatitas.mobile.domain.model.FoodAmount
import com.bluepatitas.mobile.domain.repository.FeedingRepository
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
class RealFeedingRepository @Inject constructor(
    private val api: BluePatitasApi
) : FeedingRepository {
    override suspend fun getPlans(): BluePatitasResult<List<FeedingPlan>> =
        runFeedingRequest("GET /api/feeding/plans") {
            BluePatitasResult.Success(api.getFeedingPlans().map { it.toDomain() })
        }

    override suspend fun getPlansByAnimal(animalId: String): BluePatitasResult<List<FeedingPlan>> =
        runFeedingRequest("GET /api/feeding/plans/$animalId") {
            BluePatitasResult.Success(api.getFeedingPlansByAnimal(animalId).map { it.toDomain() })
        }

    override suspend fun createPlan(form: FeedingPlanForm): BluePatitasResult<Unit> =
        runFeedingRequest("POST /api/feeding/plans") {
            val response = api.createFeedingPlan(form.toCreateRequest())
            if (response.isSuccessful) BluePatitasResult.Success(Unit) else throw response.toFeedingException()
        }

    override suspend fun updatePlan(id: String, form: FeedingPlanForm): BluePatitasResult<Unit> =
        runFeedingRequest("PUT /api/feeding/plans/$id") {
            val response = api.updateFeedingPlan(id, form.toUpdateRequest())
            if (response.isSuccessful) BluePatitasResult.Success(Unit) else throw response.toFeedingException()
        }

    override suspend fun activatePlan(id: String): BluePatitasResult<Unit> =
        runFeedingRequest("PUT /api/feeding/plans/$id/activate") {
            val response = api.activateFeedingPlan(id)
            if (response.isSuccessful) BluePatitasResult.Success(Unit) else throw response.toFeedingException()
        }

    override suspend fun deactivatePlan(id: String): BluePatitasResult<Unit> =
        runFeedingRequest("PUT /api/feeding/plans/$id/deactivate") {
            val response = api.deactivateFeedingPlan(id)
            if (response.isSuccessful) BluePatitasResult.Success(Unit) else throw response.toFeedingException()
        }

    private inline fun <T> runFeedingRequest(
        operation: String,
        block: () -> BluePatitasResult<T>
    ): BluePatitasResult<T> =
        try {
            block()
        } catch (exception: HttpException) {
            Log.e("BluePatitasFeeding", "$operation failed with HTTP ${exception.code()}.", exception)
            BluePatitasResult.Error(exception.toFeedingException())
        } catch (exception: JsonParseException) {
            Log.e("BluePatitasFeeding", "$operation response does not match DTO.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Serialization, "$operation response does not match DTO.", exception))
        } catch (exception: MalformedJsonException) {
            Log.e("BluePatitasFeeding", "$operation response has malformed JSON.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Serialization, "$operation response has malformed JSON.", exception))
        } catch (exception: SocketTimeoutException) {
            Log.e("BluePatitasFeeding", "$operation timed out.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Timeout, "$operation timed out.", exception))
        } catch (exception: UnknownHostException) {
            Log.e("BluePatitasFeeding", "$operation could not resolve host.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Network, "$operation could not resolve host.", exception))
        } catch (exception: SSLException) {
            Log.e("BluePatitasFeeding", "$operation failed due to SSL.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Network, "$operation failed due to SSL.", exception))
        } catch (exception: IOException) {
            Log.e("BluePatitasFeeding", "$operation failed due to IO.", exception)
            BluePatitasResult.Error(FeedingRepositoryException(AuthFailureReason.Network, "$operation failed due to IO.", exception))
        } catch (exception: RuntimeException) {
            Log.e("BluePatitasFeeding", "$operation failed unexpectedly.", exception)
            BluePatitasResult.Error(exception)
        }
}

class FeedingRepositoryException(
    val reason: AuthFailureReason,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

private fun Response<*>.toFeedingException(): FeedingRepositoryException {
    val code = code()
    val errorText = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
    Log.e("BluePatitasFeeding", "Feeding request failed: HTTP $code. $errorText")
    return FeedingRepositoryException(
        reason = code.toFailureReason(),
        message = "Feeding request failed: HTTP $code. $errorText".trim()
    )
}

private fun HttpException.toFeedingException(): FeedingRepositoryException =
    FeedingRepositoryException(
        reason = code().toFailureReason(),
        message = "Feeding request failed: HTTP ${code()}.",
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

private fun FeedingPlanForm.toCreateRequest(): CreateFeedingPlanRequestDto =
    CreateFeedingPlanRequestDto(
        animalId = animalId,
        dietName = dietName.trim(),
        nutritionalNotes = nutritionalNotes.trim(),
        foodQuantity = foodQuantity,
        foodUnit = foodUnit.trim(),
        timesPerDay = timesPerDay,
        scheduledTimes = scheduledTimes.trim(),
        toleranceMinutes = toleranceMinutes
    )

private fun FeedingPlanForm.toUpdateRequest(): UpdateFeedingPlanRequestDto =
    UpdateFeedingPlanRequestDto(
        dietName = dietName.trim(),
        nutritionalNotes = nutritionalNotes.trim(),
        foodQuantity = foodQuantity,
        foodUnit = foodUnit.trim(),
        timesPerDay = timesPerDay,
        scheduledTimes = scheduledTimes.trim(),
        toleranceMinutes = toleranceMinutes
    )

private fun FeedingPlanDto.toDomain(): FeedingPlan =
    FeedingPlan(
        id = id.orEmpty(),
        animalId = animalId.orEmpty(),
        dietType = DietType(
            name = dietType?.name.orEmpty(),
            nutritionalNotes = dietType?.nutritionalNotes
        ),
        foodAmount = FoodAmount(
            quantity = foodAmount?.quantity,
            unit = foodAmount?.unit.orEmpty()
        ),
        schedule = FeedingSchedule(
            timesPerDay = schedule?.timesPerDay,
            scheduledTimes = schedule?.scheduledTimes.orEmpty(),
            toleranceMinutes = schedule?.toleranceMinutes
        ),
        status = FeedingPlanStatus.fromApiValue(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
