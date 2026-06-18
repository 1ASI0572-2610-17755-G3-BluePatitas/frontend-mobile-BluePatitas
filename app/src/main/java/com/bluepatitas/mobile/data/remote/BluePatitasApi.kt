package com.bluepatitas.mobile.data.remote

import com.bluepatitas.mobile.data.remote.animal.AnimalDto
import com.bluepatitas.mobile.data.remote.auth.AuthenticatedUserDto
import com.bluepatitas.mobile.data.remote.auth.SignInRequest
import com.bluepatitas.mobile.data.remote.monitoring.MonitoringZoneDto
import com.bluepatitas.mobile.data.remote.monitoring.PerimeterAlertDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryAnimalDto
import com.bluepatitas.mobile.data.remote.veterinary.VeterinaryDashboardDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BluePatitasApi {
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body request: SignInRequest): AuthenticatedUserDto

    @GET("api/veterinary/me/dashboard")
    suspend fun getVeterinaryDashboard(): VeterinaryDashboardDto

    @GET("api/veterinary/me/animals")
    suspend fun getVeterinaryAnimals(): List<VeterinaryAnimalDto>

    @GET("api/animals")
    suspend fun getAnimals(): List<AnimalDto>

    @GET("api/monitoring/zones")
    suspend fun getMonitoringZones(): List<MonitoringZoneDto>

    @GET("api/monitoring/alerts")
    suspend fun getMonitoringAlerts(): List<PerimeterAlertDto>
}
