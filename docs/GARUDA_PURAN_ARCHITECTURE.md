# Garuda Puran Domain and Report Architecture

Phase 8.4 defines a source-gated offline content and report path for Garuda Puran. The
implementation provides typed contracts and the pipeline for approved content. It does not bundle
scripture or claim that a source-backed reading is currently available.

## Source inventory and publication boundary

The repository inventory found no Garuda Puran scripture text, published translation,
chapter/section citation set, seed records, or installed Garuda Puran content pack. Existing
references to Saroddhara in the roadmap describe planned content, not a source shipped in this
repository. The only Garuda records used in tests are synthetic fixtures explicitly labelled as test
material; they do not contain scripture or real chapter citations.

Consequently, all catalog topics currently resolve to `CONTENT_UNAVAILABLE` with a typed reason,
`GetGarudaPuranContentUseCase` returns an empty source-backed snapshot, and report preparation
returns `NotFound` until a valid local package is installed. The overall `GARUDA_PURAN` product
status remains `ComingSoon`; infrastructure completion does not imply corpus coverage.

## Domain model

`com.aynvora.core.garudapuran` owns the platform-independent contracts:

- `GarudaPuranSourceEdition` identifies an edition, publisher/editor, source language, and optional
  rights note.
- `GarudaPuranSection` and `GarudaPuranReference` carry section/chapter/verse location plus a
  canonical source reference ID. A reference must identify a section or a chapter.
- `GarudaPuranText` separates optional original text and transliteration from the selected-language
  source meaning and localized presentation.
- `GarudaPuranInterpretation` and `GarudaPuranPractice` are separate typed records and each must
  point to the source reference used.
- `GarudaPuranContentItem` binds a topic, section, reference, edition, localized text, content
  version, and content type. Interpretations and practices cannot cite a different/unrepresented
  reference.
- `GarudaPuranTopicAvailability` distinguishes `AVAILABLE`, `FOUNDATION_ONLY`, and
  `CONTENT_UNAVAILABLE` and requires a reason when no source items exist.

The topic IDs cover introduction/context, dialogue context, dharma/conduct, traditional teachings,
life guidance, death/afterlife, karma, ritual practices, spiritual guidance, and other source-backed
topics. These IDs are taxonomy only; none imply corpus coverage.

List-bearing snapshots copy their input collections before report generation. The Domain layer has
no Room, Compose, network, platform, or AI dependency.

## Content pipeline and offline behavior

The data adapter `ContentBackedGarudaPuranRepository` reads the existing `ContentRepository` and the
`GARUD_PURAN` content module. It reads only rows and packages marked `APPROVED_FOR_PUBLICATION`,
requires the package and row to agree on module and language, parses the versioned structured
metadata, validates canonical references through the domain model, and fails closed on missing or
malformed source metadata. It never calls network APIs.

English (`en`) and Hindi (`hi`) are supported. The selected language must match both the stored row
and report request. There is no fallback to another language. Source text can remain in its own
original language; source meaning, interpretation, practice, and display presentation are
selected-language content.

Content metadata is stored on the existing text content row (`ContentItem.metadataJson`) and
localized presentation uses `ContentItem.body`; the repository does not create a Garuda-specific
database, DAO, or PDF store. Schema version 1 metadata carries the topic, section, canonical
reference, source edition identifiers, original text/transliteration when supplied, source meaning,
content type, interpretations, and practices.

The current shared content synchronization implementation is a stub, and `StubContentVerifier`
explicitly does not validate checksum or cryptographic signatures. The Garuda repository enforces
the local approval state and record consistency; it does not establish authenticity on its own. Do
not publish or sync a corpus until the shared production verifier and safe package-install path are
implemented and an authorized source edition/translation has been reviewed.

## Evidence and provenance

`GarudaPuranEvidenceGraphFactory` emits separate source-content, traditional-interpretation, and
practice evidence nodes. Provenance preserves feature ID, edition, canonical reference, language,
content version, content type, and evidence kind. The only generated edges connect an
interpretation/practice to the exact source reference present in that item. No astrology edge is
inferred.

`MultiFeatureOrchestrator` reads Garuda Puran only when that feature is requested. It contributes
evidence to the response and marks it as traditional interpretation; empty or failed reads add no
invented evidence. Garuda Puran does not feed back into astrology calculations, and AI is not called
by the content or report path.

## Report generation, localization, and viewer

`PrepareGarudaPuranReportUseCase` obtains the selected-language catalog and source items from the
repository, rejects empty or mixed-language input, copies a snapshot, and builds its evidence graph.
`GarudaPuranReportGenerator` is registered in the shared `ReportGeneratorRegistry` and emits the
existing `ReportDocument` model. It includes only applicable sections for introduction,
source/edition, available topics, source-backed details, traditional teachings/practices,
references, limitations, and disclaimer. It creates no empty filler sections and cannot be invoked
with an arbitrary external input constructor.

The report pipeline has no AI dependency. Every displayed passage comes from the installed approved
content package; labels, report explanations, availability reasons, and disclaimers come from the
English or Hindi localization tables. Religious/traditional statements are labelled as tradition,
not scientific fact, and the report disclaims guaranteed outcomes and medical/legal/financial
advice.

`GarudaPuranRoute` is a reusable Compose entry route for catalog browsing, topic detail,
loading/error/unavailable states, and report entry. It delegates viewing, PDF, and share to the
existing `ReportRoute` and `ReportDocument` pipeline. The app root is currently a scaffold rather
than a feature NavHost; host applications can supply this route through their own navigation. It is
not wired as a new application-wide navigation shell in Phase 8.4.

## Capability status and extension

- Product feature: `ComingSoon("approved source-backed content package")` until actual reviewed
  content is installed.
- `garuda_source_provenance`: `IMPLEMENTED` for local schema, language, approval-state, edition, and
  reference validation.
- `garuda_chapter_search`: `FOUNDATION_ONLY`; usable only when a package supplies indexed
  source-backed records.
- Report type/generator: registered, but report preparation fails with a typed unavailable result
  while there are no approved items.

To add content, first secure rights and choose an authoritative edition/translation. Prepare one
reviewed structured record per source passage in the versioned schema, with canonical edition and
location metadata. Validate original text, translation, and any interpretation/practice separately;
cite each interpretation/practice to an included reference. Package a single language at a time,
pass the production signature/checksum verifier, install atomically with rollback, and add
real-source regression fixtures. Only then may the corresponding topic and report be considered
available.

## Analytics privacy

The existing `AnalyticsTracker` abstraction emits `garuda_puran_opened`,
`garuda_puran_topic_opened` (stable topic ID only), `garuda_puran_report_generated`,
`garuda_puran_pdf_generated`, and `garuda_puran_shared`. These events contain no birth details,
source text, report text, citations, or user-entered content. Analytics stay outside Domain logic.

## PDF platform support

The Garuda route calls the existing shared PDF/share contracts. Android has the existing Android PDF
implementation; Desktop JVM has the existing JVM implementation. There is no configured iOS Gradle
target or native bridge for this flow, so iOS compilation and Garuda PDF/share support are
unsupported and unverified.

## Validation

Phase 8.4 JVM tests cover typed invariants and defensive copies, empty/offline package behavior,
approval/language/schema/reference validation, report section order and provenance, localization,
orchestrator isolation, and privacy-safe analytics contracts. Build verification is reported
separately in the Phase 8.4 implementation summary and is not described as a test pass.

---

## Phase 8.4A — Source Acquisition, License Verification & Content Foundation

Phase 8.4A establishes the legal and structural foundation for Garuda Purana source acquisition:

1. **Source Discovery & Legal Matrix**: Researched and cataloged 7 candidate editions (User Summary
   PDF, Gita Press Code 1416, GRETIL 1906, SanskritDocuments, Wood & Subrahmanyam 1911, Manmatha
   Nath Dutt 1908, and Motilal Banarsidass 1978).
2. **Rights Verification**:
    - **Approved for Future Ingestion**: Ernest Wood & S.V. Subrahmanyam (1911) and Manmatha Nath
      Dutt (1908) are verified as Public Domain worldwide.
    - **Reference-Only Non-Distributable**: Gita Press Code 1416 and Motilal Banarsidass (1978) are
      modern copyrighted editions. GRETIL has non-commercial (CC BY-NC-SA) restrictions;
      SanskritDocuments restricts use to personal study.
    - **License Uncertain**: Anonymous summary PDF has no author or rights statement.
3. **Strongly Typed Source Manifest & Content Models**:
    - `GarudaSourceManifest`, `GarudaSource`, `GarudaEdition`, `GarudaSourceLicense`,
      `GarudaSourceRights`, `GarudaSourceStatus`, `GarudaPackageIntegrity`.
    - Normalized content models: `GarudaChapter`, `GarudaSection`, `GarudaVerse`, `GarudaParagraph`,
      `GarudaContentItem`.
    - Multiplatform FIPS 180-4 SHA-256 verifier (`GarudaChecksumVerifier`).
    - Text category separation (`ORIGINAL_SOURCE_TEXT`, `TRANSLITERATION`, `ENGLISH_TRANSLATION`,
      `HINDI_TRANSLATION`, `AYNVORA_EXPLANATION`).
4. **Content Availability**:
    - Feature content status in Phase 8.4A was strictly `CONTENT_UNAVAILABLE`.

---

## Phase 8.4B — Targeted Public-Domain Content Ingestion, Editorial Verification & Real Report Integration

Phase 8.4B transitions the Garuda Puran feature from foundation-only to real approved content for
verified jurisdictions:

1. **Primary Ingested Content Package**:
    - Package ID: `garuda-content-wood-1911-en-v1` (Version 1.0.0, English `en`).
    - Source: Ernest Wood & S.V. Subrahmanyam (1911), *The Garuda Purana (Saroddhara)*, Panini
      Office, Allahabad.
    - Checksum: SHA-256 `4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5`.
    - Rights Status: `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` (US 17 U.S.C. § 305; Indian Copyright
      Act §22 Life+60 expired Jan 1, 2026).
    - Coverage: 23 curated, visually verified passages spanning all 16 chapters of the Saroddhara
      recension, mapped across all 10 typed domain topics (`GarudaPuranTopicId`).

2. **Secondary Cross-Reference Registry**:
    - Source: Manmatha Nath Dutt (1908), *The Garuda Puranam*, Elysium Press, Calcutta.
    - Checksum: SHA-256 `2313f4e0472e3ec9a97b46cb47e6d74a675ba4d7da150214d4759da94960319e`.
    - Maintained in `GarudaSourceComparisonRegistry` to document textual variants (`SOURCE_VARIANT`,
      `EDITION_VARIANT`, `TEXT_UNCERTAIN`) without merging texts.

3. **Deterministic 12-Stage Ingestion Pipeline**:
    - Implemented in `GarudaContentIngestionPipeline.kt`: Source File -> Text Extraction ->
      Normalization -> Chapter Detection -> Section Detection -> Verse Detection -> Editorial
      Validation -> Provenance Attachment -> Review Lifecycle (`APPROVED_FOR_APP`) -> License
      Check -> Package Assembly -> Local Installation.

4. **Hindi Language Handling (Option B)**:
    - Option B selected: Hindi scripture content remains `CONTENT_UNAVAILABLE` with status
      `NO_APPROVED_CONTENT_FOR_LANGUAGE`.
    - Zero silent fallback to copyrighted Gita Press text or English passages.

5. **End-to-End Report Engine & Viewer Integration**:
    - Clean unidirectional flow: UI -> `PrepareGarudaPuranReportUseCase` ->
      `ContentBackedGarudaPuranRepository` -> `GarudaPuranReportGenerator` -> `ReportDocument` ->
      `ReportViewer` -> `ReportPdfGenerator`.
    - Generates 6 structured sections: Introduction, Source Information, Available Topics, Topic
      Details, Traditional Teachings, and Source References.
    - 100% offline readability with zero network calls and zero AI generation.

6. **Feature Capability Status**:
    - Promoted to `PARTIALLY_IMPLEMENTED` (reflects authentic English Saroddhara availability while
      Hindi remains unavailable under Option B).
