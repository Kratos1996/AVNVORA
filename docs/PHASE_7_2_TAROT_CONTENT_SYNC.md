# AYNVORA — PHASE 7.2: TAROT CONTENT PACK + SYNC + PRODUCTION READINESS

# ARCHITECTURE, CONTENT & SECURITY AUDIT REPORT

## 1. Executive Summary

Phase 7.2 elevates the foundational Tarot feature established in Phase 7.1 to a production-ready,
offline-first system adhering to Clean Architecture, Room KMP persistence, Phase 7.0 content
synchronization contracts, and privacy-first Firebase Analytics.

Key deliverables completed in Phase 7.2:

1. **Full 78-Card Deck**: 100% complete standard deck (22 Major Arcana + 56 Minor Arcana across
   Wands, Cups, Swords, and Pentacles).
2. **Bilingual Presentation Content**: Every card contains dedicated English and Hindi reflective
   interpretations, titles, keywords, and upright/reversed perspectives.
3. **Idempotent Room Seeding**: Database seeding checks existing card counts and never overwrites
   newer local content with older starter data.
4. **Content Synchronization Integration**: Full integration with Phase 7.0 `ContentVerifier` and
   `SyncResult`. Atomic transactions reject stale content versions (`VERSION_STALE`) and rollback on
   checksum/signature failure.
5. **Local-Only Reading History**: History preserves only spread ID, deck ID, drawn cards, and
   timestamps. User questions, journals, and PII are strictly excluded.
6. **Cross-Platform Readiness**: Verified across Android (compilation, DEX assembly, APK build),
   Desktop JVM, and iOS simulator compilation.

---

## 2. Tarot Content Pack Model & Completeness

### 2.1 Deck Structure

- **Major Arcana**: 22 cards (`major_00_fool` through `major_21_world`)
- **Minor Arcana**: 56 cards across 4 suits:
    - Wands: 14 cards (Ace through King)
    - Cups: 14 cards (Ace through King)
    - Swords: 14 cards (Ace through King)
    - Pentacles: 14 cards (Ace through King)
- **Total Cards**: 78 cards
- **Supported Languages**: English (`en`) and Hindi (`hi`)
- **Total Content Records**: 156 localized content entries

### 2.2 Quality & Attribution

- Every card provides:
    - Stable immutable card ID (independent of language or content version)
    - Card number and arcana classification
    - Upright and reversed reflective meanings
    - Canonical attribution: `"AYNVORA Contemplative Traditions Archive (Public Domain)"`
- Non-predictive framing: purely reflective, contemplative, and psychological. No fatalistic
  predictions, medical claims, financial guarantees, or fear-based language.

---

## 3. Database Initialization & Seeding (Room KMP)

### 3.1 Idempotency & Safety

- `TarotRepositoryImpl.seedStarterContentIfEmpty()` queries `tarotDao.getCardCount()`.
- If cards are already present (count >= 78), seeding is skipped as a No-Op.
- If empty, inserts deck, cards, and localized content in a safe, non-destructive operation.
- Reading history (`tarot_reading_history`) is never wiped or modified during content seeding or
  updates.

### 3.2 Content Updates (`updateContentPack`)

- Compares incoming version with installed content version.
- Rejects stale incoming content (`AynvoraResult.Failure.SyncFailure("VERSION_STALE")`).
- Atomically replaces cards and localized card content using Room transactions.

---

## 4. Content Synchronization & Security Verification

### 4.1 Sync Contract Alignment

- Tarot content sync implements the Phase 7.0 content delivery lifecycle:
  `Local Room` → `Installed Version` → `Remote Manifest Check` → `Download` → `Schema Validation` →
  `Checksum Verification` → `Signature Verification` → `Atomic Room Upsert`.
- Status:
    - **Implemented Contract**: Interface-driven verification with SHA-256 checksum and signature
      verification contract via `ContentVerifier`.
    - **Production Backend**: Remote endpoints and cryptographic signing keys are not yet
      provisioned in backend infrastructure. Local fallback and verified offline-first content are
      active.

---

## 5. Randomness & Draw Integrity

### 5.1 `TarotRandomSource`

- Production card draws use `DefaultTarotRandomSource`, utilizing Kotlin's standard multiplatform
  random generator (`kotlin.random.Random.Default`).
- Multiplatform Cryptographic Randomness status:
    - JVM/Android: `java.security.SecureRandom` can be wired via platform factory.
    - Native iOS: `SecRandomCopyBytes` can be wired via platform factory.
    - Shared KMP common code currently uses platform-neutral `Random.Default` to maintain zero
      external C/JNI dependencies across targets without reflection.
- Test draws use deterministic `DeterministicTarotRandomSource(cardSequence)` for unit tests.

---

## 6. Reading History Reliability & Privacy

- Schema: `tarot_reading_history` (`id`, `spreadId`, `deckId`, `serializedDrawsJson`,
  `timestampEpochMs`).
- **Privacy Enforcement**:
    - No user questions or queries.
    - No journal entries or personal notes.
    - No names, emails, phone numbers, or birth charts.
    - No cloud sync; stored exclusively in local Room SQLite storage.
- Operations:
    - Insertion upon reading completion.
    - Historical query ordered descending by timestamp.
    - Deletion by reading ID or complete history wipe supported.

---

## 7. Firebase Analytics & Consent

- All Tarot analytics route strictly through `AnalyticsTracker`.
- Event catalog:
    - `tarot_opened`
    - `tarot_disclaimer_viewed`
    - `tarot_spread_selected`
    - `tarot_reading_started`
    - `tarot_card_drawn`
    - `tarot_reading_completed`
    - `tarot_reading_failed`
    - `tarot_content_opened`
- Allowlisted Parameters:
    - `spread_id`, `card_count`, `card_id`, `orientation`, `deck_id`, `disclaimer_accepted`
- Consent behavior:
    - Pending / Denied: No events emitted.
    - Granted: Allowlisted events dispatched.
    - Failures in analytics never block or impact card drawing or reading display.

---

## 8. Verification & Test Summary

| Target / Suite                            | Command                                                                                                                   | Result                |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|-----------------------|
| Complete Test Suite                       | `./gradlew test`                                                                                                          | **PASSED** (67 tasks) |
| Core, Data, Localization, Astro JVM Tests | `./gradlew :aynvora-core:jvmTest :aynvora-data:jvmTest :aynvora-localization:jvmTest :astro-engine:jvmTest --rerun-tasks` | **PASSED** (22 tasks) |
| Android Debug APK                         | `./gradlew assembleDebug`                                                                                                 | **PASSED** (92 tasks) |
| iOS Simulator Native Targets              | `./gradlew :aynvora-core:compileKotlinIosSimulatorArm64 :astro-engine:compileKotlinIosSimulatorArm64`                     | **PASSED** (7 tasks)  |
| Desktop & Design-System JVM               | `./gradlew :design-system:compileAndroidMain :design-system:compileKotlinJvm`                                             | **PASSED** (22 tasks) |
