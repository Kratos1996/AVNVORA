# PHASE 7.0 — CLEAN ARCHITECTURE + ROOM KMP + OFFLINE-FIRST + ANALYTICS FOUNDATION

Version: 1.0 (Phase 7.0)
Status: COMPLETE

---

## 1. Scope

Phase 7.0 delivers the AYNVORA platform foundation:

1. **Clean Architecture enforcement** — formal documentation of dependency direction
2. **Room KMP local persistence** — Room 2.8.5 with KSP, serving as local source of truth for content/sync
3. **Offline-first architecture** — content, preferences, and profiles all serve from local storage immediately
4. **Content sync foundation** — interface contracts, verification model, and sync state machine
5. **Firebase Analytics foundation** — `AnalyticsTracker`, `AnalyticsEvent`, `AnalyticsConsentManager` in domain layer
6. **Analytics migration** — all existing SDK flows, language changes, and content interactions instrumented
7. **Permanent governance rules** — rules 26–31 added to `00_MASTER_RULES.md`

---

## 2. Clean Architecture

### 2.1 Dependency Direction

```
androidApp / desktopApp (Presentation)
        │
        ▼
    :ui / :aynvora-localization
        │
        ▼
    :aynvora-core (Domain)           ◄── all repository interfaces live here
        ▲
        │
    :aynvora-data (Data)             ◄── repository implementations, Room DAOs, entities
        │
        ▼
    :astro-engine (Pure Calculation)  ◄── no Firebase, no Room, no Analytics
```

**Forbidden dependencies:**
- `:aynvora-core` → Firebase ✗
- `:aynvora-core` → Room ✗
- `:aynvora-core` → Android platform ✗
- `:astro-engine` → anything but stdlib/math ✗
- Firebase SDK → `:aynvora-core` ✗

### 2.2 Module Responsibilities

| Module | Responsibility |
|--------|---------------|
| `:astro-engine` | Deterministic planetary calculations. Zero platform deps. |
| `:aynvora-core` | Domain models, repository interfaces, SDK facade, analytics interfaces |
| `:aynvora-data` | Repository implementations, Room entities/DAOs/Database, AynvoraStorageEngine |
| `:aynvora-localization` | Locale management, translation tables |
| `:ui` | Shared Compose UI components |
| `androidApp` | Platform entry point: Firebase init, Room builder, analytics tracker impl |
| `desktopApp` | Platform entry point: NoOp analytics, in-memory or file storage |

---

## 3. Room KMP Foundation

### 3.1 Versions (do not upgrade without verification)

```
Room:  2.8.5
KSP:   (existing project version — see root build.gradle.kts)
```

### 3.2 Database Schema (Version 1)

| Table | Purpose |
|-------|---------|
| `user_profiles` | User identity records |
| `birth_profiles` | Reusable birth data for chart calculations |
| `saved_charts` | Persisted chart calculation cache |
| `user_preferences` | App settings (theme, language, defaults) |
| `content_packs` | Installed content pack metadata |
| `content_items` | Individual knowledge items (Astrology, Tarot, etc.) |
| `content_sync_metadata` | Sync state and version tracking |

### 3.3 Room Architecture

```
UI / ViewModel
     │ observes Flow
     ▼
Repository Interface (aynvora-core)
     │
     ▼
Repository Impl (aynvora-data)
     │
     ├── ContentItemDao ─────────────► Room (content_items)
     ├── ContentPackDao ─────────────► Room (content_packs)
     └── ContentSyncMetadataDao ─────► Room (content_sync_metadata)
```

Profile/preference data continues to use `AynvoraStorageEngine` (JSON with mutex serialization).

### 3.4 Platform Database Builder (NOT in shared code)

Android app must build the Room database:
```kotlin
Room.databaseBuilder(
    context,
    AynvoraDatabase::class.java,
    AynvoraDatabase.DATABASE_NAME
).build()
```

This call must be made in the platform application layer (`androidApp`), not in shared modules.

### 3.5 Migration Strategy

- Database version 1 is the initial baseline.
- Non-destructive migrations are required for all future schema changes.
- `exportSchema = false` is set for Phase 7.0 (no schema export file generated).
- For Phase 8+: enable `exportSchema = true` and provide migration files.

---

## 4. Offline-First Architecture

### 4.1 Local Source of Truth

Room is the authoritative offline source for content data:

```
Remote Manifest (future)
        │ only on successful sync
        ▼
 ContentVerifier.verify(...)
        │ only if VERIFIED
        ▼
   Room Database ◄── UI observes this via Flow
        │
        ▼
 ContentRepository.observeApprovedContent(...)
        │
        ▼
      ViewModel / UI
```

### 4.2 Offline Guarantees

- Content is served immediately from Room — no network delay.
- Network failure does not erase local content.
- Partial sync failure (unverified content) does not commit to Room.
- `APPROVED_FOR_PUBLICATION` is the only trust state shown to users.

### 4.3 Storage Engine (Profile Data)

The `AynvoraStorageEngine` remains the source of truth for:
- User profiles
- Birth profiles
- Saved charts
- User preferences

It uses a Coroutine Mutex for single-writer safety and atomic file writes.

---

## 5. Content Sync Foundation

### 5.1 Domain Contracts

All contracts are in `:aynvora-core/sync/`:

- `ContentVerifier` — verifies checksum and signature
- `StubContentVerifier` — development-only stub (does NOT verify checksums)
- `ContentVerificationStatus` — outcome enum
- `ContentVerificationResult` — structured result

### 5.2 Sync Pipeline (intended flow when backend is provisioned)

```
1. ContentSyncRepository.synchronize()
2. _syncStatus → FETCHING_MANIFEST
3. Fetch remote ContentManifest (HTTP — NOT YET IMPLEMENTED)
4. Compare remote version to ContentSyncMetadataRoomEntity.activeManifestVersion
5. Skip if version not newer (SyncResult.NoOp)
6. _syncStatus → DOWNLOADING_CONTENT
7. Download raw payload bytes
8. _syncStatus → VERIFYING_INTEGRITY
9. ContentVerifier.verify(...)
10. If VERIFIED → _syncStatus → COMMITTING_TRANSACTION
11. contentItemDao.replacePackItems(...) — atomic Room transaction
12. contentSyncMetadataDao.upsert(...)
13. _syncStatus → COMPLETED
14. Return SyncResult.Success(...)
```

### 5.3 Backend Configuration Required (Blocked — Not Yet Provisioned)

The following external setup is required before remote sync can be enabled:

| Requirement | Status |
|-------------|--------|
| Content distribution server URL | ❌ Not provisioned |
| Content manifest API endpoint | ❌ Not provisioned |
| Ed25519 signing key pair | ❌ Not generated |
| Public verification key bundled with app | ❌ Not configured |
| google-services.json for Firebase | ❌ Project owner must provide |

**Until these are provisioned:** `ContentSyncRepositoryImpl.synchronize()` returns `SyncResult.NoOp` safely.

---

## 6. Analytics Foundation

### 6.1 Architecture

```
ViewModel / Feature Layer
        │ calls
        ▼
AnalyticsTracker (interface, aynvora-core)
        │ wraps
        ▼
ConsentAwareAnalyticsTracker (checks consent before delegating)
        │ delegates to
        ▼
FirebaseAnalyticsTracker (androidApp only)
        │ calls
        ▼
Firebase Analytics SDK
```

### 6.2 Consent Flow

```
App Start → consentState = PENDING → analytics suppressed
     │
User grants consent → consentState = GRANTED
     │
FirebaseAnalyticsConsentManager.grantConsent()
     │ → setAnalyticsCollectionEnabled(true)
     │ → _consentState.value = GRANTED
     │
ConsentAwareAnalyticsTracker passes events to delegate
```

### 6.3 Event Catalog (all events in `AnalyticsEvent.kt`)

| Category | Events |
|----------|--------|
| Lifecycle | `app_opened`, `sdk_initialized` |
| Chart | `chart_calculation_requested/succeeded/failed` |
| Varga | `divisional_chart_requested/succeeded/failed` |
| Shadbala | `shadbala_calculation_requested/succeeded/failed` |
| Ashtakavarga | `ashtakavarga_calculation_requested/succeeded/failed` |
| Shodhana | `shodhana_calculation_requested/succeeded` |
| Pinda | `pinda_calculation_requested/succeeded` |
| Birth Profile | `birth_profile_saved/deleted` |
| Charts | `chart_saved/deleted` |
| Preferences | `preference_changed`, `theme_toggled` |
| Language | `language_changed` |
| Content | `content_item_opened`, `offline_content_accessed` |
| Sync | `content_sync_started/completed/failed` |
| Error | `sdk_error_observed` |

### 6.4 Privacy Rules (Permanent)

**NEVER send:**
- Name, email, phone number
- Birth date, birth time, birth coordinates
- Chart results, planetary positions
- Raw exception messages
- Religious or spiritual identity
- Any user-entered content

**Allowed parameters:** ruleset ID, chart division, duration_ms, error code, preference key, language code, module ID, sync reason, theme flag, analytics enabled flag.

### 6.5 Platform Analytics Coverage

| Platform | Implementation |
|----------|---------------|
| Android | `FirebaseAnalyticsTracker` + `FirebaseAnalyticsConsentManager` |
| iOS | `NoOpAnalyticsTracker` + `InMemoryAnalyticsConsentManager` (stub — extend in future phase) |
| Desktop | `NoOpAnalyticsTracker` + `InMemoryAnalyticsConsentManager` (Firebase not supported) |
| Tests | `NoOpAnalyticsTracker` + `InMemoryAnalyticsConsentManager` |

### 6.6 iOS Analytics Status

Firebase Analytics iOS requires `google-services-info.plist` and native Firebase SDK configuration via Swift/ObjC.
The current KMP/iOS build does not have a verified Firebase iOS integration.
Until verified, iOS analytics uses `NoOpAnalyticsTracker` — no events fire on iOS.
This is safe and correct behavior.

---

## 7. Testing Summary

### Tests Added in Phase 7.0

| Test File | Module | Coverage |
|-----------|--------|---------|
| `AnalyticsTrackerTest.kt` | `:aynvora-core` | Event names, params, NoOp acceptance |
| `AnalyticsConsentTest.kt` | `:aynvora-core` | Consent state, ConsentAwareTracker gating |
| `ArchitectureIsolationTest.kt` | `:aynvora-core` | Domain purity, ContentVerifier, PII absence |
| `OfflineFirstContractTest.kt` | `:aynvora-data` | Profile CRUD, AynvoraDataFactory, sync contracts |

### Key Test Assertions

- Analytics domain interfaces compile in `commonTest` (proves no platform imports)
- `ConsentAwareAnalyticsTracker` suppresses events when consent is PENDING or DENIED
- `ConsentAwareAnalyticsTracker` passes events when consent is GRANTED
- `StubContentVerifier` rejects stale/equal versions
- `StubContentVerifier` accepts newer versions
- User profile CRUD via InMemoryStorageDriver works offline
- No PII in standard event params

---

## 8. Known Gaps and Deferred Items

| Gap | Status |
|-----|--------|
| iOS Firebase Analytics | Deferred — requires native plist + Swift bindings |
| Production `ContentVerifier` | Deferred — requires signing key provisioning |
| Remote content sync HTTP fetch | Deferred — requires backend endpoint |
| Room androidTest suite | Deferred — requires instrumented Android test runner |
| Repository analytics (save/delete events) | Deferred — wiring into UI feature flows |
| `duration_ms` in analytics events | Currently `0L` — deferred until timing utility available in KMP without extra dep |

---

## 9. Files Created / Modified

| File | Change |
|------|--------|
| `aynvora-core/.../analytics/AnalyticsEvent.kt` | Extended with language, content, sync, SDK events |
| `aynvora-core/.../analytics/AnalyticsConsentManager.kt` | Created — consent interface + InMemory impl + ConsentAwareTracker |
| `aynvora-core/.../sync/ContentVerifier.kt` | Created — verification contract + StubContentVerifier |
| `aynvora-data/.../repository/ContentRepositoryImpl.kt` | Created — Room-backed content/sync repository impls |
| `aynvora-data/.../AynvoraDataFactory.kt` | Extended — added AynvoraDataComponents + createWithRoom() |
| `androidApp/.../analytics/FirebaseAnalyticsTracker.kt` | Created (prev phase) |
| `androidApp/.../analytics/FirebaseAnalyticsConsentManager.kt` | Created — Firebase consent enablement |
| `androidApp/.../MainActivity.kt` | Updated — analytics wiring |
| `docs/00_MASTER_RULES.md` | Updated — rules 26–31 added |
| `docs/PHASE_ANALYTICS_FOUNDATION.md` | Created (prev phase) |
| `docs/PHASE_7_0_ARCHITECTURE.md` | Created — this document |
| Test files (4 new) | See Testing Summary |
