package com.bluepatitas.mobile.app

import android.app.Application
import com.bluepatitas.mobile.core.persistence.bluePatitasDataStore
import com.bluepatitas.mobile.core.util.AppLocaleController
import com.bluepatitas.mobile.data.local.PreferenceKeys
import com.bluepatitas.mobile.domain.model.AppLanguage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class BluePatitasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val savedLanguageTag = runBlocking {
            bluePatitasDataStore.data
                .map { preferences -> preferences[PreferenceKeys.Language] ?: AppLanguage.ENGLISH.tag }
                .first()
        }
        AppLocaleController.applyTag(savedLanguageTag)
    }
}
