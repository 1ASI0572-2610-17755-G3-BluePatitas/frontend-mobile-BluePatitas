package com.bluepatitas.mobile.data.mock

import com.bluepatitas.mobile.domain.model.AppSession
import com.bluepatitas.mobile.domain.model.UserRole

object DemoAccounts {
    fun sessionFor(role: UserRole): AppSession =
        when (role) {
            UserRole.SHELTER_ADMIN -> AppSession(
                userId = "demo-admin",
                displayName = "Marina Herrera",
                email = "admin@bluepatitas.com",
                role = UserRole.SHELTER_ADMIN,
                shelterId = "shelter-bluepatitas-demo"
            )

            UserRole.VETERINARIAN -> AppSession(
                userId = "demo-vet",
                displayName = "Elena Ramos",
                email = "vet@bluepatitas.com",
                role = UserRole.VETERINARIAN,
                shelterId = "shelter-bluepatitas-demo"
            )
        }
}
