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

If an administrator signs in before creating a shelter, the app routes to shelter onboarding:

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

There is still no backend, Firebase, maps, camera streaming or real email delivery. `FakeAuthRepository` validates the demo credentials and invitation code. `DataStoreShelterRepository` stores the demo shelter locally. These implementations can be replaced by API-backed repositories without changing the Compose screens.

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
