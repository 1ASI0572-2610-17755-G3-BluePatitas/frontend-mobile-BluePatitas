package com.bluepatitas.mobile.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import com.bluepatitas.mobile.data.remote.shelter.ShelterDto
import com.bluepatitas.mobile.data.remote.shelter.ShelterRequestDto
import com.bluepatitas.mobile.domain.model.AuthFailureReason
import com.bluepatitas.mobile.domain.model.ShelterDraft
import com.bluepatitas.mobile.domain.model.ShelterProfile
import com.bluepatitas.mobile.domain.repository.ShelterRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class DataStoreShelterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val api: BluePatitasApi
) : ShelterRepository {

    override val shelter: Flow<ShelterProfile?> = dataStore.data.map { preferences ->
        if (preferences[PreferenceKeys.ShelterCreated] != true) return@map null
        ShelterProfile(
            id = preferences[PreferenceKeys.ShelterId] ?: "shelter-wuf",
            name = preferences[PreferenceKeys.ShelterName].orEmpty(),
            taxId = preferences[PreferenceKeys.ShelterTaxId].orEmpty(),
            institutionalEmail = preferences[PreferenceKeys.ShelterEmail].orEmpty(),
            contactPhone = preferences[PreferenceKeys.ShelterPhone].orEmpty(),
            address = preferences[PreferenceKeys.ShelterAddress].orEmpty(),
            reference = preferences[PreferenceKeys.ShelterReference].orEmpty(),
            district = preferences[PreferenceKeys.ShelterDistrict].orEmpty(),
            city = preferences[PreferenceKeys.ShelterCity].orEmpty()
        )
    }

    override suspend fun saveDraft(draft: ShelterDraft) {
        dataStore.edit { preferences ->
            writeDraft(preferences, draft, created = false)
        }
    }

    override suspend fun createShelter(draft: ShelterDraft): ShelterProfile {
        val request = draft.toRequest(administrator = currentAdministrator())
        val remoteShelter = try {
            val createResponse = api.createShelter(request)
            if (createResponse.isSuccessful) {
                createResponse.body() ?: api.getShelter()
            } else if (createResponse.shouldTryUpdate()) {
                val updateResponse = api.updateShelter(request)
                updateResponse.bodyOrThrow()
            } else {
                throw createResponse.toShelterException()
            }
        } catch (exception: HttpException) {
            Log.e("BluePatitasShelter", "Shelter request failed with HTTP ${exception.code()}.", exception)
            throw exception.toShelterException()
        } catch (exception: JsonParseException) {
            Log.e("BluePatitasShelter", "Shelter response does not match DTO.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Serialization, "Shelter response does not match DTO.", exception)
        } catch (exception: MalformedJsonException) {
            Log.e("BluePatitasShelter", "Shelter response has malformed JSON.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Serialization, "Shelter response has malformed JSON.", exception)
        } catch (exception: SocketTimeoutException) {
            Log.e("BluePatitasShelter", "Shelter request timed out.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Timeout, "Render may be waking up or backend took too long.", exception)
        } catch (exception: UnknownHostException) {
            Log.e("BluePatitasShelter", "Shelter request could not resolve host.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "No internet, DNS failure, or backend unreachable.", exception)
        } catch (exception: SSLException) {
            Log.e("BluePatitasShelter", "Shelter request failed due to SSL.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "Certificate or secure connection problem.", exception)
        } catch (exception: IOException) {
            Log.e("BluePatitasShelter", "Shelter request failed due to IO.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "Internet problem or backend unreachable.", exception)
        }

        val profile = remoteShelter.toProfile(draft)
        dataStore.edit { preferences ->
            writeProfile(preferences, profile)
            preferences[PreferenceKeys.OnboardingCompleted] = true
            preferences[PreferenceKeys.ShelterSessionName] = profile.name
            preferences[PreferenceKeys.ShelterId] = profile.id
        }
        return profile
    }

    override suspend fun syncShelterFromBackend(): ShelterProfile? {
        val fallbackDraft = dataStore.data.first().toDraftFallback()
        val remoteShelter = try {
            api.getShelter()
        } catch (exception: HttpException) {
            if (exception.code() == 404) {
                Log.i("BluePatitasShelter", "No shelter found for current admin session.")
                return null
            }
            Log.e("BluePatitasShelter", "Shelter sync failed with HTTP ${exception.code()}.", exception)
            throw exception.toShelterException()
        } catch (exception: JsonParseException) {
            Log.e("BluePatitasShelter", "Shelter sync response does not match DTO.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Serialization, "Shelter sync response does not match DTO.", exception)
        } catch (exception: MalformedJsonException) {
            Log.e("BluePatitasShelter", "Shelter sync response has malformed JSON.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Serialization, "Shelter sync response has malformed JSON.", exception)
        } catch (exception: SocketTimeoutException) {
            Log.e("BluePatitasShelter", "Shelter sync timed out.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Timeout, "Render may be waking up or backend took too long.", exception)
        } catch (exception: UnknownHostException) {
            Log.e("BluePatitasShelter", "Shelter sync could not resolve host.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "No internet, DNS failure, or backend unreachable.", exception)
        } catch (exception: SSLException) {
            Log.e("BluePatitasShelter", "Shelter sync failed due to SSL.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "Certificate or secure connection problem.", exception)
        } catch (exception: IOException) {
            Log.e("BluePatitasShelter", "Shelter sync failed due to IO.", exception)
            throw ShelterRepositoryException(AuthFailureReason.Network, "Internet problem or backend unreachable.", exception)
        }

        if (!remoteShelter.isValidShelter()) return null

        val profile = remoteShelter.toProfile(fallbackDraft)
        dataStore.edit { preferences ->
            writeProfile(preferences, profile)
            preferences[PreferenceKeys.OnboardingCompleted] = true
            preferences[PreferenceKeys.ShelterSessionName] = profile.name
            preferences[PreferenceKeys.ShelterId] = profile.id
        }
        return profile
    }

    private suspend fun currentAdministrator(): String {
        val preferences = dataStore.data.first()
        return preferences[PreferenceKeys.DisplayName]
            ?.takeIf { it.isNotBlank() }
            ?: preferences[PreferenceKeys.Email]
            ?.takeIf { it.isNotBlank() }
            ?: "Shelter administrator"
    }

    private suspend fun Response<ShelterDto>.bodyOrThrow(): ShelterDto =
        if (isSuccessful) {
            body() ?: api.getShelter()
        } else {
            throw toShelterException()
        }

    private suspend fun Response<ShelterDto>.toShelterException(): ShelterRepositoryException {
        val code = code()
        if (code == 401 || code == 403) clearStoredSession()
        val errorText = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
        Log.e("BluePatitasShelter", "Shelter request failed: HTTP $code. $errorText")
        return ShelterRepositoryException(
            reason = code.toFailureReason(),
            message = "Shelter request failed: HTTP $code. $errorText".trim(),
            cause = null
        )
    }

    private fun Response<ShelterDto>.shouldTryUpdate(): Boolean {
        val code = code()
        if (code == 409) return true
        val errorText = runCatching { errorBody()?.string().orEmpty().lowercase() }.getOrDefault("")
        return errorText.contains("duplicate") ||
            errorText.contains("already") ||
            errorText.contains("exist") ||
            errorText.contains("conflict")
    }

    private suspend fun HttpException.toShelterException(): ShelterRepositoryException {
        val code = code()
        if (code == 401 || code == 403) clearStoredSession()
        return ShelterRepositoryException(
            reason = code.toFailureReason(),
            message = "Shelter request failed: HTTP $code.",
            cause = this
        )
    }

    private fun Int.toFailureReason(): AuthFailureReason =
        when (this) {
            400 -> AuthFailureReason.BadRequest
            401, 403 -> AuthFailureReason.SessionExpired
            404 -> AuthFailureReason.EndpointNotFound
            409 -> AuthFailureReason.Conflict
            500 -> AuthFailureReason.ServerError
            else -> AuthFailureReason.Unknown
        }

    private suspend fun clearStoredSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.UserId)
            preferences.remove(PreferenceKeys.FirstName)
            preferences.remove(PreferenceKeys.LastName)
            preferences.remove(PreferenceKeys.DisplayName)
            preferences.remove(PreferenceKeys.Email)
            preferences.remove(PreferenceKeys.Token)
            preferences.remove(PreferenceKeys.Role)
            preferences.remove(PreferenceKeys.ShelterId)
            preferences.remove(PreferenceKeys.ShelterSessionName)
            preferences.remove(PreferenceKeys.OnboardingCompleted)
        }
    }

    private fun writeDraft(
        preferences: MutablePreferences,
        draft: ShelterDraft,
        created: Boolean
    ) {
        preferences[PreferenceKeys.ShelterCreated] = created
        preferences[PreferenceKeys.ShelterName] = draft.name.trim()
        preferences[PreferenceKeys.ShelterTaxId] = draft.taxId.trim()
        preferences[PreferenceKeys.ShelterEmail] = draft.institutionalEmail.trim()
        preferences[PreferenceKeys.ShelterPhone] = draft.contactPhone.trim()
        preferences[PreferenceKeys.ShelterAddress] = draft.address.trim()
        preferences[PreferenceKeys.ShelterReference] = draft.reference.trim()
        preferences[PreferenceKeys.ShelterDistrict] = draft.district.trim()
        preferences[PreferenceKeys.ShelterCity] = draft.city.trim()
    }

    private fun writeProfile(
        preferences: MutablePreferences,
        profile: ShelterProfile
    ) {
        preferences[PreferenceKeys.ShelterCreated] = true
        preferences[PreferenceKeys.ShelterName] = profile.name
        preferences[PreferenceKeys.ShelterTaxId] = profile.taxId
        preferences[PreferenceKeys.ShelterEmail] = profile.institutionalEmail
        preferences[PreferenceKeys.ShelterPhone] = profile.contactPhone
        preferences[PreferenceKeys.ShelterAddress] = profile.address
        preferences[PreferenceKeys.ShelterReference] = profile.reference
        preferences[PreferenceKeys.ShelterDistrict] = profile.district
        preferences[PreferenceKeys.ShelterCity] = profile.city
    }
}

class ShelterRepositoryException(
    val reason: AuthFailureReason,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

private fun ShelterDraft.toRequest(administrator: String): ShelterRequestDto =
    ShelterRequestDto(
        name = name.trim(),
        city = city.trim(),
        address = address.trim(),
        administrator = administrator.trim(),
        phone = contactPhone.trim(),
        email = institutionalEmail.trim()
    )

private fun ShelterDto.toProfile(draft: ShelterDraft): ShelterProfile =
    ShelterProfile(
        id = id.orEmpty(),
        name = name.orEmpty().ifBlank { draft.name.trim() },
        taxId = draft.taxId.trim(),
        institutionalEmail = email.orEmpty().ifBlank { draft.institutionalEmail.trim() },
        contactPhone = phone.orEmpty().ifBlank { draft.contactPhone.trim() },
        address = address.orEmpty().ifBlank { draft.address.trim() },
        reference = draft.reference.trim(),
        district = draft.district.trim(),
        city = city.orEmpty().ifBlank { draft.city.trim() }
    )

private fun ShelterDto.isValidShelter(): Boolean =
    !id.isNullOrBlank() || !name.isNullOrBlank()

private fun Preferences.toDraftFallback(): ShelterDraft =
    ShelterDraft(
        name = this[PreferenceKeys.ShelterName].orEmpty(),
        taxId = this[PreferenceKeys.ShelterTaxId].orEmpty(),
        institutionalEmail = this[PreferenceKeys.ShelterEmail].orEmpty(),
        contactPhone = this[PreferenceKeys.ShelterPhone].orEmpty(),
        address = this[PreferenceKeys.ShelterAddress].orEmpty(),
        reference = this[PreferenceKeys.ShelterReference].orEmpty(),
        district = this[PreferenceKeys.ShelterDistrict].orEmpty(),
        city = this[PreferenceKeys.ShelterCity].orEmpty()
    )
