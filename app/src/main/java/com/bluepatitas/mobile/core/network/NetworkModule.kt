package com.bluepatitas.mobile.core.network

import com.bluepatitas.mobile.data.remote.BearerAuthInterceptor
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    const val BackendBaseUrl = "https://backend-bluepatitas.onrender.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        bearerAuthInterceptor: BearerAuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            android.util.Log.d("BluePatitasNetwork", "Request: ${request.method} ${request.url}")
            try {
                val response = chain.proceed(request)
                android.util.Log.d("BluePatitasNetwork", "Response: ${response.code} for ${request.url}")
                if (!response.isSuccessful) {
                    val errorBody = response.peekBody(Long.MAX_VALUE).string()
                    android.util.Log.e("BluePatitasNetwork", "Response Error Body: $errorBody")
                }
                response
            } catch (e: Exception) {
                android.util.Log.e("BluePatitasNetwork", "Request failed: ${request.url}", e)
                throw e
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(bearerAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BackendBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBluePatitasApi(retrofit: Retrofit): BluePatitasApi =
        retrofit.create(BluePatitasApi::class.java)
}
