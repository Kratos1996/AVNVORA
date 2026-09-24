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

## KMP Architecture Layers & Canonical Package Structure

- **Presentation (`:ui`)**:
    - `com.aynvora.ui`: Core app scaffold and navigation host
    - `com.aynvora.ui.tarot`: Tarot presentation screen, state, and UI components
    - `com.aynvora.ui.report`: Shared ReportDocument viewer/route/PDF line layout; Android and JVM
      PDF/share adapters
- **Domain (`:aynvora-core`)**:
    - `com.aynvora.core`: Primary public SDK facade (`AynvoraSdk`, `Aynvora`)
    - `com.aynvora.core.models`: Public domain models (immutable, validated, platform-independent)
    - `com.aynvora.core.repository`: Domain repository contracts (`UserProfileRepository`,
      `BirthProfileRepository`, `SavedChartRepository`, `UserPreferencesRepository`,
      `ContentRepository`, `ContentSyncRepository`)
    - `com.aynvora.core.usecase`: Pure domain use cases (`SaveBirthProfileUseCase`,
      `ObserveBirthProfilesUseCase`, `DeleteBirthProfileUseCase`, `SaveChartUseCase`,
      `ObserveSavedChartsUseCase`, `DeleteChartUseCase`, `GetUserPreferencesUseCase`, etc.)
    - `com.aynvora.core.tarot`: Tarot domain models, repository contracts, draw engine, and use
      cases
    - `com.aynvora.core.feature`: Core Feature Registry and typed feature availability (
      `CoreFeatureRegistry.kt`)
    - `com.aynvora.core.palmistry`: Hastrekha/Palmistry domain models and contracts
    - `com.aynvora.core.gemstone`: Navaratna gemstone models, inventory context, and recommendations
    - `com.aynvora.core.gita`: Bhagavad Gita Sanskrit shloka models, editions, and repository
      interface
    - `com.aynvora.core.garudapuran`: typed Garuda Puran topic, section, edition, source text,
      interpretation/practice, reference, availability models and repository use cases
    - `com.aynvora.core.lalkitab`: Lal Kitab rules, conditions, and symbolic remedy contracts
    - `com.aynvora.core.ai`: On-Device AI Assistant, structured tool contracts, tool registry, and
      provenance
    - `com.aynvora.core.guidance`: Daily morning/night routine models and scheduling interfaces
    - `com.aynvora.core.wallpaper`: Personalized Wallpaper Studio device profiles and prompt builder
    - `com.aynvora.core.astrology.prediction`: Predictive core, classical rule engine, timing
      windows, life topic taxonomy, and Prediction Evidence Graph (`AstrologyPredictionEngine`,
      `CanonicalAstrologyRules`)
    - `com.aynvora.core.intelligence`: Core Intelligence & multi-feature orchestration (
      `MultiFeatureOrchestrator`, `EvidenceGraph`, `FeatureCapabilityRegistry`,
      `DataSufficiencyValidator`, tradition profiles)
    - `com.aynvora.core.analytics`: Analytics tracker & consent interfaces, domain event catalog
    - `com.aynvora.core.report`: Typed report model, built-in report catalog/generator registry,
      Kundali and Garuda Puran report preparation/generators, and PDF/share contracts
    - `com.aynvora.core.di`: Koin domain DI module (`coreDomainModule`)
    - `com.aynvora.core.result`: Deterministic result monad (`AynvoraResult`)
    - `com.aynvora.core.sync`: Content verification contracts (`ContentVerifier`)
- **Calculation Engine (`:astro-engine`)**:
    - `com.aynvora.astro`: Pure mathematical astronomical calculations, zero platform or framework
      dependencies
    - `com.aynvora.astro.dasha`: Vimshottari Dasha calculation engine (Mahadasha, Antardasha,
      Pratyantardasha)
    - `com.aynvora.astro.transit`: Planetary transit engine (Gochara, timeline, ingress, retrograde
      stations)
    - `com.aynvora.astro.panchang`: Five-limb classical Panchang engine (Tithi, Nakshatra, Yoga,
      Karana, Vara)
- **Data Layer (`:aynvora-data`)**:
    - `com.aynvora.data`: Repository factory (`AynvoraDataFactory`)
    - `com.aynvora.data.repository`: Concrete implementations of domain repositories (
      `UserProfileRepositoryImpl`, `BirthProfileRepositoryImpl`, `SavedChartRepositoryImpl`,
      `UserPreferencesRepositoryImpl`, `ContentRepositoryImpl`, `ContentSyncRepositoryImpl`)
    - `com.aynvora.data.database`: Room KMP database (`AynvoraDatabase`)
    - `com.aynvora.data.database.entity`: Room entities
    - `com.aynvora.data.database.dao`: Room DAOs
    - `com.aynvora.data.tarot`: Tarot Room repository implementation, starter content, and content
      pack
    - `com.aynvora.data.garudapuran`: approved-package adapter over the generic offline
      `ContentRepository`; no Garuda source pack is bundled
    - `com.aynvora.data.storage`: File storage drivers and migrations
    - `com.aynvora.data.security`: Storage cipher abstractions
    - `com.aynvora.data.di`: Koin data DI module (`coreDataModule`)
- **Presentation (`:ui`)**:
    - `com.aynvora.ui`: Core app scaffold and navigation host
    - `com.aynvora.ui.tarot`: Tarot presentation screen, state, and UI components
    - `com.aynvora.ui.garudapuran`: reusable Garuda Puran content/catalog entry route; delegates
      reports to `com.aynvora.ui.report`
    - `com.aynvora.ui.di`: Koin presentation module & full app aggregator (`uiModule`,
      `aynvoraAppModules`)
- **Design System (`:design-system`)**:
    - `com.aynvora.designsystem`: Theme tokens, typography, colors, components, sheets, dialogs
- **Localization (`:aynvora-localization`)**:
    - `com.aynvora.localization`: Multiplatform locale registry, formatters, translation tables, and
      `AynvoraReportTextResolver`
- **Platform Hosts**:
    - `androidApp`: Android entry point, Firebase Analytics implementation, platform Room driver
    - `desktopApp`: JVM Desktop application host, NoOp analytics, desktop storage driver
    - `iosApp`: Native iOS application host; report PDF/share bridge is not currently configured

Dependency rule:
Presentation (`:ui`) → Domain (`:aynvora-core`) ← Data (`:aynvora-data`)
`:astro-engine` remains pure math/astronomy.
Tarot feature: completely decoupled from `:astro-engine`.
The report domain is platform-independent. Android/JVM rendering consumes the same `ReportDocument`;
no report calculation is repeated in a platform renderer. The current `:ui` Gradle configuration has
Android and JVM targets only.

Astrology reference audit and validation classifications: `REFERENCE_VALIDATION_JKR.md` and
`PHASE_8_2_ASTROLOGY_REFERENCE_PARITY.md`. The JKR source PDF is in `docs/`; parity is classified by
calculation and profile.

Garuda Puran source governance, package metadata, provenance, report flow, current no-corpus status,
and extension procedure: `GARUDA_PURAN_ARCHITECTURE.md`. The repository contains no source-backed
Garuda Puran content package; the route/catalog/report preparation remain source-gated.

## Rule
Do not duplicate domain logic in androidMain, iosMain or desktopMain.
