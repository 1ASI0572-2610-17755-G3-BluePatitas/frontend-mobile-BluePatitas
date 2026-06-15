# BluePatitas Android

BluePatitas is a native Android foundation for an IoT shelter management app. This iteration sets up the technical base only: architecture, theme, simulated session persistence, role-based demo navigation and reusable Compose components.

## Stack

- Kotlin
- Single Android app module: `app`
- Jetpack Compose with Material 3
- Navigation Compose
- MVVM with StateFlow
- Hilt dependency injection
- DataStore Preferences
- Kotlin DSL and Gradle version catalog
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`

## Open In Android Studio

Open the root folder `frontend-mobile-bluepatitas` directly in Android Studio. Do not open a nested project folder. The project contains `settings.gradle.kts`, root `build.gradle.kts`, `gradle/`, `gradlew` and `gradlew.bat` at the repository root.

If Android Studio asks for the SDK location, use the local Android SDK installed on this machine. This workspace includes `local.properties` for the current environment, but the file is ignored by Git.

## Build

```powershell
.\gradlew.bat clean assembleDebug test
```

Optional lint:

```powershell
.\gradlew.bat lintDebug
```

## Demo Sessions

The first screen is `Demo access`. It is not the final Login screen.

- `Enter as administrator` starts a simulated `SHELTER_ADMIN` session for Marina Herrera, `admin@bluepatitas.com`.
- `Enter as veterinarian` starts a simulated `VETERINARIAN` session for Elena Ramos, `vet@bluepatitas.com`.
- The selected language and simulated session are persisted with DataStore.
- `Sign out` clears the session and returns to demo access.

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

The final Login, Register, Dashboard, Animals, Diet, Geofence, Alerts, Monitoring and Devices screens are intentionally not implemented in this iteration. There is no Firebase, real maps, external API consumption, camera streaming or credentials.

Room is not configured yet because no persisted domain entities exist. It should be introduced when the Animals module defines real local entities and sync rules.

## Replacing Simulated Repositories

The domain layer already depends on repository interfaces. Future API work should add remote data sources under `data`, map DTOs to domain models through `data/mapper`, and bind API-backed repository implementations in Hilt. Presentation code should continue calling use cases, so the flow remains:

```text
Composable -> ViewModel -> Use Case -> Repository
```
