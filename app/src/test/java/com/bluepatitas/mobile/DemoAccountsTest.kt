package com.bluepatitas.mobile

import com.bluepatitas.mobile.data.mock.DemoAccounts
import com.bluepatitas.mobile.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoAccountsTest {
    @Test
    fun administratorDemoAccountMatchesExpectedRole() {
        val session = DemoAccounts.sessionFor(UserRole.SHELTER_ADMIN)

        assertEquals("admin@bluepatitas.com", session.email)
        assertEquals(UserRole.SHELTER_ADMIN, session.role)
    }

    @Test
    fun veterinarianDemoAccountMatchesExpectedRole() {
        val session = DemoAccounts.sessionFor(UserRole.VETERINARIAN)

        assertEquals("vet@bluepatitas.com", session.email)
        assertEquals(UserRole.VETERINARIAN, session.role)
    }
}
