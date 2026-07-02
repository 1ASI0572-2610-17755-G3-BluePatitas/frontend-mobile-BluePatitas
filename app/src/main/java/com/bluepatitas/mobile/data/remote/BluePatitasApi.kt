package com.bluepatitas.mobile.data.remote

import com.bluepatitas.mobile.data.remote.animal.AnimalDto
import com.bluepatitas.mobile.data.remote.animal.RegisterAnimalRequestDto
import com.bluepatitas.mobile.data.remote.auth.AuthenticatedUserDto
import com.bluepatitas.mobile.data.remote.auth.SignInRequest
import com.bluepatitas.mobile.data.remote.auth.SignUpRequestDto
import com.bluepatitas.mobile.data.remote.auth.UserDto
import com.bluepatitas.mobile.data.remote.monitoring.MonitoringZoneDto
import com.bluepatitas.mobile.data.remote.monitoring.PerimeterAlertDto
import com.bluepatitas.mobile.data.remote.shelter.ShelterDto
import com.bluepatitas.mobile.data.remote.shelter.ShelterRequestDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryAnimalDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryDashboardDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.PUT
import okhttp3.ResponseBody

interface BluePatitasApi {
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body request: SignInRequest): AuthenticatedUserDto

    @POST("api/v1/authentication/sign-up")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<Unit>

    @GET("api/v1/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String,
        @Header("Authorization") authorizationHeader: String
    ): UserDto

    @GET("api/veterinary/me/dashboard")
    suspend fun getVeterinaryDashboard(): VeterinaryDashboardDto

    @GET("api/veterinary/me/animals")
    suspend fun getVeterinaryAnimals(): List<VeterinaryAnimalDto>

    @GET("api/animals")
    suspend fun getAnimals(): List<AnimalDto>

    @POST("api/animals")
    suspend fun createAnimal(@Body request: RegisterAnimalRequestDto): Response<AnimalDto>

    @GET("api/animals/{id}")
    suspend fun getAnimal(@Path("id") id: String): AnimalDto

    @GET("api/monitoring/zones")
    suspend fun getMonitoringZones(): List<MonitoringZoneDto>

    @GET("api/monitoring/alerts")
    suspend fun getMonitoringAlerts(): List<PerimeterAlertDto>

    @GET("api/monitoring/shelter")
    suspend fun getShelter(): ShelterDto

    @GET("api/monitoring/shelter")
    suspend fun getShelterRaw(): Response<ResponseBody>

    @POST("api/monitoring/shelter")
    suspend fun createShelter(@Body request: ShelterRequestDto): Response<ShelterDto>

    @PUT("api/monitoring/shelter")
    suspend fun updateShelter(@Body request: ShelterRequestDto): Response<ShelterDto>
}
