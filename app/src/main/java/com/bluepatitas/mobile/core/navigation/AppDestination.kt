package com.bluepatitas.mobile.core.navigation

import androidx.annotation.StringRes
import com.bluepatitas.mobile.R
import com.bluepatitas.mobile.domain.model.UserRole

data class AppDestination(
    val route: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int
)

fun destinationsFor(role: UserRole): List<AppDestination> =
    when (role) {
        UserRole.SHELTER_ADMIN -> listOf(
            AppDestination("admin_home", R.string.home, R.string.home_placeholder_admin),
            AppDestination("admin_animals", R.string.animals, R.string.animals_placeholder_admin),
            AppDestination("admin_monitoring", R.string.monitoring, R.string.monitoring_placeholder_admin),
            AppDestination("admin_alerts", R.string.alerts, R.string.alerts_placeholder_admin),
            AppDestination("admin_more", R.string.more, R.string.more_placeholder_admin)
        )

        UserRole.VETERINARIAN -> listOf(
            AppDestination("vet_home", R.string.home, R.string.home_placeholder_vet),
            AppDestination("vet_animals", R.string.animals, R.string.animals_placeholder_vet),
            AppDestination("vet_monitoring", R.string.monitoring, R.string.monitoring_placeholder_vet),
            AppDestination("vet_alerts", R.string.alerts, R.string.alerts_placeholder_vet),
            AppDestination("vet_profile", R.string.profile, R.string.profile_placeholder_vet)
        )
    }
