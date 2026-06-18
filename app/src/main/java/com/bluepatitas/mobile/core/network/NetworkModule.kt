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
    const val BackendBaseUrl = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        bearerAuthInterceptor: BearerAuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(bearerAuthInterceptor)
            .build()

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
