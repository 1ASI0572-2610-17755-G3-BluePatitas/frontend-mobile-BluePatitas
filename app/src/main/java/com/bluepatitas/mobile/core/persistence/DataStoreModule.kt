package com.bluepatitas.mobile.core.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.bluePatitasDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bluepatitas_preferences"
)
