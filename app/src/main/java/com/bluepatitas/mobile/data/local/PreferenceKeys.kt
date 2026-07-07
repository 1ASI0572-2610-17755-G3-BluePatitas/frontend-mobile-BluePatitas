package com.bluepatitas.mobile.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val UserId = stringPreferencesKey("session_user_id")
    val FirstName = stringPreferencesKey("session_first_name")
    val LastName = stringPreferencesKey("session_last_name")
    val DisplayName = stringPreferencesKey("session_display_name")
    val Email = stringPreferencesKey("session_email")
    val Token = stringPreferencesKey("session_token")
    val Role = stringPreferencesKey("session_role")
    val ShelterId = stringPreferencesKey("session_shelter_id")
    val ShelterSessionName = stringPreferencesKey("session_shelter_name")
    val OnboardingCompleted = booleanPreferencesKey("session_onboarding_completed")
    val Language = stringPreferencesKey("language")
    val DemoMode = booleanPreferencesKey("demo_mode")
    val ShelterCreated = booleanPreferencesKey("shelter_created")
    val ShelterName = stringPreferencesKey("shelter_name")
    val ShelterTaxId = stringPreferencesKey("shelter_tax_id")
    val ShelterEmail = stringPreferencesKey("shelter_email")
    val ShelterPhone = stringPreferencesKey("shelter_phone")
    val ShelterAddress = stringPreferencesKey("shelter_address")
    val ShelterReference = stringPreferencesKey("shelter_reference")
    val ShelterDistrict = stringPreferencesKey("shelter_district")
    val ShelterCity = stringPreferencesKey("shelter_city")
    val EdgeGatewayUrl = stringPreferencesKey("edge_gateway_url")
}
