package com.bluepatitas.mobile.core.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.bluepatitas.mobile.data.repository.DataStoreAppPreferencesRepository
import com.bluepatitas.mobile.data.repository.DataStoreSessionRepository
import com.bluepatitas.mobile.data.repository.DataStoreShelterRepository
import com.bluepatitas.mobile.data.repository.FakeAuthRepository
import com.bluepatitas.mobile.domain.repository.AppPreferencesRepository
import com.bluepatitas.mobile.domain.repository.AuthRepository
import com.bluepatitas.mobile.domain.repository.SessionRepository
import com.bluepatitas.mobile.domain.repository.ShelterRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        repository: DataStoreSessionRepository
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(
        repository: DataStoreAppPreferencesRepository
    ): AppPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: FakeAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindShelterRepository(
        repository: DataStoreShelterRepository
    ): ShelterRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.bluePatitasDataStore
    }
}
