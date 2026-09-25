# Garuda Puran Provenance & Evidence Policy

This document establishes the traceability, grounding, and provenance verification policies for the
Garuda Purana domain within the AYNVORA Kotlin Multiplatform SDK.

---

## 1. Provenance Mandate

The AYNVORA SDK operates under a strict principle of **verifiable scriptural and astrological
grounding**. Every content item, interpretation, practice, and report section must be
deterministically traceable to its source and edition.

The system must always be capable of answering:
> **"Which exact source, edition, chapter, section, and page produced this content, and under what
legal authorization?"**

---

## 2. Integration with EvidenceGraph Architecture

Garuda Purana citations integrate with the core `com.aynvora.core.evidence.PredictionEvidenceGraph`
and `com.aynvora.core.evidence.EvidenceReference` pipeline.

### Required Provenance Fields

Every scriptural provenance edge must contain the following typed properties:

| Field                | Type     | Description                      | Example                          |
|----------------------|----------|----------------------------------|----------------------------------|
| `feature`            | `String` | Feature identifier               | `"GARUDA_PURAN"`                 |
| `sourceId`           | `String` | Unique stable source ID          | `"garuda-puran-wood-1911-en"`    |
| `editionId`          | `String` | Stable edition identifier        | `"wood-1911-saroddhara"`         |
| `chapter`            | `String` | Chapter number or identifier     | `"2"`                            |
| `section`            | `String` | Section or topic key             | `"yamamarga"`                    |
| `reference`          | `String` | Canonical reference identifier   | `"GP_SARODDHARA_CH2_V15"`        |
| `language`           | `String` | BCP-47 language code             | `"en"`                           |
| `contentVersion`     | `String` | Schema / package content version | `"1.0.0"`                        |
| `licenseStatus`      | `String` | License classification enum name | `"PUBLIC_DOMAIN_CLAIM_EXPLICIT"` |
| `verificationStatus` | `String` | Verification degree enum name    | `"FILE_IDENTITY_VERIFIED"`       |

### Code Contract: `GarudaSourceReference.toProvenanceMap()`

Implemented in `com.aynvora.core.garudapuran.GarudaPuranSourceModels.kt`:

```kotlin
fun toProvenanceMap(): Map<String, String> {
    return buildMap {
        put("feature", "GARUDA_PURAN")
        put("sourceId", sourceId)
        put("editionId", editionId)
        chapter?.let { put("chapter", it) }
        section?.let { put("section", it) }
        canonicalReferenceId?.let { put("reference", it) }
        pageRange?.let { put("pageRange", it.toDisplayString()) }
        language?.let { put("language", it) }
        contentVersion?.let { put("contentVersion", it) }
        licenseStatus?.let { put("licenseStatus", it.name) }
        verificationStatus?.let { put("verificationStatus", it.name) }
    }
}
```

---

## 3. Strict Rules of Scriptural Grounding

1. **NEVER FABRICATE PROVENANCE EDGES**:
    - If an explanation is derived from general tradition but cannot be cited to a verified chapter
      and verse in the source manifest, it must NOT be assigned a fake citation or synthetic verse
      number.
    - Fabricating verse references or misattributing quotes is a critical defect.

2. **SEPARATION OF SCRIPTURE AND EXPLANATION**:
    - Original scripture (`ORIGINAL_SOURCE_TEXT`) and historical translations (
      `ENGLISH_TRANSLATION`, `HINDI_TRANSLATION`) must never be modified or merged with AYNVORA
      editorial advice.
    - Any synthetic guidance must be explicitly typed as `AYNVORA_EXPLANATION` and must cite the
      underlying passage via an explicit edge, rather than posing as the scripture itself.

3. **NO EDGES TO NON-DISTRIBUTABLE SOURCES IN USER EXPORTS**:
    - When a report or user export is generated, client-visible citations must only reference
      approved, distributable sources.
    - Non-distributable sources (such as Gita Press or MLBD) may only exist in internal audit logs
      or developer reference catalogs.

---

## 4. Analytics Policy & Data Privacy

In accordance with `13_PRIVACY.md` and `19_LOGGING_OBSERVABILITY.md`:

1. **NO SCRIPTURE TEXT IN TELEMETRY**:
    - Source passages, Sanskrit shlokas, English/Hindi translations, and religious explanations must
      **NEVER** be included in analytics events or telemetry payloads.
2. **METADATA-ONLY TELEMETRY**:
    - If analytics are emitted for package lifecycle management (e.g. package download started,
      verification succeeded, package installed, rollback executed), payloads must contain ONLY:
        - `event_name` (e.g., `GARUDA_PACKAGE_INSTALLED`)
        - `package_id`
        - `package_version`
        - `source_id`
        - `status`
        - `duration_ms`
3. **ZERO PII**:
    - User profile data, birth details, astrological chart parameters, and religious search queries
      must never be correlated with source retrieval telemetry.
