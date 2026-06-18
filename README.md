# BluePatitas Android

BluePatitas is a native Android app foundation for an IoT shelter management product. The current iteration includes real backend sign-in and veterinary read endpoints, while Register and shelter onboarding remain visual/mock until the full admin registration flow is defined.

## Stack

- Kotlin
- Single Android app module: `app`
- Jetpack Compose with Material 3
- Navigation Compose
- MVVM with StateFlow
- Hilt dependency injection
- DataStore Preferences
- Retrofit + OkHttp
- CameraX phone camera preview
- Local Android notifications
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

Real sign-in uses the local backend at `http://10.0.2.2:8080/`. Administrator sessions route from the backend response: completed onboarding goes to administrator navigation, otherwise the app continues shelter onboarding. New administrator accounts created from Register still use the mock path and complete shelter onboarding:

```text
Basic information -> Location -> Confirmation -> Administrator navigation
```

Signing out clears the persisted session and returns to Welcome. Existing shelter draft/profile data remains local for the current onboarding prototype.

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

## Backend Integration

`RealAuthRepository` posts login credentials to `POST /api/v1/authentication/sign-in`. The returned token, user identity, role, shelter id/name and onboarding flag are persisted in DataStore. `BearerAuthInterceptor` reads the token from DataStore and adds `Authorization: Bearer <token>` to protected requests.

Veterinarian Home consumes `GET /api/veterinary/me/dashboard`. Veterinarian Animals consumes `GET /api/veterinary/me/animals`. Both screens show loading and connection error states with retry.

Admin and veterinarian navigation now share presentable Home, Animals, Monitoring, Alerts and Profile experiences. Monitoring consumes backend zones and alerts, and falls back to clearly labeled presentation data when an endpoint is unavailable. The zone detail view can activate the phone camera through CameraX, simulate safe-zone breaches, and create local in-app alerts plus Android local notifications through the `bluepatitas_alerts` channel.

## Mocked Behavior

Register administrator, veterinarian invitation and shelter onboarding are intentionally still mock/local in this phase. `FakeAuthRepository` remains available for those flows and for the explicit development access screen. There is still no Firebase, maps, camera streaming or real email delivery.

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
|   |-- remote
|   |-- repository
|   `-- mapper
`-- feature
    |-- developer
    |-- auth
    |-- onboarding
    |-- veterinary
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

Diet, full device management, remote video streaming, Firebase Push and advanced reports are intentionally deferred. Veterinary dashboard and shelter animals are read-only backend integrations. Monitoring is functional with backend zones/alerts, phone camera preview and controlled geofence simulation. Room is still deferred until the Animals module defines real local entities and synchronization needs.

## Future API Replacement

Keep presentation code calling ViewModels and use cases. Future backend work should connect Register/sign-up only after defining the complete flow: create admin -> sign in -> create shelter/onboarding -> dashboard. Firebase Push can replace the current local notification path once the backend emits real push events.
