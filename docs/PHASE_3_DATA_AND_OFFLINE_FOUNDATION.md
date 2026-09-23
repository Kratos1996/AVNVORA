# Phase 3: Data Contracts + Offline-First Persistence Foundation

## 1. Phase Objective
Establish the data architecture and offline-first persistence foundation for the AYNVORA SDK. This encompasses clean domain contracts, repository boundaries, storage abstractions, atomic persistence, schema versioning, non-destructive migrations, privacy enforcement, and platform-independent offline behavior, without implementing product screens or astrology calculation formulas.

---

## 2. Data Architecture
The data architecture follows a strict layered pattern:
```
UI / Consumer Application
          ↓
Public SDK Boundary (:aynvora-core)
  - Domain Models
  - Repository Interfaces
  - AynvoraResult Model
          ↓
Implementation Layer (:aynvora-data)
  - Repository Implementations
  - Explicit Bidirectional Mappers
  - Internal Storage Entities
  - AynvoraStorageEngine (Mutex-Synchronized ACID Store)
  - StorageMigration & MigrationRunner
  - StorageCipher Abstraction
          ↓
Low-Level Drivers
  - InMemoryStorageDriver (Ephemeral / Unit Tests)
  - JvmFileStorageDriver (Desktop / Atomic File I/O)
```
Astrology calculations remain fully decoupled: `:astro-engine` has zero knowledge of persistence or database entities.

---

## 3. Domain Models
All domain models are located in `com.aynvora.core.models` and are completely decoupled from storage entities:
- `UserProfile`: Stable ID, display name, optional context notes, creation and updated epoch timestamps.
- `BirthProfile`: Reusable profile referencing validated `BirthData` (`BirthDate`, `BirthTime`, `BirthPlace`), notes, and epoch timestamps.
- `BirthPlace`: Validated geographic birthplace with latitude (-90..90), longitude (-180..180), IANA timezone ID, name, optional country, and optional place ID.
- `SavedChart`: Persisted chart metadata and cache reference containing `birthProfileId`, `CalculationConfig`, engine version, schema version, calculation timestamp, status (`PENDING`, `COMPLETED`, `FAILED`), and optional cached result JSON.
- `CalculationConfig`: Existing public calculation configuration model specifying `CalculationProfile`, `AyanamsaConvention`, and `HouseSystem`.
- `UserPreferences`: Platform-independent preferences specifying `ThemePreference` (`SYSTEM`, `LIGHT`, `DARK`), language code, and default calculation profile/ayanamsa/house system.

---

## 4. Repository Contracts
Defined in `com.aynvora.core.repository`:
- `UserProfileRepository`: `getProfile(id)`, `getAllProfiles()`, `saveProfile(profile)`, `deleteProfile(id)`, `observeProfile(id)`.
- `BirthProfileRepository`: `getBirthProfile(id)`, `getAllBirthProfiles()`, `saveBirthProfile(profile)`, `deleteBirthProfile(id)`, `observeAllBirthProfiles()`, `observeBirthProfile(id)`.
- `SavedChartRepository`: `getSavedChart(id)`, `getChartsForBirthProfile(birthProfileId)`, `getAllSavedCharts()`, `saveChart(chart)`, `deleteChart(id)`, `observeChartsForBirthProfile(birthProfileId)`.
- `UserPreferencesRepository`: `getPreferences()`, `updatePreferences(preferences)`, `observePreferences()`.

---

## 5. Storage Architecture
The persistence engine (`AynvoraStorageEngine`) coordinates:
1. **Thread-Safety & ACID Concurrency**: Coroutine `Mutex` prevents concurrent read-modify-write race conditions.
2. **Atomic Writes**: Persistent file drivers use temporary swap files (`.tmp`) followed by atomic rename operations to prevent file corruption during sudden crashes or power loss.
3. **Pluggable Storage Drivers**: Storage driver contract (`StorageDriver`) decouples the engine from the underlying disk or in-memory backing.
4. **Caching**: In-memory container caching avoids repeated disk reads and unnecessary JSON parsing on subsequent reads.

---

## 6. Selected Persistence Technology
**Multiplatform Atomic Structured Storage Engine (`AynvoraStorageEngine`)** backed by `kotlinx.serialization.json` and pluggable `StorageDriver` implementations (`InMemoryStorageDriver` and `JvmFileStorageDriver`).

---

## 7. Why It Was Selected
- **Universal Multiplatform Support**: Pure Kotlin Multiplatform code with zero C/C++ native runtime dependencies or SQLite C-interop linker dependencies that fail on developer machines lacking Xcode.
- **Compiler Compatibility**: Eliminates external KSP/SQLDelight compiler plugin incompatibilities with Kotlin 2.4.x.
- **ACID-like Atomic Writes**: Atomic swap file writes guarantee zero partial-write corruption on sudden power loss.
- **Deterministic Schema Versioning & Migrations**: Step-by-step non-destructive migrations are handled deterministically in pure Kotlin.
- **Strict Replaceability**: The repository boundary decouples the UI from the storage implementation; the database engine can be upgraded without modifying a single UI or domain contract.

---

## 8. Module Structure
- `:aynvora-core`: Public SDK facade, domain models, repository interfaces, `AynvoraResult` error models. Zero database dependencies.
- `:aynvora-data`: Internal persistence module containing entities, mappers, repository implementations, storage engine, migration runner, and storage drivers.
- `:ui`: Presentation layer depending only on `:aynvora-core` and `:design-system`. Has zero dependency on `:aynvora-data`.
- `:astro-engine`: Calculation engine with zero persistence dependencies.

---

## 9. Serialization Strategy
- `@Serializable` is applied to public value objects in `:aynvora-core` to enable platform-independent serialization across process boundaries.
- `kotlinx.serialization.json` is used internally in `:aynvora-data` for encoding storage entities and verifying JSON integrity.
- Unknown JSON keys are safely ignored (`ignoreUnknownKeys = true`) to enable smooth forward schema evolution.

---

## 10. Schema Version
- Current schema version: `1`.
- Tracked in `StorageContainerEntity.schemaVersion` and validated on every cold load.

---

## 11. Migration Strategy
- Coordinated by `MigrationRunner`.
- Steps implement `StorageMigration(fromVersion, toVersion, migrate(json))`.
- Sequential forward migrations are executed iteratively (`v1 -> v2 -> v3`).
- Schema downgrades (`storedVersion > targetVersion`) are rejected with `AynvoraResult.Failure.MigrationFailure`.
- Missing migration steps fail deterministically without data corruption.

---

## 12. Offline-First Behavior
- 100% offline operation: All profile, chart, and preference CRUD operations run purely against local storage.
- No network connection is ever initiated or required for data operations.
- Astrological calculation engine runs independently against local reference data.

---

## 13. Security Boundaries
- **No Credentials at Rest**: Stored entities strictly forbid passwords, auth tokens, API keys, or private signing keys.
- **Encryption Abstraction**: `StorageCipher` interface supports seamless integration with platform-native keystores (Android Keystore, Apple Keychain, JVM Keystore).
- **No Path Leakage**: Internal file paths and OS exceptions are sanitized into `AynvoraResult.Failure` categories before reaching calling layers.

---

## 14. Privacy Considerations
- Data minimization is strictly enforced: `UserProfile` only contains an ID, display name, optional context notes, and timestamps.
- No location tracking or network geocoding: `BirthPlace` operates purely with user-supplied coordinates and timezone IDs.

---

## 15. Mapping Strategy
Explicit bidirectional mappers in `com.aynvora.data.mapper.Mappers.kt`:
- `UserProfile.toEntity()` / `UserProfileEntity.toDomain()`
- `BirthProfile.toEntity()` / `BirthProfileEntity.toDomain()`
- `SavedChart.toEntity()` / `SavedChartEntity.toDomain()`
- `UserPreferences.toEntity()` / `UserPreferencesEntity.toDomain()`
Reflection-heavy magic mappings are avoided.

---

## 16. Error Handling
Persistence errors are encapsulated in `AynvoraResult.Failure`:
- `NotFound(resourceId, message)`
- `StorageFailure(operation, message)`
- `CorruptedData(resourceId, message)`
- `MigrationFailure(fromVersion, toVersion, message)`
- `InternalFailure(message)`

---

## 17. Test Strategy
1. **Domain Tests** (`aynvora-core:jvmTest`): Validation rules for `UserProfile`, `BirthProfile`, `BirthPlace`, `SavedChart`, and `UserPreferences`.
2. **Mapping Tests** (`aynvora-data:jvmTest`): Bidirectional domain <-> entity transformations, enum conversions.
3. **Repository Tests** (`aynvora-data:jvmTest`): Full CRUD lifecycle, filtering by `birthProfileId`, Flow observation emissions, `NotFound` handling.
4. **Storage Engine Tests** (`aynvora-data:jvmTest`): Offline initialization, corrupted payload handling, schema downgrade rejection, sequential migration execution, and cipher integration.
5. **Driver Tests** (`aynvora-data:jvmTest`): File-backed atomic writes and deletion.

---

## 18. Performance Considerations
- Mutex-synchronized memory caching eliminates redundant disk I/O and JSON parsing for read operations.
- Atomic swap files eliminate costly file lock waits.
- Flow emissions utilize buffered shared flows (`MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)`) to prevent backpressure blocking.

---

## 19. Files Added
- `aynvora-data/build.gradle.kts`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/UserProfile.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/BirthProfile.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/SavedChart.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/UserPreferences.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/repository/Repositories.kt`
- `aynvora-core/src/commonTest/kotlin/com/aynvora/core/DomainContractsTest.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/entity/Entities.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/mapper/Mappers.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/security/StorageCipher.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/storage/StorageDriver.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/storage/StorageMigration.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/storage/AynvoraStorageEngine.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/repository/RepositoryImplementations.kt`
- `aynvora-data/src/commonMain/kotlin/com/aynvora/data/AynvoraDataFactory.kt`
- `aynvora-data/src/commonTest/kotlin/com/aynvora/data/MappersTest.kt`
- `aynvora-data/src/commonTest/kotlin/com/aynvora/data/RepositoryTest.kt`
- `aynvora-data/src/commonTest/kotlin/com/aynvora/data/StorageEngineTest.kt`
- `aynvora-data/src/jvmMain/kotlin/com/aynvora/data/storage/JvmFileStorageDriver.kt`
- `aynvora-data/src/jvmTest/kotlin/com/aynvora/data/storage/JvmFileStorageDriverTest.kt`
- `docs/PHASE_3_DATA_AND_OFFLINE_FOUNDATION.md`

---

## 20. Files Changed
- `settings.gradle.kts`: Included `:aynvora-data`.
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/result/AynvoraResult.kt`: Added `NotFound`, `StorageFailure`, `CorruptedData`, `MigrationFailure`.
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/BirthData.kt`: Added optional `country` and `id` to `BirthPlace`.
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/AynvoraSdk.kt`: Added repository accessors and updated factory.
- `docs/10_DATA_ARCHITECTURE.md`: Documented implemented architecture and layer structure.
- `docs/11_OFFLINE_FIRST.md`: Documented implemented offline-first behavior.
- `docs/12_SECURITY.md`: Documented storage and persistence security boundaries.

---

## 21. Dependencies Added
- None to root or external dependencies. All modules reuse existing libraries (`kotlinx-coroutines-core`, `kotlinx-serialization-json`).

---

## 22. Dependencies Removed
- None.

---

## 23. Verification Commands
```bash
./gradlew :aynvora-core:jvmTest
./gradlew :astro-engine:jvmTest
./gradlew :design-system:jvmTest
./gradlew :aynvora-data:jvmTest
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:packageDistributionForCurrentOS
./gradlew test
```

---

## 24. Verification Results
- All unit and integration tests passed across all modules (100% success).
- Android application debug APK assembled successfully (`BUILD SUCCESSFUL in 8s`).
- Desktop distribution packaged successfully (`BUILD SUCCESSFUL in 1s`).
- Zero direct dependencies from `:ui` or `:aynvora-core` to `:aynvora-data` or database entities.
- Zero dependencies from `:astro-engine` to persistence.

---

## 25. Known Limitations
- Full iOS binary framework linking (`linkDebugTestIosSimulatorArm64` / `linkReleaseFrameworkIosArm64`) is not executable in environments without macOS Xcode installed. Multiplatform Kotlin compilation for iOS (`compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`) succeeds.

---

## 26. Deferred Synchronization Work
- Remote cloud data synchronization, conflict resolution policies, and multi-device account sync are deliberately deferred to future cloud/sync phases.

---

## 27. Deferred Reference-App Work
- Consumer application screens (onboarding, birth input, chart viewers, dashboard, navigation graph) are deliberately deferred to Phase 4.

---

## 28. Next Recommended Phase
**Phase 4: Reference Application Screens & Workflows** (Onboarding, Birth Profile creation, Dashboard, and SDK integration).
