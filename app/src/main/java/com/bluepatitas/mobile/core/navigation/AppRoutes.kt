package com.bluepatitas.mobile.core.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Welcome : AppRoute("welcome")
    data object DeveloperEntry : AppRoute("developer-entry")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object ForgotPassword : AppRoute("forgot-password")
    data object Invitation : AppRoute("invitation")
    data object ShelterGraph : AppRoute("shelter-onboarding")
    data object ShelterBasicInfo : AppRoute("shelter-basic-info")
    data object ShelterLocation : AppRoute("shelter-location")
    data object ShelterConfirmation : AppRoute("shelter-confirmation")
    data object AdminMain : AppRoute("admin-main")
    data object VeterinarianMain : AppRoute("veterinarian-main")
}
