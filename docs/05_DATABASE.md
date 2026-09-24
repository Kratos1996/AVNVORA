# AYNVORA Database Foundation & Room KMP Specification
Version: 1.0 (Phase 7.0)

## 1. Overview & Core Rules

The local database serves as the authoritative, offline-first persistence foundation for AYNVORA.
In accordance with Rule 26 and Rule 29 of `00_MASTER_RULES.md`:

- **Clean Architecture**: Room KMP entities, DAOs, and database classes live strictly in `:aynvora-data`. Domain interfaces live in `:aynvora-core`.
- **Local Source of Truth**: UI and ViewModels observe local state via Kotlin coroutines `Flow` from Room DAOs. Network is never consumed directly by the presentation layer.
- **Offline-First**: All read operations succeed immediately without network access.
- **Zero Sensitive Data**: No personal data (PII), raw birth charts, religious beliefs, or analytics event tables are stored in Room.

---

## 2. Technology Stack & Compatibility

- **Library**: AndroidX Room KMP 2.8.5 (`androidx.room:room-runtime`, `androidx.room:room-compiler`)
- **SQLite Engine**: Bundled SQLite (`androidx.sqlite:sqlite-bundled`)
- **Code Generation**: KSP (Kotlin Symbol Processing) targeting JVM, Android, iOS (Arm64 & SimulatorArm64)
- **Database Name**: `aynvora.db`
- **Schema Version**: 1 (baseline)

---

## 3. Database Schema & Tables

### 3.1 `user_profiles`
Stores user identity records.
- `id` (TEXT, PK): Unique stable identifier.
- `displayName` (TEXT): Profile name.
- `contextNotes` (TEXT, nullable): Optional non-sensitive context.
- `createdAtEpochMs` (INTEGER): Creation timestamp.
- `updatedAtEpochMs` (INTEGER): Last modified timestamp.

### 3.2 `birth_profiles`
Reusable birth data records for astrological calculations.
- `id` (TEXT, PK): Unique stable identifier.
- `name` (TEXT): Subject name.
- `year`, `month`, `day`, `hour`, `minute`, `second` (INTEGER): Birth datetime components.
- `placeName` (TEXT): Name of birth place.
- `latitude`, `longitude` (REAL): Coordinates for ephemeris calculations.
- `timezoneId` (TEXT): IANA timezone identifier (e.g., `"Asia/Kolkata"`).
- `country`, `placeId`, `notes` (TEXT, nullable): Supplementary metadata.
- `createdAtEpochMs`, `updatedAtEpochMs` (INTEGER): Timestamps.

### 3.3 `saved_charts`
Persisted chart calculation cache for instant offline retrieval.
- `id` (TEXT, PK): Unique chart identifier.
- `birthProfileId` (TEXT): Reference to `birth_profiles.id` (Indexed).
- `calculationProfile`, `ayanamsa`, `houseSystem` (TEXT): Engine settings used.
- `engineVersion` (TEXT): Version of `:astro-engine` used.
- `calculationTimestampEpochMs` (INTEGER): Timestamp of calculation.
- `schemaVersion` (INTEGER): Result schema format version.
- `status` (TEXT): Calculation outcome status (`SUCCESS`, `ERROR`, etc.).
- `cachedResultJson` (TEXT, nullable): Serialized calculation result payload.

### 3.4 `user_preferences`
App configuration, theme, and default astrological conventions.
- `id` (TEXT, PK): Default `"default_preferences"`.
- `theme` (TEXT): Active theme (`"DARK"`, `"LIGHT"`, `"SYSTEM"`).
- `languageCode` (TEXT): Canonical locale identifier (e.g. `"en"`, `"hi"`).
- `defaultCalculationProfile`, `defaultAyanamsa`, `defaultHouseSystem` (TEXT): Defaults.

### 3.5 `content_packs`
Downloaded knowledge packs (astrological texts, references).
- `packId` (TEXT, PK): Pack identifier.
- `moduleId` (TEXT): Module domain (`"ASTROLOGY"`). Indexed.
- `contentVersion` (INTEGER): Incremental integer version.
- `language` (TEXT): Locale code. Indexed.
- `title`, `description` (TEXT): Editorial information.
- `sourceAttribution` (TEXT): Canonical textual source reference.
- `trustState` (TEXT): `ContentTrustState` (`"VERIFIED"`, `"APPROVED_FOR_PUBLICATION"`, etc.).
- `checksumSha256` (TEXT): 64-char SHA-256 hash.
- `installedAtEpochMs`, `lastSyncEpochMs` (INTEGER): Timestamps.

### 3.6 `content_items`
Structured knowledge units belonging to a content pack.
- `id` (TEXT, PK): Stable item identifier.
- `packId` (TEXT): Reference to parent content pack (Indexed).
- `moduleId` (TEXT): Module domain (Indexed).
- `itemKey` (TEXT): Internal unique key within the pack (Unique composite index on `[packId, itemKey]`).
- `title`, `subtitle`, `body` (TEXT): Structured text content.
- `language` (TEXT): Locale code (Indexed).
- `metadataJson` (TEXT, nullable): Structured JSON attributes.
- `orderIndex` (INTEGER): Display sequence index.
- `tagsCsv` (TEXT): Comma-separated search/classification tags.
- `trustState` (TEXT): Trust status (`"APPROVED_FOR_PUBLICATION"`).

### 3.7 `content_sync_metadata`
Tracks synchronization cycles, versions, and validation state.
- `id` (TEXT, PK): Default `"global_sync_state"`.
- `lastSyncTimestampEpochMs` (INTEGER): Timestamp of last synchronization.
- `lastSyncStatus` (TEXT): Sync status (`"COMPLETED"`, `"FAILED"`, `"NO_OP"`).
- `lastSyncError` (TEXT, nullable): Sanitized error code (no sensitive server details).
- `activeManifestVersion` (INTEGER): Currently active manifest version.
- `etag` (TEXT, nullable): Remote manifest caching token.

---

## 4. Platform Initialization Contract

Room database construction is platform-specific and instantiated at the host application boundary:

- **Android (`androidApp`)**:
  ```kotlin
  val db = Room.databaseBuilder(
      context.applicationContext,
      AynvoraDatabase::class.java,
      AynvoraDatabase.DATABASE_NAME
  ).setDriver(BundledSQLiteDriver()).build()
  ```
- **JVM Desktop / Tests**:
  Constructed via `Room.inMemoryDatabaseBuilder` or file-based builder with `BundledSQLiteDriver`.

Shared code (`:aynvora-core`, `:aynvora-data`) receives the initialized `AynvoraDatabase` instance via `AynvoraDataFactory.createWithRoom()`.
