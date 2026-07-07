package com.bluepatitas.mobile.core.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.bluepatitas.mobile.data.repository.DataStoreAppPreferencesRepository
import com.bluepatitas.mobile.data.repository.DataStoreEdgeGatewaySettingsRepository
import com.bluepatitas.mobile.data.repository.DataStoreSessionRepository
import com.bluepatitas.mobile.data.repository.DataStoreShelterRepository
import com.bluepatitas.mobile.data.repository.FakeAuthRepository
import com.bluepatitas.mobile.data.repository.RealAnimalRepository
import com.bluepatitas.mobile.data.repository.RealAuthRepository
import com.bluepatitas.mobile.data.repository.RealEdgeGatewayRepository
import com.bluepatitas.mobile.data.repository.RealFeedingRepository
import com.bluepatitas.mobile.data.repository.RealMonitoringRepository
import com.bluepatitas.mobile.data.repository.RealVeterinaryRepository
import com.bluepatitas.mobile.domain.repository.AnimalRepository
import com.bluepatitas.mobile.domain.repository.AppPreferencesRepository
import com.bluepatitas.mobile.domain.repository.AuthRepository
import com.bluepatitas.mobile.domain.repository.EdgeGatewayRepository
import com.bluepatitas.mobile.domain.repository.EdgeGatewaySettingsRepository
import com.bluepatitas.mobile.domain.repository.FeedingRepository
import com.bluepatitas.mobile.domain.repository.MonitoringRepository
import com.bluepatitas.mobile.domain.repository.SessionRepository
import com.bluepatitas.mobile.domain.repository.ShelterRepository
import com.bluepatitas.mobile.domain.repository.VeterinaryRepository
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
    abstract fun bindEdgeGatewaySettingsRepository(
        repository: DataStoreEdgeGatewaySettingsRepository
    ): EdgeGatewaySettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: RealAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindShelterRepository(
        repository: DataStoreShelterRepository
    ): ShelterRepository

    @Binds
    @Singleton
    abstract fun bindVeterinaryRepository(
        repository: RealVeterinaryRepository
    ): VeterinaryRepository

    @Binds
    @Singleton
    abstract fun bindAnimalRepository(
        repository: RealAnimalRepository
    ): AnimalRepository

    @Binds
    @Singleton
    abstract fun bindFeedingRepository(
        repository: RealFeedingRepository
    ): FeedingRepository

    @Binds
    @Singleton
    abstract fun bindEdgeGatewayRepository(
        repository: RealEdgeGatewayRepository
    ): EdgeGatewayRepository

    @Binds
    @Singleton
    abstract fun bindMonitoringRepository(
        repository: RealMonitoringRepository
    ): MonitoringRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.bluePatitasDataStore
    }
}
