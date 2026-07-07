package com.bluepatitas.mobile.data.repository

import android.util.Log
import com.bluepatitas.mobile.core.common.BluePatitasResult
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.DispenserSchedule
import com.bluepatitas.mobile.domain.model.DispenserStatus
import com.bluepatitas.mobile.domain.model.EdgeGatewaySnapshot
import com.bluepatitas.mobile.domain.model.EdgeSimulatorStatus
import com.bluepatitas.mobile.domain.repository.EdgeGatewayRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class RealEdgeGatewayRepository @Inject constructor() : EdgeGatewayRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun getSnapshot(baseUrl: String): BluePatitasResult<EdgeGatewaySnapshot> =
        runEdgeRequest("GET Edge snapshot") {
            val simulator = getJson(baseUrl, "/api/simulador/estado").toSimulatorStatus()
            val dispenser = getJson(baseUrl, "/api/dispensador/status").toDispenserStatus()
            val schedule = getJson(baseUrl, "/api/dispensador/configurar_horario").toDispenserSchedule()
            BluePatitasResult.Success(
                EdgeGatewaySnapshot(
                    simulatorStatus = simulator,
                    dispenserStatus = dispenser,
                    schedule = schedule
                )
            )
        }

    override suspend fun forceFeed(baseUrl: String): BluePatitasResult<Unit> =
        runEdgeRequest("POST /api/dispensador/forzar_alimento") {
            postJson(baseUrl, "/api/dispensador/forzar_alimento", "{}")
            BluePatitasResult.Success(Unit)
        }

    override suspend fun configureSchedule(
        baseUrl: String,
        active: Boolean,
        interval: String
    ): BluePatitasResult<Unit> =
        runEdgeRequest("POST /api/dispensador/configurar_horario") {
            val body = gson.toJson(mapOf("activo" to active, "intervalo" to interval.trim()))
            postJson(baseUrl, "/api/dispensador/configurar_horario", body)
            BluePatitasResult.Success(Unit)
        }

    private suspend inline fun <T> runEdgeRequest(
        operation: String,
        crossinline block: () -> BluePatitasResult<T>
    ): BluePatitasResult<T> =
        try {
            withContext(Dispatchers.IO) {
                block()
            }
        } catch (exception: EdgeGatewayRepositoryException) {
            Log.e("BluePatitasEdge", "$operation failed: ${exception.message}", exception)
            BluePatitasResult.Error(exception)
        } catch (exception: SocketTimeoutException) {
            Log.e("BluePatitasEdge", "$operation timed out.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Timeout, "$operation timed out.", exception))
        } catch (exception: UnknownHostException) {
            Log.e("BluePatitasEdge", "$operation could not resolve host.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Network, "$operation could not resolve host.", exception))
        } catch (exception: SSLException) {
            Log.e("BluePatitasEdge", "$operation failed due to SSL.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Network, "$operation failed due to SSL.", exception))
        } catch (exception: IOException) {
            Log.e("BluePatitasEdge", "$operation failed due to IO.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Network, "$operation failed due to IO.", exception))
        } catch (exception: JsonParseException) {
            Log.e("BluePatitasEdge", "$operation response does not match DTO.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Serialization, "$operation response does not match DTO.", exception))
        } catch (exception: RuntimeException) {
            Log.e("BluePatitasEdge", "$operation failed unexpectedly.", exception)
            BluePatitasResult.Error(EdgeGatewayRepositoryException(AuthFailureReason.Unknown, "$operation failed unexpectedly.", exception))
        }

    private fun getJson(baseUrl: String, path: String): JsonObject {
        val request = Request.Builder()
            .url(edgeUrl(baseUrl, path))
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw response.toEdgeException(path)
            return gson.fromJson(response.body?.string().orEmpty(), JsonObject::class.java)
        }
    }

    private fun postJson(baseUrl: String, path: String, body: String) {
        val request = Request.Builder()
            .url(edgeUrl(baseUrl, path))
            .post(body.toRequestBody(jsonMediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw response.toEdgeException(path)
        }
    }
}

class EdgeGatewayRepositoryException(
    val reason: AuthFailureReason,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

private fun edgeUrl(baseUrl: String, path: String): String =
    "${baseUrl.trim().trimEnd('/')}/${path.trimStart('/')}"

private fun okhttp3.Response.toEdgeException(path: String): EdgeGatewayRepositoryException {
    val code = code
    val errorText = runCatching { body?.string().orEmpty() }.getOrDefault("")
    return EdgeGatewayRepositoryException(
        reason = when (code) {
            400 -> AuthFailureReason.BadRequest
            401, 403 -> AuthFailureReason.SessionExpired
            404 -> AuthFailureReason.EndpointNotFound
            500 -> AuthFailureReason.ServerError
            else -> AuthFailureReason.Unknown
        },
        message = "Edge request $path failed: HTTP $code. $errorText".trim()
    )
}

private fun JsonObject.toSimulatorStatus(): EdgeSimulatorStatus =
    EdgeSimulatorStatus(
        simulationActive = get("simulacion_activa")?.asBoolean ?: false,
        latitude = get("latitude")?.takeUnless { it.isJsonNull }?.asDouble,
        longitude = get("longitude")?.takeUnless { it.isJsonNull }?.asDouble
    )

private fun JsonObject.toDispenserStatus(): DispenserStatus =
    DispenserStatus(active = get("activar")?.asBoolean ?: false)

private fun JsonObject.toDispenserSchedule(): DispenserSchedule =
    DispenserSchedule(
        active = get("activo")?.asBoolean ?: false,
        intervalSeconds = get("intervalo_segundos")?.takeUnless { it.isJsonNull }?.asInt
    )
