# AYNVORA — CANONICAL REPOSITORY & PACKAGE ARCHITECTURE

# Clean Architecture Physical Directory & Responsibility Map

## 1. Architectural Philosophy & Dependency Inversion

AYNVORA strictly enforces Clean Architecture across Kotlin Multiplatform targets (Android, JVM
Desktop, iOS).

```
         ┌────────────────────────────────────────────────────────┐
         │                  Presentation Layer                    │
         │             (:ui, androidApp, desktopApp)              │
         └───────────────────────────┬────────────────────────────┘
                                     │ depends inward
                                     ▼
         ┌────────────────────────────────────────────────────────┐
         │                      Domain Layer                      │
         │      (:aynvora-core, :astro-engine, contracts)         │
         │  - Domain Models (immutable, validated)                │
         │  - Repository Interfaces (contracts)                   │
         │  - Use Cases (business actions)                        │
         │  - Analytics Domain Abstraction                        │
         └───────────────────────────▲────────────────────────────┘
                                     │ implements contracts
                                     │
         ┌───────────────────────────┴────────────────────────────┐
         │                       Data Layer                       │
         │                    (:aynvora-data)                     │
         │  - Repository Implementations                          │
         │  - Room KMP Database, Entities & DAOs                  │
         │  - Storage Drivers, Ciphers, and Migrations            │
         │  - Local Starter Content & Synchronizers               │
         └────────────────────────────────────────────────────────┘
```

---

## 2. Module Inventory & Responsibilities

| Module                  | Responsibility Category            | Pure Kotlin?  | Dependencies Allowed                                     | Forbidden Dependencies                               |
|-------------------------|------------------------------------|---------------|----------------------------------------------------------|------------------------------------------------------|
| `:astro-engine`         | Pure Mathematical Calculation      | Yes           | Standard Math/Algorithms                                 | UI, Room, Firebase, Android, iOS, Network, Analytics |
| `:aynvora-core`         | Domain Layer & SDK Facade          | Yes           | `:astro-engine`, Coroutines, Serialization               | Room, Firebase, Android, iOS, UI, Desktop            |
| `:aynvora-data`         | Data Layer & Persistence           | Multiplatform | `:aynvora-core`, Room KMP, SQLite, Serialization         | Firebase, UI, Compose                                |
| `:aynvora-localization` | Localization & Locale Registry     | Yes           | Serialization                                            | Database, UI, Firebase                               |
| `:design-system`        | Multiplatform UI Tokens & Widgets  | Compose MP    | Compose Runtime, Material3, `:aynvora-localization`      | Room, Firebase, `:astro-engine`                      |
| `:ui`                   | Presentation Scaffold & Navigation | Compose MP    | `:design-system`, `:aynvora-core`, ViewModel Compose     | Room, Firebase SDK directly                          |
| `androidApp`            | Android Application Host           | Android       | `:ui`, `:aynvora-core`, Firebase Analytics & Crashlytics | None (Application Root)                              |
| `desktopApp`            | JVM Desktop Host                   | JVM           | `:ui`, `:aynvora-core`                                   | Android, iOS, Firebase SDK                           |
| `iosApp`                | iOS Native Host                    | Swift/iOS     | Frameworks from `:aynvora-core`, `:ui`                   | Android, JVM                                         |

---

## 3. Canonical Package Specification

### 3.1 Domain Layer (`:aynvora-core`)

- `com.aynvora.core`
    - `AynvoraSdk`: Main public SDK interface
    - `Aynvora`: Public SDK entry point factory
- `com.aynvora.core.models`
    - Immutable domain entities and value objects: `UserProfile`, `BirthProfile`, `SavedChart`,
      `UserPreferences`, `BirthData`, `CalculationConfig`, `ChartResult`, `ContentItem`,
      `ContentPack`, etc.
- `com.aynvora.core.repository`
    - Public repository contracts: `UserProfileRepository`, `BirthProfileRepository`,
      `SavedChartRepository`, `UserPreferencesRepository`, `ContentRepository`,
      `ContentSyncRepository`.
- `com.aynvora.core.usecase`
    - Atomic domain actions:
        - `SaveBirthProfileUseCase`, `ObserveBirthProfilesUseCase`, `DeleteBirthProfileUseCase`
        - `SaveChartUseCase`, `ObserveSavedChartsUseCase`, `DeleteChartUseCase`
        - `GetUserPreferencesUseCase`, `ObserveUserPreferencesUseCase`,
          `UpdateUserPreferencesUseCase`
- `com.aynvora.core.tarot`
    - Tarot domain feature boundary: `TarotCard`, `TarotDeck`, `TarotSpread`, `TarotReading`,
      `TarotCardContent`, `TarotDrawEngine`, `TarotRandomSource`, `TarotRepository`,
      `PerformTarotReadingUseCase`, `GetTarotCardContentUseCase`.
- `com.aynvora.core.analytics`
    - Platform-neutral analytics: `AnalyticsTracker`, `AnalyticsEvent`, `AnalyticsConsentManager`,
      `NoOpAnalyticsTracker`, `ConsentAwareAnalyticsTracker`.
- `com.aynvora.core.result`
    - Deterministic operation result: `AynvoraResult<T>`.
- `com.aynvora.core.sync`
    - Content package verification contracts: `ContentVerifier`, `ContentVerificationResult`.

### 3.2 Data Layer (`:aynvora-data`)

- `com.aynvora.data`
    - `AynvoraDataFactory`: Factory constructing repository bundles and Room wiring.
    - `AynvoraDataComponents`, `AynvoraRepositories`.
- `com.aynvora.data.repository`
    - Implementations: `UserProfileRepositoryImpl`, `BirthProfileRepositoryImpl`,
      `SavedChartRepositoryImpl`, `UserPreferencesRepositoryImpl`, `ContentRepositoryImpl`,
      `ContentSyncRepositoryImpl`.
- `com.aynvora.data.database`
    - `AynvoraDatabase`: Room KMP database definition.
- `com.aynvora.data.database.entity`
    - SQLite persistence entities: `UserProfileRoomEntity`, `BirthProfileRoomEntity`,
      `SavedChartRoomEntity`, `UserPreferencesRoomEntity`, `ContentPackRoomEntity`,
      `ContentItemRoomEntity`, `ContentSyncMetadataRoomEntity`, `TarotDeckRoomEntity`,
      `TarotCardRoomEntity`, `TarotCardContentRoomEntity`, `TarotReadingHistoryRoomEntity`.
- `com.aynvora.data.database.dao`
    - Room DAOs: `UserProfileDao`, `BirthProfileDao`, `SavedChartDao`, `UserPreferencesDao`,
      `ContentPackDao`, `ContentItemDao`, `ContentSyncMetadataDao`, `TarotDao`.
- `com.aynvora.data.tarot`
    - `TarotRepositoryImpl`: Room-backed Tarot persistence and synchronizer.
    - `TarotStarterContent`, `TarotCompleteContentPack`: Canonical 78-card bilingual content pack.
- `com.aynvora.data.storage`
    - Storage engine: `AynvoraStorageEngine`, `StorageDriver`, `StorageMigration`.
- `com.aynvora.data.security`
    - `StorageCipher`, `NoOpStorageCipher`.

### 3.3 Presentation Layer (`:ui`)

- `com.aynvora.ui`
    - Root app entry: `AynvoraApp`.
- `com.aynvora.ui.tarot`
    - Feature UI: `TarotScreen`.

---

## 4. Feature Isolation & Rules for Future Modules

1. **Astrology**: Core calculations live in `:astro-engine`. Core domain models live in
   `com.aynvora.core.models`. Repositories live in `:aynvora-data`.
2. **Tarot**: Decoupled feature domain. Domain in `com.aynvora.core.tarot`. Data in
   `com.aynvora.data.tarot`. UI in `com.aynvora.ui.tarot`. Zero dependency on `:astro-engine`.
3. **Future Features (Garud Puran, Tantra, Subscriptions, Profile)**:
    - Must define:
        - Domain models, contracts, and use cases in `com.aynvora.core.<feature>`.
        - Room entities, DAOs, and repository implementations in `com.aynvora.data.<feature>`.
        - Screens and view models in `com.aynvora.ui.<feature>`.
        - Localization strings in `com.aynvora.localization`.
        - Analytics events in `AnalyticsEvent`.
    - Never couple independent feature domains directly to each other.
