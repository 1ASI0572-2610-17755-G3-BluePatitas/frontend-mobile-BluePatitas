# Architecture

BluePatitas uses a pragmatic Domain-Driven Design organization. The domain package contains the language of the product: sessions, roles, repositories and use cases. Feature packages remain thin until real workflows are implemented.

## Clean Architecture

The current layers are:

- `domain`: business models, repository contracts and use cases.
- `data`: DataStore-backed implementations, mock data and future mapper location.
- `app` and `feature`: Activity, app composition, ViewModels and Compose UI.
- `core`: shared design system, navigation, persistence setup, notifications and utilities.

The dependency direction is inward. Presentation depends on domain use cases. Data implements domain interfaces. Domain does not depend on Android UI.

## MVVM Flow

The intended flow is:

```text
Composable -> ViewModel -> Use Case -> Repository
```

Composables render state and forward events. ViewModels collect StateFlow streams and call use cases. Use cases coordinate domain actions. Repositories hide DataStore today and will hide REST or local database details later.

## Simulated Data And Future API

`SessionRepository` is currently implemented by `DataStoreSessionRepository`. Demo accounts live in `data/mock`. When the backend exists, new API data sources can be added without changing Composables. Hilt bindings should swap the implementation behind the same repository contracts.

Room is intentionally deferred. It should be introduced when the Animals module defines entities, local caching and synchronization requirements.

## Role-Based Navigation

Navigation is derived from the active `UserRole`.

- `SHELTER_ADMIN`: Home, Animals, Monitoring, Alerts, More.
- `VETERINARIAN`: Home, Animals, Monitoring, Alerts, Profile.

Routes use separate prefixes (`admin_*` and `vet_*`) so role-specific navigation stacks are not mixed. Signing out clears the session and returns to the development-only demo access screen.

## Internationalization

English is the default language. Spanish Latin America resources are stored in `values-b+es+419`. The language selection is persisted in DataStore and applied through Compose resource configuration for this prototype. If future requirements need OS-level per-app language integration, add platform-specific handling through Android locale APIs while keeping strings in Android resources.
