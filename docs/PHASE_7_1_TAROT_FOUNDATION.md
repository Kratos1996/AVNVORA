# PHASE 7.1 — TAROT FOUNDATION SPECIFICATION

Version: 1.0 (Phase 7.1)
Status: IMPLEMENTED & TESTED

## 1. Scope & Objective

Phase 7.1 introduces the foundational Tarot feature into AYNVORA as a completely independent,
offline-first contemplative domain adhering to Clean Architecture:

- **Clean Architecture Boundary**:
    - Domain (`:aynvora-core/tarot`): pure models, spreads, random source abstraction, draw engine,
      use cases, disclaimer.
    - Data (`:aynvora-data/tarot`): Room entities, DAO methods, starter bilingual content,
      repository implementation.
    - Presentation (`:ui/tarot`): reflection screen, disclaimer gate, spread selector, draw view.
- **Astrology Engine Decoupling**: Absolute zero dependency or coupling with `:astro-engine`,
  planetary calculations, houses, or birth data.
- **Non-Predictive Governance**: Formatted strictly for mindfulness, relaxation, and
  self-reflection. Zero supernatural claims or medical/financial advice.
- **Privacy & Analytics**: Emits allowlisted, non-PII events (`tarot_opened`,
  `tarot_disclaimer_viewed`, `tarot_spread_selected`, `tarot_reading_started`, `tarot_card_drawn`,
  `tarot_reading_completed`, `tarot_content_opened`). Never records user questions or journal text.

---

## 2. Domain Architecture

### 2.1 Models (`:aynvora-core/tarot/TarotModels.kt`)

- `TarotCard`: Stable ID, sequence number, canonical name, Arcana (`MAJOR`, `MINOR`), Suit (`WANDS`,
  `CUPS`, `SWORDS`, `PENTACLES`).
- `TarotDeck`: Stable deck descriptor (`"rider_waite_smith_standard"`).
- `TarotCardOrientation`: Explicit `UPRIGHT` vs `REVERSED`.
- `TarotSpread`: Configurable positions (`SingleCard`, `ThreeCardTimeline`).
- `TarotSpreadPosition`: Stable position identifier, ordering index, localized title, description.
- `TarotCardDraw`: Compound representation of drawn card, orientation, and spread position.
- `TarotReading`: Immutable reading result with unique reading ID, spread ID, draws, and timestamp.
- `TarotCardContent`: Localized reflective perspective, short description, keywords,
  upright/reversed meanings, source attribution.
- `TarotDisclaimer`: Mandatory disclosure model in English and Hindi.

### 2.2 Random Draw Engine (`:aynvora-core/tarot/TarotDrawEngine.kt`)

- `TarotRandomSource`: Interface abstracting randomness.
- `DefaultTarotRandomSource`: Production multiplatform random generator.
- `DeterministicTarotRandomSource`: Seeded reproducible generator for automated testing.
- `TarotDrawEngine`: Performs Fisher-Yates draw without replacement and orientation assignment.

### 2.3 Use Cases (`:aynvora-core/tarot/TarotUseCases.kt`)

- `PerformTarotReadingUseCase`: Orchestrates card retrieval, random draw, history persistence, and
  analytics dispatching.
- `GetTarotCardContentUseCase`: Retrieves localized reflective interpretations.

---

## 3. Data & Room Persistence

### 3.1 Entities & DAOs (`:aynvora-data`)

- `tarot_decks`: Metadata for installed decks.
- `tarot_cards`: 78 standard canonical card definitions.
- `tarot_card_content`: Localized reflective descriptions in English (`en`) and Hindi (`hi`).
- `tarot_reading_history`: Local historical readings storing card IDs, orientations, and positions.
- `TarotDao`: Room DAO providing atomic queries, seeding, and reactive Flow observation.
- `TarotRepositoryImpl`: Handles offline reads, auto-bootstrapping default decks/content on initial
  empty database state, and local history saving.

---

## 4. Analytics Catalog & Privacy Allowlist

### 4.1 Events

| Event Name                | Parameters                            | Purpose                                  |
|---------------------------|---------------------------------------|------------------------------------------|
| `tarot_opened`            | none                                  | Tracks feature discovery                 |
| `tarot_disclaimer_viewed` | none                                  | Tracks ethical disclosure acknowledgment |
| `tarot_spread_selected`   | `spread_id`                           | Measures spread preferences              |
| `tarot_reading_started`   | `spread_id`, `card_count`             | Measures reading initiations             |
| `tarot_card_drawn`        | `spread_id`, `card_id`, `orientation` | Card occurrence tracking                 |
| `tarot_reading_completed` | `spread_id`, `card_count`             | Completion metrics                       |
| `tarot_reading_failed`    | `spread_id`, `error_code`             | Error rate tracking                      |
| `tarot_content_opened`    | `card_id`, `language_code`            | Card meaning inspection                  |

### 4.2 Privacy Rules

- **Prohibited**: User questions, private notes, journal entries, names, email, phone, birth data,
  astrology positions.
- **Allowed**: `spread_id`, `card_count`, `card_id`, `orientation`, `language_code`, `error_code`.
- **Consent Gate**: Controlled via `ConsentAwareAnalyticsTracker` and suppressed when consent is
  `PENDING` or `DENIED`.

---

## 5. Testing & Verification

1. **Domain Tests** (`TarotDomainTest.kt`):
    - Standard 78-card deck completeness and unique IDs.
    - Single-card and Three-card timeline draw correctness.
    - Duplicate prevention within draws.
    - Reproducible draws with `DeterministicTarotRandomSource`.
    - Orientation control with `allowReversed`.
    - Non-PII analytics tracking.
    - Ethical disclaimer content verification.
2. **Data Tests** (`TarotDataTest.kt`):
    - Automatic seeding of standard deck when Room is empty.
    - Bilingual content retrieval in English and Hindi.
    - Local reading persistence without PII.
3. **Architecture Isolation** (`ArchitectureIsolationTest.kt`):
    - Tarot domain zero platform/database dependencies.
    - NoOp analytics tracking safety across all Tarot events.
4. **Localization Tests** (`LocaleManagerTest.kt`):
    - Tarot translation key verification for English and Hindi catalogs.
