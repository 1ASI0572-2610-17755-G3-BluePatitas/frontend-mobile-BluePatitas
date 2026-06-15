package com.bluepatitas.mobile.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val UserId = stringPreferencesKey("session_user_id")
    val DisplayName = stringPreferencesKey("session_display_name")
    val Email = stringPreferencesKey("session_email")
    val Role = stringPreferencesKey("session_role")
    val ShelterId = stringPreferencesKey("session_shelter_id")
    val Language = stringPreferencesKey("language")
    val DemoMode = booleanPreferencesKey("demo_mode")
}
