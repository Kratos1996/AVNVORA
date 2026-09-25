# Garuda Puran Content Ingestion Pipeline & Normalization Specification

This document details the deterministic, 12-stage content ingestion pipeline implemented in
`GarudaContentIngestionPipeline.kt` for processing, normalizing, reviewing, and packaging historical
Garuda Purana texts into the AYNVORA SDK.

---

## 1. End-to-End Pipeline Architecture

```
SOURCE FILE (PDF/OCR Archive)
    ↓
TEXT EXTRACTION (Raw character stream with layout metadata)
    ↓
TEXT NORMALIZATION (Deterministic string sanitation preserving semantic meaning)
    ↓
CHAPTER DETECTION (Regex boundary parsing matching 16 Saroddhara chapters)
    ↓
SECTION DETECTION (Thematic section delineation within chapters)
    ↓
VERSE / REFERENCE DETECTION (Canonical reference assignment: GP_WOOD_CH{XX}_V{YY})
    ↓
EDITORIAL VALIDATION (Verification of completeness, transliteration, and structure)
    ↓
PROVENANCE ATTACHMENT (Immutable link to sourceId, editionId, pages, checksum)
    ↓
CONTENT REVIEW (Transition through review states to APPROVED_FOR_APP)
    ↓
LICENSE ELIGIBILITY CHECK (Enforcement of PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS)
    ↓
APPROVED CONTENT PACKAGE ASSEMBLY (JSON serialization & SHA-256 fingerprinting)
    ↓
LOCAL INSTALLATION & REPOSITORY (ContentBackedGarudaPuranRepository offline cache)
    ↓
REPORT ENGINE & VIEWER (GarudaPuranReportGenerator -> ReportDocument -> ReportViewer)
    ↓
EXPORT TO PDF (ReportPdfGenerator)
```

---

## 2. Text Normalization Rules & Quality Assurance

Text normalization is strictly deterministic and non-interpretive. The pipeline applies only allowed
structural transformations and strictly forbids any theological or linguistic tampering.

### Allowed Transformations

1. **Line-Break Normalization**: Merging soft line breaks within sentences while preserving hard
   paragraph and verse boundaries (`\r\n` -> `\n`, `[ \t]+` -> single space).
2. **Whitespace Normalization**: Trimming leading/trailing whitespace and collapsing extraneous
   internal spaces.
3. **Hyphenation Resolution**: Recombining words broken across lines by OCR (e.g.,
   `trans- \n migrate` -> `transmigrate`).
4. **Obvious OCR Encoding Artifacts**: Replacing legacy encoding artefacts, smart quotes, inverted
   marks, and erroneous ligature joins with standard Unicode UTF-8 representations.
5. **Page Header/Footer Removal**: Stripping running headers (e.g., "THE GARUDA PURANA", "THE SACRED
   BOOKS OF THE HINDUS", page numbers) when provably editorial.
6. **Chapter Heading Normalization**: Standardizing chapter numbering ("CHAPTER I.", "CHAPTER II.")
   to uniform canonical identifiers (`GP_WOOD_CH01`, etc.).

### Forbidden Transformations

The ingestion engine and human editors are strictly forbidden from:

- Silently rewriting sentences or modernizing archaic vocabulary.
- Paraphrasing verses to soften or reinterpret theological concepts.
- Altering deity names, sage names, hell names (Narakas), or ritual names.
- Fabricating Sanskrit shlokas or verse citations from AI memory.
- Removing difficult or uncomfortable passages (e.g., descriptions of Yamamarga or punishment in
  Naraka).
- Merging separate verses into synthetic composites without recording original spans.

---

## 3. Editorial Review Lifecycle

Each content item advances through an explicit state machine:

| Review State             | Description                                                                 | In Shipped App? |
|--------------------------|-----------------------------------------------------------------------------|-----------------|
| `UNREVIEWED`             | Raw OCR extraction without human or programmatic validation.                | **NO**          |
| `AUTO_NORMALIZED`        | Whitespace and hyphenation resolved by automated pipeline rules.            | **NO**          |
| `EDITOR_REVIEW_REQUIRED` | Text flagged with scan artifacts or divergent readings for editor scrutiny. | **NO**          |
| `SOURCE_VERIFIED`        | Visually checked against physical book scan/page images.                    | **NO**          |
| `APPROVED_FOR_APP`       | Passed source, rights, editorial, and provenance checks.                    | **YES**         |
| `REJECTED`               | Defective OCR, unverifiable provenance, or copyright encumbered.            | **NO**          |

> [!IMPORTANT]
> The production content package (`garuda-content-wood-1911-en-v1`) contains **ONLY** items with
> state `APPROVED_FOR_APP`.
> AI models may assist in indexing or error detection, but **cannot silently approve scripture text
**.

---

## 4. Separation of Layers: Scripture vs. AYNVORA Explanation

A strict tripartite boundary is maintained for every content item:

1. **Original Source Text (`originalSourceText`)**: Authentic Sanskrit verse in Roman
   transliteration / Devanagari.
2. **Source Meaning (`sourceMeaning`)**: Faithful 1911 translation by Ernest Wood & S.V.
   Subrahmanyam.
3. **AYNVORA Explanation (`aynvoraExplanation`)**: Clearly labeled context note:
    - Always prefixed: `"AYNVORA Explanatory Note: ..."`
    - Never represented as scripture or divine revelation.
    - Strictly objective, philosophical, and historical.
    - Contains zero predictive, fatalistic, or superstitious claims.
    - Zero astrology cross-linkage.
