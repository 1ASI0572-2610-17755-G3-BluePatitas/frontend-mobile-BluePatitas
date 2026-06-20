package com.bluepatitas.mobile.core.network

import android.content.Context
import android.content.pm.ApplicationInfo
import com.bluepatitas.mobile.data.remote.BearerAuthInterceptor
import com.bluepatitas.mobile.data.remote.BluePatitasApi
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    const val BackendBaseUrl = "https://backend-bluepatitas.onrender.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        bearerAuthInterceptor: BearerAuthInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val loggingInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            if (isDebuggable) {
                android.util.Log.d("BluePatitasNetwork", "Request: ${request.method} ${request.url}")
            }
            try {
                val response = chain.proceed(request)
                if (isDebuggable) {
                    android.util.Log.d("BluePatitasNetwork", "Response: ${response.code} for ${request.url}")
                }
                if (isDebuggable && !response.isSuccessful) {
                    val errorBody = response.peekBody(16_384).string()
                    android.util.Log.e("BluePatitasNetwork", "Response Error Body: $errorBody")
                }
                response
            } catch (e: Exception) {
                if (isDebuggable) {
                    android.util.Log.e("BluePatitasNetwork", "Request failed: ${request.method} ${request.url}", e)
                }
                throw e
            }
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
