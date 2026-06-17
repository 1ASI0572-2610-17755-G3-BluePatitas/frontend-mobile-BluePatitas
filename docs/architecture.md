# Architecture

BluePatitas uses a pragmatic Domain-Driven Design organization with Clean Architecture boundaries.

## Layers

- `domain`: business models, repository contracts and use cases.
- `data`: mocked auth, DataStore-backed session/preferences/shelter persistence and future mapper location.
- `app`: root app state, locale handling and root navigation composition.
- `feature`: Compose screens and ViewModels for developer access, auth and onboarding.
- `core`: shared design system, navigation routes, persistence setup, notifications and utilities.

The dependency direction remains inward. Presentation depends on use cases and domain models. Data implements domain repository contracts. Domain does not depend on Compose or Android UI.

## MVVM Flow

The expected flow is:

```text
Composable -> ViewModel -> Use Case -> Repository
```

Composables render state and send events. ViewModels expose StateFlow and run validation/orchestration. Use cases call repository contracts. Repositories hide DataStore and mock behavior today, and can hide REST or local database details later.

## Root Navigation

The root graph starts at `splash`.

```text
splash
welcome
developer-entry
login
register
forgot-password
invitation
shelter-onboarding
admin-main
veterinarian-main
```

Splash waits for persisted DataStore state. If there is no session, it routes to Welcome. If there is a veterinarian session, it routes to veterinarian navigation. If there is an administrator session and a shelter exists, it routes to administrator navigation; otherwise it continues shelter onboarding.

Auth and onboarding routes are cleared from the back stack when the app enters a main role destination.

## Auth And Onboarding

`FakeAuthRepository` owns mock credential and invitation validation:

- `admin@bluepatitas.com` / `admin123`
- `vet@bluepatitas.com` / `vet123`
- `VET-BP-2026`

`DataStoreShelterRepository` persists whether the shelter was created and stores basic shelter data. Signing out clears session keys only; shelter data remains for the demo.

## Role-Based Navigation

`SHELTER_ADMIN`: Home, Animals, Monitoring, Alerts, More.

`VETERINARIAN`: Home, Animals, Monitoring, Alerts, Profile.

Routes keep role prefixes (`admin_*`, `vet_*`) so navigation stacks do not mix administrative and veterinarian destinations.

## Internationalization

English is the default language. Spanish Latin America resources are in `values-b+es+419`. Runtime language changes use official per-app locale APIs through AppCompat:

```text
AppCompatDelegate.setApplicationLocales(...)
LocaleListCompat.forLanguageTags("en" / "es-419")
```

The selected language is persisted in DataStore and applied during `Application.onCreate()` before Compose content is mounted.

## Future Backend

When the backend exists, add remote data sources and replace Hilt bindings behind `AuthRepository`, `SessionRepository` and `ShelterRepository`. The UI and ViewModels should continue depending on use cases rather than API clients.
