# BluePatitas Android

BluePatitas is a native Android app foundation for an IoT shelter management product. The current iteration includes the initial access and onboarding flow with mocked repositories, while keeping the architecture ready for a future REST API.

## Stack

- Kotlin
- Single Android app module: `app`
- Jetpack Compose with Material 3
- Navigation Compose
- MVVM with StateFlow
- Hilt dependency injection
- DataStore Preferences
- AppCompat per-app locales for runtime language changes
- Kotlin DSL and Gradle version catalog
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`

## Open In Android Studio

Open the root folder `frontend-mobile-bluepatitas` directly in Android Studio. The project root contains `settings.gradle.kts`, root `build.gradle.kts`, `gradle/`, `gradlew` and `gradlew.bat`.

## Build

```powershell
.\gradlew.bat clean assembleDebug test
.\gradlew.bat lintDebug
```

## Current Flow

Fresh install:

```text
Splash -> Welcome
```

Existing session:

```text
Splash -> role navigation
```

The demo administrator credentials represent an existing shelter administrator and go directly to administrator navigation. New administrator accounts created from Register still complete shelter onboarding:

```text
Basic information -> Location -> Confirmation -> Administrator navigation
```

Signing out clears only the session and returns to Welcome. Demo shelter data is kept so a later administrator login can go directly to the administrator navigation.

## Demo Credentials

Administrator:

```text
admin@bluepatitas.com
password: admin123
role: SHELTER_ADMIN
```

Veterinarian:

```text
vet@bluepatitas.com
password: vet123
role: VETERINARIAN
```

Veterinarian invitation:

```text
VET-BP-2026
```

## Implemented Screens

- Splash
- Welcome
- Login
- Register administrator
- Forgot password
- Accept veterinarian invitation
- Shelter onboarding step 1: basic information
- Shelter onboarding step 2: location
- Shelter creation confirmation
- Development access screen, now secondary from Welcome
- Role-based placeholder navigation

## Mocked Behavior

There is still no backend, Firebase, maps, camera streaming or real email delivery. `FakeAuthRepository` validates the demo credentials and invitation code, while DataStore simulates persisted `role`, `shelterId` and shelter creation state. In the current mock flow, demo administrator credentials represent an existing shelter admin and navigate directly to the admin area. New admin accounts created from Register still complete the shelter onboarding flow. Once the Backend is available, this decision will be based on the login response fields such as role, shelterId and onboardingCompleted.

Resumen en español: en el flujo mock actual, las credenciales demo de administrador representan a un administrador existente con refugio y entran directamente al área admin. Las cuentas nuevas creadas desde Register todavía completan el onboarding de refugio. Cuando exista Backend real, esta decisión vendrá desde la respuesta de login con campos como role, shelterId y onboardingCompleted.

## Project Structure

```text
app/src/main/java/com/bluepatitas/mobile
|-- app
|-- core
|   |-- common
|   |-- designsystem
|   |-- navigation
|   |-- persistence
|   |-- notifications
|   `-- util
|-- domain
|   |-- model
|   |-- repository
|   `-- usecase
|-- data
|   |-- local
|   |-- mock
|   |-- repository
|   `-- mapper
`-- feature
    |-- developer
    |-- auth
    |-- onboarding
    |-- dashboard
    |-- animals
    |-- monitoring
    |-- alerts
    |-- feeding
    |-- devices
    |-- veterinarians
    `-- settings
```

## Not Implemented Yet

Real Dashboard, Animals, Diet, Geofence, Alerts, Monitoring and Devices screens are intentionally deferred. Room is still deferred until the Animals module defines real local entities and synchronization needs.

## Future API Replacement

Keep presentation code calling ViewModels and use cases. Add API data sources under `data`, map DTOs through `data/mapper`, and bind API-backed implementations for `AuthRepository`, `SessionRepository` and `ShelterRepository` in Hilt.
