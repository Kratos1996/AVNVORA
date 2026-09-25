# Garuda Puran Content Schema & Data Architecture

This document defines the strongly typed, immutable metadata and content schemas established in *
*PHASE 8.4A** of the AYNVORA Kotlin Multiplatform SDK.

---

## 1. Architectural Philosophy

1. **Strict Immutability**: All models are defined as Kotlin data classes with read-only `val`
   properties and validation checks in `init` blocks. Untyped maps such as `Map<String, Any>` are
   strictly prohibited.
2. **Rights Separation**: Metadata tracking is completely isolated from content distribution
   eligibility. A source can be tracked for bibliography and checksums while being classified as
   non-distributable.
3. **No Raw Scripture in Room**: SQLite/Room entities in AYNVORA store package metadata, version
   numbers, installation status, and checksums only. Large scripture text binaries are never stored
   in Room tables.
4. **Offline-First Contract**: Content packages are designed as atomic, versioned, checksum-verified
   local bundles with rollback compatibility.

---

## 2. Strongly Typed Source Metadata Model

Located in `com.aynvora.core.garudapuran.GarudaPuranSourceModels.kt`:

### A. Core Enums

- **`GarudaRightsStatus`**: `APPROVED_FOR_DISTRIBUTION`, `REFERENCE_ONLY_NON_DISTRIBUTABLE`,
  `LICENSE_UNCERTAIN`.
- **`GarudaLicenseStatus`**: `PUBLIC_DOMAIN_CLAIM_EXPLICIT`, `LICENSE_EXPLICIT`, `RESTRICTED`,
  `LICENSE_UNCERTAIN`, `NOT_STATED`.
- **`GarudaSourceStatus`**: `DISCOVERED`, `ACQUIRED`, `REFERENCE_ONLY`, `APPROVED_FOR_DISTRIBUTION`,
  `RESTRICTED`.
- **`GarudaContentReviewStatus`**: `UNREVIEWED`, `REVIEW_PENDING`, `SOURCE_VERIFIED`,
  `LICENSE_VERIFIED`, `APPROVED_FOR_APP`, `REJECTED`, `REFERENCE_ONLY`.
- **`GarudaVerificationStatus`**: `UNVERIFIED`, `FILE_IDENTITY_VERIFIED`, `PARTIALLY_VERIFIED`,
  `INDEX_VISUALLY_VERIFIED`, `CONTENT_VISUALLY_VERIFIED`, `EDITORIAL_VERIFIED`.
- **`GarudaPackageInstallationStatus`**: `NOT_INSTALLED`, `DOWNLOADING`, `VERIFYING`, `INSTALLED`,
  `FAILED`, `ROLLED_BACK`.

### B. Manifest & Source Models

- **`GarudaSourceManifest`**:
  ```kotlin
  data class GarudaSourceManifest(
      val manifestVersion: Int,
      val sources: List<GarudaSource>,
      val packages: List<GarudaContentPackageMetadata> = emptyList(),
  )
  ```
    - Enforces unique source IDs and package IDs upon construction.
- **`GarudaSource`**:
    - `sourceId: String`
    - `editionId: String`
    - `edition: GarudaEdition`
    - `sourcePathOrReference: String`
    - `checksumSha256: String` (validated to be 64-character lowercase hex)
    - `fileSizeBytes: Long`, `pageCount: Int`
    - `textBearingPageCount: Int`, `noTextLayerPageCount: Int`
    - `rights: GarudaSourceRights`
    - `license: GarudaSourceLicense`
    - `sourceStatus: GarudaSourceStatus`
    - `reviewStatus: GarudaContentReviewStatus`
    - Computed getters: `title`, `language`, `editor`, `translator`, `publisher`, `publicationYear`,
      `rightsStatus`, `commercialUse`, `redistributionAllowed`, `derivativeAllowed`,
      `attributionRequired`.
- **`GarudaEdition`**:
    - `editionId: String`, `title: String`, `language: String`
    - `publisher: String?`, `author: String?`, `editor: String?`, `translator: String?`
    - `publicationYear: Int?`, `publicationNote: String?`, `publisherItemCode: String?`
    - `sourceTradition: String?`, `workDescription: String?`, `pageCount: Int`
- **`GarudaSourceLicense`**:
    - `licenseStatus: GarudaLicenseStatus`, `licenseName: String`
    - `commercialUseAllowed: Boolean`, `redistributionAllowed: Boolean`
    - `derivativeAllowed: Boolean`, `attributionRequired: Boolean`, `attributionText: String?`

---

## 3. Normalized Content Models

To support future ingestion without silent rewrites or loss of attribution, normalized content
models enforce granular structure:

### A. Structural Elements

- **`GarudaChapter`**:
    - `chapterId: String`
    - `sourceId: String`, `editionId: String`
    - `chapterNumber: Int`, `chapterTitle: String`
    - `language: String`, `referenceRange: String`
    - `availability: GarudaPuranContentStatus`
    - `licenseStatus: GarudaLicenseStatus`
    - `printedPageRange: GarudaPageRange?`, `pdfPageRange: GarudaPageRange?`
    - `verseCount: Int?`
- **`GarudaSection`**:
    - `sectionId: String`, `sourceId: String`, `editionId: String`, `chapterNumber: Int`
    - `sectionNumber: Int`, `title: String`, `description: String?`
- **`GarudaVerse`**:
    - `verseId: String`, `sourceId: String`, `editionId: String`
    - `chapterNumber: Int`, `verseNumber: Int`
    - `originalText: String`, `transliteration: String?`
    - `normalizedText: String?`, `transformationNote: String?`
    - `textCategory: GarudaTextCategory`
    - `licenseStatus: GarudaLicenseStatus`
    - **Invariant**: `textCategory` cannot be `AYNVORA_EXPLANATION` (verses can only contain genuine
      scriptural texts or translations).
- **`GarudaParagraph`**:
    - For prose commentaries or translations (e.g., Dutt 1908).
- **`GarudaContentItem`**:
    - Comprehensive unit representing a passage, its source provenance, and governance status:
    - `contentId`, `sourceId`, `editionId`, `chapterNumber`, `sectionNumber`
    - `pageRange: GarudaPageRange`, `reference: GarudaReference`
    - `originalLanguage: String`, `sourceLanguage: String`, `languageCode: String`
    - `contentType: GarudaNormalizedContentType`
    - `contentVersion: String`, `sourceStatus: GarudaSourceContentStatus`
    - `rightsStatus: GarudaRightsStatus`, `reviewStatus: GarudaContentReviewStatus`
    - `originalText: String?`, `normalizedText: String?`, `transformationNote: String?`
    - `verseStart: Int?`, `verseEnd: Int?`
    - `textCategory: GarudaTextCategory`
    - **Invariant**: Content cannot be marked `APPROVED_FOR_APP` unless
      `rightsStatus == APPROVED_FOR_DISTRIBUTION`.

---

## 4. Sanskrit & Translation Handling

To prevent historical misattribution and maintain absolute textual honesty, the schema enforces 5
strictly separated text categories:

```kotlin
enum class GarudaTextCategory {
    ORIGINAL_SOURCE_TEXT,   // Sanskrit mula shloka in Devanagari
    TRANSLITERATION,        // Romanized IAST / CSX transliteration
    ENGLISH_TRANSLATION,    // Direct translation into English from historical source
    HINDI_TRANSLATION,      // Direct translation into Hindi from historical source
    AYNVORA_EXPLANATION;    // Modern synthetic editorial explanation / guidance
}
```

### Inviolable Rules:

1. **Never represent an AYNVORA explanation as scripture**: Explanations generated by AYNVORA must
   be explicitly tagged as `AYNVORA_EXPLANATION` and stored separately from `originalText` or
   `sourceMeaning`.
2. **Never attach a fake verse citation to an explanation**: Citations (`canonicalReferenceId`) must
   strictly map to verified chapter/verse or printed page boundaries in the source edition.
3. **No silent normalization**: Any normalization (e.g. whitespace stripping, ligature substitution)
   performed by `GarudaTextNormalizer` generates a deterministic `transformationNote` recording the
   exact operation.

---

## 5. Checksum & Integrity Architecture

Integrity verification is implemented via `GarudaChecksumVerifier`:

- Implements standard **FIPS 180-4 SHA-256** using purely multiplatform bitwise arithmetic.
- Zero platform-specific dependencies (works uniformly across Android, JVM, iOS, macOS, and
  Desktop).
- Checksum verification detects corruption or unauthorized file substitution.

```kotlin
data class GarudaPackageIntegrity(
    val packageId: String,
    val packageVersion: String,
    val fileIdentifier: String,
    val checksumSha256: String,
    val generatedTimestampEpochMs: Long,
    val verifiedTimestampEpochMs: Long? = null,
    val isVerified: Boolean = false,
)
```

> **Cryptographic Authenticity Limitation**:
> Checksum (SHA-256) verification ensures that a local or remote file has not suffered bit-level
> corruption. It does **not** prove cryptographic authenticity or provenance signing. Public-key
> signature verification (e.g., Ed25519) will be introduced in subsequent infrastructure phases.

---

## 6. Room Database Rule & Offline Package Contract

In accordance with project architecture rule `05_DATABASE.md`:

- **Room stores only lightweight package metadata**:
    - `package_id` (Primary Key)
    - `package_version`
    - `source_id`, `edition_id`
    - `language`
    - `checksum_sha256`
    - `installation_status` (`NOT_INSTALLED`, `INSTALLED`, `ROLLED_BACK`)
    - `installed_at_epoch_ms`
- **Corpus Text Storage**: Content packages are shipped or synchronized as atomic, compressed JSON
  assets that unpack into secure application storage, never into relational table rows.
- **Rollback Compatibility**: Package manifests declare `rollbackCompatible: Boolean`. If a content
  package fails integrity verification or is corrupted, the system rolls back to the prior
  known-good package metadata.
