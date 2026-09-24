# AYNVORA Project Structure
Version: 1.0

## Repository
- apps/: host/reference applications
- modules/: reusable SDK modules
- design-system/: tokens/components/assets
- docs/: authoritative specifications
- build-logic/: convention plugins when justified
- gradle/: version catalog/wrapper
- scripts/: reproducible tooling
- security/: threat models and security test artifacts where appropriate

## KMP Architecture Layers
- `:ui`: Compose Multiplatform shared presentation layer
- `:aynvora-core`: Domain layer (domain models, repository contracts, SDK facade, analytics abstraction). Zero platform or database dependencies.
- `:astro-engine`: Deterministic calculation engine. Independent of UI, database, network, and analytics.
- `:aynvora-data`: Data layer (Room KMP entities, DAOs, database, StorageEngine, repository implementations).
- `:aynvora-localization`: Multiplatform localization tables and locale management.
- `androidApp`: Android application entry point (Firebase SDK setup, Room platform builder).
- `desktopApp`: JVM Desktop application entry point (NoOp analytics, file storage).
- `iosApp`: iOS native application host.

Dependency rule:
Presentation (`:ui`) → Domain (`:aynvora-core`) ← Data (`:aynvora-data`)
`:astro-engine` remains pure math/astronomy.

## Rule
Do not duplicate domain logic in androidMain, iosMain or desktopMain.
