# AYNVORA Data Architecture
Version: 1.1 (Phase 3 Foundation)

## Principles
- **Offline-First**: Core calculations, profile management, and chart persistence operate completely offline without network requirements.
- **Strict Separation of Concerns**: Public SDK facade and domain models are decoupled from database and storage entities.
- **Data Integrity & Versioning**: Persisted container schemas are explicitly versioned with non-destructive migrations.
- **Privacy by Design**: Collect only strictly justified personal fields. Zero credentials, passwords, or authentication tokens stored in profile persistence.
- **Deterministic Storage & Calculation**: Storage contains configuration and cache references; astrological formulas are never duplicated in the persistence layer.

## Architectural Layers
```
┌─────────────────────────────────────────────────────────────┐
│                      UI / Application                       │
└──────────────────────────────┬──────────────────────────────┘
                               │ depends on
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 :aynvora-core (Public SDK)                  │
│  - Domain Models (UserProfile, BirthProfile, SavedChart)    │
│  - Repository Interfaces (UserProfileRepository, etc.)      │
│  - Result Model (AynvoraResult with deterministic failures)  │
└──────────────────────────────▲──────────────────────────────┘
                               │ implements contracts
┌──────────────────────────────┴──────────────────────────────┐
│                 :aynvora-data (Internal)                    │
│  - Repository Implementations                               │
│  - Bidirectional Mappers (Domain <-> Internal Entities)     │
│  - Storage Entities (UserProfileEntity, ContainerEntity)    │
│  - AynvoraStorageEngine (ACID-like Atomic Mutex Store)      │
│  - StorageMigration & MigrationRunner                       │
│  - StorageCipher & NoOpStorageCipher                        │
│  - StorageDriver (InMemoryStorageDriver, File Drivers)      │
└─────────────────────────────────────────────────────────────┘
```

## Implemented Domain Contracts
1. **UserProfile**: Stable local ID, display name, optional context notes, creation and update timestamps.
2. **BirthProfile**: Reusable profile encapsulating validated `BirthData` (`BirthDate`, `BirthTime`, `BirthPlace`), notes, and timestamps.
3. **BirthPlace**: Validated coordinates (-90..90 lat, -180..180 lon), IANA timezone ID, name, optional country, and optional place ID.
4. **SavedChart**: Persistent chart cache metadata referencing `BirthProfile`, `CalculationConfig`, engine version, schema version, status, and serialized result payload.
5. **CalculationConfig**: Deterministic engine configuration specifying `CalculationProfile`, `AyanamsaConvention`, and `HouseSystem`.
6. **UserPreferences**: Platform-independent settings for `ThemePreference`, language code, and default calculation profile/ayanamsa/house system.

## Storage Technology & Architecture
- **Engine**: Multiplatform Atomic Structured Storage Engine (`AynvoraStorageEngine`).
- **Driver Abstraction**: Pluggable `StorageDriver` enabling in-memory storage for test/preview and atomic file drivers (`JvmFileStorageDriver`) for persistent targets.
- **Concurrency & Atomicity**: Coroutine `Mutex` serialization guarantees single-writer safety and prevents race conditions. File writes use temporary swap files (`.tmp`) followed by atomic rename to eliminate partial write corruption.
- **Schema Evolution**: Schema Version 1 baseline with automated `MigrationRunner` ensuring sequential forward migrations and prohibiting unsupported schema downgrades.
- **Security & Encryption**: Payload-level `StorageCipher` interface enables seamless binding to platform-secure keystores/keychains without leaking crypto specifics.
