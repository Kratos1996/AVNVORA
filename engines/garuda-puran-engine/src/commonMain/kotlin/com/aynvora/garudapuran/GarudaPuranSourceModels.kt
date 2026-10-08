package com.aynvora.garudapuran

import kotlinx.serialization.Serializable

/** Legal state recorded for an edition. This records evidence; it does not grant rights. */
@Serializable
enum class GarudaLicenseStatus {
    NOT_STATED,
    LICENSE_EXPLICIT,
    RIGHTS_EXPLICITLY_GRANTED,
    PUBLIC_DOMAIN_CLAIM_EXPLICIT,
    PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
    RIGHTS_VERIFIED,
    RIGHTS_REVIEW_REQUIRED,
    RIGHTS_UNCLEAR,
    LICENSE_UNCERTAIN,
    RESTRICTED;

    val isPermittedForRedistribution: Boolean
        get() = this == LICENSE_EXPLICIT ||
                this == RIGHTS_EXPLICITLY_GRANTED ||
                this == PUBLIC_DOMAIN_CLAIM_EXPLICIT ||
                this == PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS ||
                this == RIGHTS_VERIFIED
}

@Serializable
enum class GarudaRightsStatus {
    RIGHTS_UNVERIFIED,
    RIGHTS_UNCLEAR,
    RIGHTS_REVIEW_REQUIRED,
    LICENSE_UNCERTAIN,
    REFERENCE_ONLY,
    REFERENCE_ONLY_NON_DISTRIBUTABLE,
    RESTRICTED,
    PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
    RIGHTS_VERIFIED,
    APPROVED_FOR_DISTRIBUTION;

    val isDistributable: Boolean
        get() = this == APPROVED_FOR_DISTRIBUTION ||
                this == PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS ||
                this == RIGHTS_VERIFIED
    val isReferenceOnly: Boolean get() = !isDistributable
}

@Serializable
enum class GarudaSourceStatus {
    DISCOVERED,
    ACQUIRED,
    METADATA_VERIFIED,
    SOURCE_VERIFIED,
    LICENSE_VERIFIED,
    REFERENCE_ONLY,
    REJECTED,
    APPROVED_FOR_DISTRIBUTION;

    val isEligibleForDistribution: Boolean get() = this == APPROVED_FOR_DISTRIBUTION
}

@Serializable
enum class GarudaVerificationStatus {
    NOT_VERIFIED,
    FILE_IDENTITY_VERIFIED,
    INDEX_VISUALLY_VERIFIED,
    CONTENT_VISUALLY_VERIFIED,
    PARTIALLY_VERIFIED,
}

@Serializable
enum class GarudaTextExtractionStatus {
    CLEAN_TEXT_LAYER,
    LEGACY_FONT_ENCODING_GARBLED,
    MIXED_TEXT_AND_IMAGES,
    IMAGE_ONLY,
    UNKNOWN,
}

@Serializable
enum class GarudaSourceContentStatus {
    DISCOVERED,
    METADATA_VERIFIED,
    REFERENCE_ONLY,
    CONTENT_NOT_INGESTED,
    APPROVED,
}

@Serializable
enum class GarudaContentReviewStatus {
    UNREVIEWED,
    AUTO_NORMALIZED,
    EDITOR_REVIEW_REQUIRED,
    REVIEW_PENDING,
    SOURCE_VERIFIED,
    LICENSE_VERIFIED,
    APPROVED,
    APPROVED_FOR_APP,
    REJECTED,
    REFERENCE_ONLY;

    val isApproved: Boolean get() = this == APPROVED || this == APPROVED_FOR_APP
}

@Serializable
enum class GarudaSourceVariantType {
    EXACT_MATCH,
    SOURCE_VARIANT,
    EDITION_VARIANT,
    TEXT_UNCERTAIN;
}

@Serializable
data class GarudaSourceComparison(
    val canonicalReferenceId: String,
    val primarySourceId: String,
    val primaryEditionId: String,
    val primaryChapter: Int,
    val primaryVerse: Int,
    val secondarySourceId: String,
    val secondaryEditionId: String,
    val secondaryChapter: Int,
    val secondaryVerse: Int,
    val variantType: GarudaSourceVariantType,
    val comparisonNotes: String,
)

@Serializable
enum class GarudaPackageInstallationStatus {
    NOT_INSTALLED,
    INSTALLED,
    REJECTED,
}

@Serializable
enum class GarudaNormalizedContentType {
    TABLE_OF_CONTENTS_ENTRY,
    SUMMARY,
    SOURCE_PASSAGE,
    TRANSLATION,
    EDITORIAL_COMMENTARY,
    GLOSSARY,
}

@Serializable
enum class GarudaTextCategory {
    ORIGINAL_SOURCE_TEXT,
    TRANSLITERATION,
    ENGLISH_TRANSLATION,
    HINDI_TRANSLATION,
    AYNVORA_EXPLANATION;

    val isScriptureText: Boolean
        get() = this == ORIGINAL_SOURCE_TEXT || this == TRANSLITERATION

    val isTranslation: Boolean
        get() = this == ENGLISH_TRANSLATION || this == HINDI_TRANSLATION

    val isExplanation: Boolean
        get() = this == AYNVORA_EXPLANATION
}

@Serializable
data class GarudaPageRange(
    val pdfPageStart: Int,
    val pdfPageEnd: Int = pdfPageStart,
    val printedPageStart: Int? = null,
    val printedPageEnd: Int? = printedPageStart,
) {
    init {
        require(pdfPageStart > 0 && pdfPageEnd >= pdfPageStart)
        require(printedPageStart == null || printedPageStart > 0)
        require(printedPageEnd == null || printedPageEnd > 0)
        require(printedPageStart == null || printedPageEnd == null || printedPageEnd >= printedPageStart)
    }
}

/** Citation metadata shared by content, rights evidence, and the EvidenceGraph. */
@Serializable
data class GarudaSourceReference(
    val sourceId: String,
    val editionId: String,
    val chapter: String? = null,
    val section: String? = null,
    val pageRange: GarudaPageRange? = null,
    val language: String,
    val contentVersion: String? = null,
    val rightsStatus: GarudaRightsStatus,
    val verificationStatus: GarudaVerificationStatus,
    val canonicalReferenceId: String? = null,
    val licenseStatus: GarudaLicenseStatus? = null,
) {
    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank() && language.isNotBlank())
        require(chapter == null || chapter.isNotBlank())
        require(section == null || section.isNotBlank())
        require(contentVersion == null || contentVersion.isNotBlank())
        require(canonicalReferenceId == null || canonicalReferenceId.isNotBlank())
        require(chapter != null || section != null || pageRange != null || canonicalReferenceId != null)
    }

    fun toProvenanceMap(): Map<String, String> {
        val map = linkedMapOf<String, String>()
        map["feature"] = "GARUDA_PURAN"
        map["sourceId"] = sourceId
        map["editionId"] = editionId
        if (chapter != null) map["chapter"] = chapter
        if (section != null) map["section"] = section
        if (canonicalReferenceId != null) map["reference"] = canonicalReferenceId
        map["language"] = language
        if (contentVersion != null) map["contentVersion"] = contentVersion
        if (licenseStatus != null) map["licenseStatus"] = licenseStatus.name
        map["rightsStatus"] = rightsStatus.name
        map["verificationStatus"] = verificationStatus.name
        return map
    }
}

@Serializable
data class GarudaReference(
    val canonicalReferenceId: String,
    val source: GarudaSourceReference,
) {
    init {
        require(canonicalReferenceId.isNotBlank())
    }
}

@Serializable
data class GarudaEdition(
    val editionId: String,
    val title: String,
    val language: String,
    val publisher: String? = null,
    val author: String? = null,
    val editor: String? = null,
    val translator: String? = null,
    val publicationYear: Int? = null,
    val publicationYearAsPrinted: String? = null,
    val publisherItemCode: String? = null,
    val publicationNote: String? = null,
    val subtitle: String? = null,
    val sourceTradition: String? = null,
    val workDescription: String? = null,
    val pageCount: Int,
) {
    init {
        require(editionId.isNotBlank() && title.isNotBlank() && language.isNotBlank())
        require(pageCount > 0 && (publicationYear == null || publicationYear > 0))
        require(
            listOf(
                publisher,
                author,
                editor,
                translator,
                publicationYearAsPrinted,
                publisherItemCode,
                publicationNote,
                subtitle,
                sourceTradition,
                workDescription
            )
                .all { it == null || it.isNotBlank() })
    }
}

@Serializable
data class GarudaSourceLicense(
    val licenseStatus: GarudaLicenseStatus,
    val licenseName: String,
    val commercialUseAllowed: Boolean,
    val redistributionAllowed: Boolean,
    val derivativeAllowed: Boolean,
    val attributionRequired: Boolean,
    val attributionText: String? = null,
    val licenseUrl: String? = null,
    val permissionRequired: Boolean = !redistributionAllowed,
) {
    init {
        require(licenseName.isNotBlank())
        require(licenseUrl == null || licenseUrl.isNotBlank())
        require(attributionText == null || attributionText.isNotBlank())
        if (redistributionAllowed) {
            require(licenseStatus.isPermittedForRedistribution) {
                "redistributionAllowed requires an explicit license, explicit rights grant, or public domain claim"
            }
        }
    }
}

@Serializable
data class GarudaSourceRights(
    val sourceId: String,
    val editionId: String,
    val licenseStatus: GarudaLicenseStatus,
    val rightsStatus: GarudaRightsStatus,
    val verificationStatus: GarudaVerificationStatus,
    val evidence: List<GarudaSourceReference> = emptyList(),
    val note: String? = null,
    val commercialUseAllowed: Boolean = rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
    val redistributionAllowed: Boolean = rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
    val derivativeAllowed: Boolean = rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
    val attributionRequired: Boolean = true,
) {
    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank())
        require(evidence.all { it.sourceId == sourceId && it.editionId == editionId })
        require(note == null || note.isNotBlank())
        require(rightsStatus != GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION || licenseStatus.isPermittedForRedistribution) {
            "APPROVED_FOR_DISTRIBUTION requires an explicit license, granted rights, or explicit public domain claim"
        }
    }
}

@Serializable
data class GarudaSource(
    val sourceId: String,
    val editionId: String,
    val edition: GarudaEdition,
    val sourcePathOrReference: String,
    val checksumSha256: String,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val textBearingPageCount: Int,
    val noTextLayerPageCount: Int,
    val embeddedImageObjectCount: Int,
    val textExtractionStatus: GarudaTextExtractionStatus,
    val verificationStatus: GarudaVerificationStatus,
    val rights: GarudaSourceRights,
    val license: GarudaSourceLicense = GarudaSourceLicense(
        licenseStatus = rights.licenseStatus,
        licenseName = when (rights.licenseStatus) {
            GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT -> "Public Domain"
            GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS -> "Public Domain in Eligible Jurisdictions"
            GarudaLicenseStatus.RIGHTS_VERIFIED -> "Rights Verified"
            GarudaLicenseStatus.RIGHTS_REVIEW_REQUIRED -> "Rights Review Required"
            GarudaLicenseStatus.LICENSE_EXPLICIT -> "Explicit License"
            GarudaLicenseStatus.RIGHTS_EXPLICITLY_GRANTED -> "Explicit Permission Granted"
            GarudaLicenseStatus.RESTRICTED -> "Restricted / Proprietary"
            GarudaLicenseStatus.LICENSE_UNCERTAIN -> "Uncertain / Unverified"
            GarudaLicenseStatus.RIGHTS_UNCLEAR -> "Rights Unclear"
            GarudaLicenseStatus.NOT_STATED -> "Not Stated"
        },
        commercialUseAllowed = rights.commercialUseAllowed,
        redistributionAllowed = rights.redistributionAllowed,
        derivativeAllowed = rights.derivativeAllowed,
        attributionRequired = rights.attributionRequired,
        attributionText = edition.publisher ?: edition.editor ?: edition.author,
    ),
    val verificationDate: String? = null,
    val verificationMethod: String? = null,
    val contentVersion: String? = null,
    val reviewStatus: GarudaContentReviewStatus = GarudaContentReviewStatus.UNREVIEWED,
    val sourceStatus: GarudaSourceStatus = if (rights.rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION) GarudaSourceStatus.APPROVED_FOR_DISTRIBUTION else GarudaSourceStatus.REFERENCE_ONLY,
) {
    val title: String get() = edition.title
    val language: String get() = edition.language
    val editor: String? get() = edition.editor
    val translator: String? get() = edition.translator
    val publisher: String? get() = edition.publisher
    val publicationYear: Int? get() = edition.publicationYear
    val sourceLocation: String get() = sourcePathOrReference
    val rightsStatus: GarudaRightsStatus get() = rights.rightsStatus
    val commercialUse: Boolean get() = license.commercialUseAllowed
    val redistributionAllowed: Boolean get() = license.redistributionAllowed
    val derivativeAllowed: Boolean get() = license.derivativeAllowed
    val attributionRequired: Boolean get() = license.attributionRequired
    val checksum: String get() = checksumSha256

    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank() && sourcePathOrReference.isNotBlank())
        require(edition.editionId == editionId && edition.pageCount == pageCount)
        require(checksumSha256.matches(Regex("^[0-9a-f]{64}$"))) { "Checksum must be 64-char lowercase hex string" }
        require(fileSizeBytes > 0 && pageCount > 0)
        require(textBearingPageCount >= 0 && noTextLayerPageCount >= 0 && textBearingPageCount + noTextLayerPageCount == pageCount)
        require(embeddedImageObjectCount >= 0)
        require(rights.sourceId == sourceId && rights.editionId == editionId)
        require(
            rights.rightsStatus != GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION ||
                    (license.commercialUseAllowed && license.redistributionAllowed)
        ) { "Approved for distribution sources must permit commercial use and redistribution" }
        require(
            !reviewStatus.isApproved ||
                    rights.rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
        ) { "Source cannot be approved for application use unless rights are APPROVED_FOR_DISTRIBUTION" }
    }
}

@Serializable
data class GarudaContentPackageMetadata(
    val packageId: String,
    val packageVersion: String,
    val sourceId: String,
    val editionId: String,
    val language: String,
    val checksumSha256: String,
    val installationStatus: GarudaPackageInstallationStatus,
    val verificationStatus: GarudaVerificationStatus,
    val rightsStatus: GarudaRightsStatus,
    val updateTimestampEpochMs: Long = 0L,
    val rollbackCompatible: Boolean = true,
) {
    init {
        require(packageId.isNotBlank() && packageVersion.isNotBlank())
        require(sourceId.isNotBlank() && editionId.isNotBlank() && language.isNotBlank())
        require(checksumSha256.matches(Regex("^[0-9a-f]{64}$")))
        require(
            installationStatus != GarudaPackageInstallationStatus.INSTALLED ||
                    rightsStatus.isDistributable
        ) { "Cannot install a package that is not distributable" }
    }
}

@Serializable
data class GarudaPackageIntegrity(
    val packageId: String,
    val packageVersion: String,
    val fileIdentifier: String,
    val checksumSha256: String,
    val generatedTimestampEpochMs: Long,
    val verifiedTimestampEpochMs: Long? = null,
    val isVerified: Boolean = false,
) {
    init {
        require(packageId.isNotBlank() && packageVersion.isNotBlank() && fileIdentifier.isNotBlank())
        require(checksumSha256.matches(Regex("^[0-9a-f]{64}$")))
        require(generatedTimestampEpochMs >= 0L)
        require(verifiedTimestampEpochMs == null || verifiedTimestampEpochMs >= generatedTimestampEpochMs)
    }
}

@Serializable
data class GarudaSourceManifest(
    val manifestVersion: Int,
    val sources: List<GarudaSource>,
    val packages: List<GarudaContentPackageMetadata> = emptyList(),
) {
    init {
        require(manifestVersion > 0)
        require(sources.map { it.sourceId }
            .distinct().size == sources.size) { "Duplicate sourceId found in manifest" }
        require(sources.map { it.editionId }
            .distinct().size == sources.size) { "Duplicate editionId found in manifest" }
        require(packages.map { it.packageId }
            .distinct().size == packages.size) { "Duplicate packageId found in manifest" }
        require(packages.all { pack -> sources.any { it.sourceId == pack.sourceId && it.editionId == pack.editionId } }) {
            "Package sourceId and editionId must match an existing source in the manifest"
        }
    }

    fun getSource(sourceId: String): GarudaSource? = sources.firstOrNull { it.sourceId == sourceId }
    fun getApprovedSources(): List<GarudaSource> =
        sources.filter { it.rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION }

    fun getReferenceOnlySources(): List<GarudaSource> =
        sources.filter { it.rightsStatus.isReferenceOnly }

    fun getUncertainSources(): List<GarudaSource> =
        sources.filter { it.rightsStatus == GarudaRightsStatus.LICENSE_UNCERTAIN || it.rights.licenseStatus == GarudaLicenseStatus.LICENSE_UNCERTAIN }

    fun isDistributable(sourceId: String): Boolean =
        getSource(sourceId)?.rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION

    companion object {
        fun empty() = GarudaSourceManifest(manifestVersion = 1, sources = emptyList())
    }
}

@Serializable
data class GarudaChapterReference(
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val title: String,
    val language: String,
    val pageRange: GarudaPageRange,
    val contentType: GarudaNormalizedContentType = GarudaNormalizedContentType.TABLE_OF_CONTENTS_ENTRY,
    val sourceStatus: GarudaSourceContentStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
    val rightsStatus: GarudaRightsStatus = GarudaRightsStatus.RIGHTS_UNVERIFIED,
    val reviewStatus: GarudaContentReviewStatus = GarudaContentReviewStatus.UNREVIEWED,
) {
    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank() && chapterNumber > 0)
        require(title.isNotBlank() && language.isNotBlank())
        require(!reviewStatus.isApproved || rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
    }
}

@Serializable
data class GarudaChapter(
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val title: String,
    val language: String,
    val pageRange: GarudaPageRange,
    val chapterId: String = "ch_${sourceId}_$chapterNumber",
    val referenceRange: String? = null,
    val availability: GarudaPuranContentStatus = GarudaPuranContentStatus.CONTENT_UNAVAILABLE,
    val licenseStatus: GarudaLicenseStatus = GarudaLicenseStatus.NOT_STATED,
    val sections: List<GarudaSection> = emptyList(),
    val sourceStatus: GarudaSourceContentStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
    val rightsStatus: GarudaRightsStatus = GarudaRightsStatus.RIGHTS_UNVERIFIED,
    val reviewStatus: GarudaContentReviewStatus = GarudaContentReviewStatus.UNREVIEWED,
) {
    init {
        require(chapterId.isNotBlank() && sourceId.isNotBlank() && editionId.isNotBlank() && chapterNumber > 0)
        require(title.isNotBlank() && language.isNotBlank())
        require(sections.all { it.sourceId == sourceId && it.editionId == editionId && it.chapterNumber == chapterNumber })
        require(!reviewStatus.isApproved || rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
    }
}

@Serializable
data class GarudaSection(
    val sectionId: String,
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val sectionNumber: Int,
    val title: String,
    val language: String,
    val pageRange: GarudaPageRange,
    val sourceStatus: GarudaSourceContentStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
    val rightsStatus: GarudaRightsStatus = GarudaRightsStatus.RIGHTS_UNVERIFIED,
    val reviewStatus: GarudaContentReviewStatus = GarudaContentReviewStatus.UNREVIEWED,
) {
    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank() && chapterNumber > 0 && sectionNumber > 0)
        require(title.isNotBlank() && language.isNotBlank())
        require(!reviewStatus.isApproved || rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
    }
}

@Serializable
data class GarudaVerse(
    val verseId: String,
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val originalText: String? = null,
    val transliteration: String? = null,
    val normalizedText: String? = null,
    val textCategory: GarudaTextCategory = GarudaTextCategory.ORIGINAL_SOURCE_TEXT,
    val pageRange: GarudaPageRange? = null,
    val licenseStatus: GarudaLicenseStatus = GarudaLicenseStatus.NOT_STATED,
) {
    init {
        require(verseId.isNotBlank() && sourceId.isNotBlank() && editionId.isNotBlank())
        require(chapterNumber > 0 && verseNumber > 0)
        require(textCategory != GarudaTextCategory.AYNVORA_EXPLANATION || originalText == null) {
            "AYNVORA explanations must never be represented as original scripture text"
        }
    }
}

@Serializable
data class GarudaParagraph(
    val paragraphId: String,
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val sectionNumber: Int? = null,
    val paragraphNumber: Int,
    val text: String,
    val language: String,
    val textCategory: GarudaTextCategory = GarudaTextCategory.ENGLISH_TRANSLATION,
    val pageRange: GarudaPageRange? = null,
    val licenseStatus: GarudaLicenseStatus = GarudaLicenseStatus.NOT_STATED,
) {
    init {
        require(paragraphId.isNotBlank() && sourceId.isNotBlank() && editionId.isNotBlank())
        require(chapterNumber > 0 && paragraphNumber > 0 && text.isNotBlank() && language.isNotBlank())
    }
}

@Serializable
data class GarudaContentItem(
    val contentId: String,
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int?,
    val sectionNumber: Int?,
    val pageRange: GarudaPageRange,
    val reference: GarudaReference,
    val originalLanguage: String,
    val contentType: GarudaNormalizedContentType,
    val contentVersion: String,
    val sourceStatus: GarudaSourceContentStatus,
    val rightsStatus: GarudaRightsStatus,
    val reviewStatus: GarudaContentReviewStatus,
    val originalText: String? = null,
    val normalizedText: String? = null,
    val transformationNote: String? = null,
    val verseStart: Int? = null,
    val verseEnd: Int? = null,
    val textCategory: GarudaTextCategory = GarudaTextCategory.ORIGINAL_SOURCE_TEXT,
    val sourceLanguage: String = originalLanguage,
    val languageCode: String = sourceLanguage,
    val chapterName: String? = null,
    val section: String? = null,
    val originalTextReference: String? = null,
    val translationReference: String? = null,
    val licenseStatus: GarudaLicenseStatus = GarudaLicenseStatus.NOT_STATED,
    val verificationStatus: GarudaVerificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
) {
    val chapter: Int? get() = chapterNumber
    val language: String get() = languageCode

    init {
        require(contentId.isNotBlank() && sourceId.isNotBlank() && editionId.isNotBlank())
        require(chapterNumber == null || chapterNumber > 0)
        require(sectionNumber == null || sectionNumber > 0)
        require(verseStart == null || verseStart > 0)
        require(verseEnd == null || verseEnd > 0)
        require(verseStart == null || verseEnd == null || verseEnd >= verseStart)
        require(originalLanguage.isNotBlank() && contentVersion.isNotBlank())
        require(sourceLanguage.isNotBlank() && languageCode.isNotBlank())
        require(reference.source.sourceId == sourceId && reference.source.editionId == editionId)
        require(reference.source.pageRange == pageRange)
        require(!reviewStatus.isApproved || rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION) {
            "Content item cannot be approved unless rights are APPROVED_FOR_DISTRIBUTION"
        }
        require(originalText == null || originalText.isNotBlank())
        require(normalizedText == null || normalizedText.isNotBlank())
        require(transformationNote == null || transformationNote.isNotBlank())
        if (textCategory == GarudaTextCategory.AYNVORA_EXPLANATION) {
            require(originalText == null) {
                "AYNVORA explanations cannot contain raw original scripture text in originalText field"
            }
        }
    }
}

@Serializable
data class GarudaVerseReference(
    val sourceId: String,
    val editionId: String,
    val chapterNumber: Int,
    val verseStart: Int,
    val verseEnd: Int = verseStart,
    val canonicalReferenceId: String,
    val pageRange: GarudaPageRange? = null,
) {
    init {
        require(sourceId.isNotBlank() && editionId.isNotBlank())
        require(chapterNumber > 0 && verseStart > 0)
        require(verseEnd >= verseStart)
        require(canonicalReferenceId.isNotBlank())
    }
}

@Serializable
data class GarudaGlossaryEntry(
    val entryId: String,
    val sourceId: String,
    val editionId: String,
    val term: String,
    val definition: String,
    val reference: GarudaSourceReference,
    val language: String,
    val sourceStatus: GarudaSourceContentStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
    val rightsStatus: GarudaRightsStatus = GarudaRightsStatus.RIGHTS_UNVERIFIED,
    val reviewStatus: GarudaContentReviewStatus = GarudaContentReviewStatus.UNREVIEWED,
) {
    init {
        require(entryId.isNotBlank() && sourceId.isNotBlank() && editionId.isNotBlank())
        require(term.isNotBlank() && definition.isNotBlank() && language.isNotBlank())
        require(reference.sourceId == sourceId && reference.editionId == editionId)
        require(!reviewStatus.isApproved || rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
    }
}

object GarudaSourceManifestFactory {
    fun create(
        manifestVersion: Int,
        sources: List<GarudaSource>,
        packages: List<GarudaContentPackageMetadata> = emptyList(),
    ) = GarudaSourceManifest(manifestVersion, sources.toList(), packages.toList())
}

/**
 * Deterministic text normalizer for scripture and translation text.
 * Trims whitespace, standardizes spaces and newlines, and records transformation metadata.
 */
object GarudaTextNormalizer {
    fun normalize(rawText: String): NormalizedTextResult {
        val trimmed = rawText.trim()
        val normalized = trimmed.replace(Regex("[ \\t]+"), " ")
            .replace("\r\n", "\n")
            .replace("\r", "\n")
        val transformed = rawText != normalized
        val note =
            if (transformed) "Deterministic whitespace and newline normalization applied." else null
        return NormalizedTextResult(normalized, transformed, note)
    }
}

@Serializable
data class NormalizedTextResult(
    val normalizedText: String,
    val isTransformed: Boolean,
    val transformationNote: String? = null,
)

/**
 * Pure Kotlin SHA-256 calculator and verifier (commonMain multiplatform compatible).
 * Compliant with FIPS 180-4 standard.
 */
object GarudaChecksumVerifier {
    fun calculateSha256(bytes: ByteArray): String {
        val messageBits = bytes.size.toLong() * 8L
        val k = ((448 - (bytes.size * 8 + 8) % 512 + 512) % 512) / 8
        val padded = ByteArray(bytes.size + 1 + k + 8)
        bytes.copyInto(padded, 0)
        padded[bytes.size] = 0x80.toByte()
        for (i in 0 until 8) {
            padded[padded.size - 1 - i] = ((messageBits ushr (i * 8)) and 0xFF).toByte()
        }

        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        val kConstants = intArrayOf(
            0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
            0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
            0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
            0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
            0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
            0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
            0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
            0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
            0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
            0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
            0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
            0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
            0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
            0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
            0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
            0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
        )

        val w = IntArray(64)
        for (chunk in 0 until padded.size step 64) {
            for (i in 0 until 16) {
                val idx = chunk + i * 4
                w[i] = ((padded[idx].toInt() and 0xFF) shl 24) or
                        ((padded[idx + 1].toInt() and 0xFF) shl 16) or
                        ((padded[idx + 2].toInt() and 0xFF) shl 8) or
                        (padded[idx + 3].toInt() and 0xFF)
            }
            for (i in 16 until 64) {
                val s0 =
                    (w[i - 15].rotateRight(7)) xor (w[i - 15].rotateRight(18)) xor (w[i - 15] ushr 3)
                val s1 =
                    (w[i - 2].rotateRight(17)) xor (w[i - 2].rotateRight(19)) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (i in 0 until 64) {
                val s1 = (e.rotateRight(6)) xor (e.rotateRight(11)) xor (e.rotateRight(25))
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + kConstants[i] + w[i]
                val s0 = (a.rotateRight(2)) xor (a.rotateRight(13)) xor (a.rotateRight(22))
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h
        }

        fun Int.toHex(): String = buildString(8) {
            for (i in 28 downTo 0 step 4) {
                val nibble = (this@toHex ushr i) and 0xF
                append("0123456789abcdef"[nibble])
            }
        }

        return h0.toHex() + h1.toHex() + h2.toHex() + h3.toHex() + h4.toHex() + h5.toHex() + h6.toHex() + h7.toHex()
    }

    fun calculateSha256(text: String): String = calculateSha256(text.encodeToByteArray())

    fun verifyChecksum(expectedSha256: String, actualBytes: ByteArray): Boolean {
        require(expectedSha256.length == 64) { "Expected SHA-256 must be a 64-character hex string" }
        val actualSha256 = calculateSha256(actualBytes)
        return expectedSha256.equals(actualSha256, ignoreCase = true)
    }
}

fun GarudaRightsStatus.toTextKey(): GarudaPuranTextKey = when (this) {
    GarudaRightsStatus.RIGHTS_UNVERIFIED -> GarudaPuranTextKey.SOURCE_RIGHTS_UNVERIFIED
    GarudaRightsStatus.RIGHTS_UNCLEAR -> GarudaPuranTextKey.SOURCE_RIGHTS_UNCLEAR
    GarudaRightsStatus.RIGHTS_REVIEW_REQUIRED -> GarudaPuranTextKey.SOURCE_RIGHTS_REVIEW_REQUIRED
    GarudaRightsStatus.LICENSE_UNCERTAIN -> GarudaPuranTextKey.SOURCE_RIGHTS_LICENSE_UNCERTAIN
    GarudaRightsStatus.REFERENCE_ONLY -> GarudaPuranTextKey.SOURCE_REFERENCE_ONLY
    GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE -> GarudaPuranTextKey.SOURCE_REFERENCE_ONLY_NON_DISTRIBUTABLE
    GarudaRightsStatus.RESTRICTED -> GarudaPuranTextKey.SOURCE_RESTRICTED
    GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS -> GarudaPuranTextKey.SOURCE_RIGHTS_PUBLIC_DOMAIN_ELIGIBLE
    GarudaRightsStatus.RIGHTS_VERIFIED -> GarudaPuranTextKey.SOURCE_RIGHTS_VERIFIED
    GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION -> GarudaPuranTextKey.SOURCE_APPROVED_FOR_DISTRIBUTION
}

fun GarudaLicenseStatus.toTextKey(): GarudaPuranTextKey = when (this) {
    GarudaLicenseStatus.NOT_STATED -> GarudaPuranTextKey.SOURCE_LICENSE_NOT_STATED
    GarudaLicenseStatus.LICENSE_EXPLICIT -> GarudaPuranTextKey.SOURCE_LICENSE_EXPLICIT
    GarudaLicenseStatus.RIGHTS_EXPLICITLY_GRANTED -> GarudaPuranTextKey.SOURCE_RIGHTS_EXPLICITLY_GRANTED
    GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT -> GarudaPuranTextKey.SOURCE_PUBLIC_DOMAIN_CLAIM_EXPLICIT
    GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS -> GarudaPuranTextKey.SOURCE_RIGHTS_PUBLIC_DOMAIN_ELIGIBLE
    GarudaLicenseStatus.RIGHTS_VERIFIED -> GarudaPuranTextKey.SOURCE_RIGHTS_VERIFIED
    GarudaLicenseStatus.RIGHTS_REVIEW_REQUIRED -> GarudaPuranTextKey.SOURCE_RIGHTS_REVIEW_REQUIRED
    GarudaLicenseStatus.RIGHTS_UNCLEAR -> GarudaPuranTextKey.SOURCE_RIGHTS_UNCLEAR
    GarudaLicenseStatus.LICENSE_UNCERTAIN -> GarudaPuranTextKey.SOURCE_RIGHTS_LICENSE_UNCERTAIN
    GarudaLicenseStatus.RESTRICTED -> GarudaPuranTextKey.SOURCE_RESTRICTED
}

fun GarudaVerificationStatus.toTextKey(): GarudaPuranTextKey = when (this) {
    GarudaVerificationStatus.NOT_VERIFIED -> GarudaPuranTextKey.SOURCE_VERIFICATION_NOT_VERIFIED
    GarudaVerificationStatus.FILE_IDENTITY_VERIFIED -> GarudaPuranTextKey.SOURCE_VERIFICATION_FILE_IDENTITY
    GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED -> GarudaPuranTextKey.SOURCE_VERIFICATION_INDEX
    GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED -> GarudaPuranTextKey.SOURCE_VERIFICATION_CONTENT
    GarudaVerificationStatus.PARTIALLY_VERIFIED -> GarudaPuranTextKey.SOURCE_VERIFICATION_PARTIAL
}

fun GarudaContentReviewStatus.toTextKey(): GarudaPuranTextKey = when (this) {
    GarudaContentReviewStatus.UNREVIEWED -> GarudaPuranTextKey.SOURCE_REVIEW_UNREVIEWED
    GarudaContentReviewStatus.AUTO_NORMALIZED -> GarudaPuranTextKey.SOURCE_REVIEW_AUTO_NORMALIZED
    GarudaContentReviewStatus.EDITOR_REVIEW_REQUIRED -> GarudaPuranTextKey.SOURCE_REVIEW_EDITOR_REQUIRED
    GarudaContentReviewStatus.REVIEW_PENDING -> GarudaPuranTextKey.SOURCE_REVIEW_PENDING
    GarudaContentReviewStatus.SOURCE_VERIFIED -> GarudaPuranTextKey.SOURCE_REVIEW_SOURCE_VERIFIED
    GarudaContentReviewStatus.LICENSE_VERIFIED -> GarudaPuranTextKey.SOURCE_REVIEW_LICENSE_VERIFIED
    GarudaContentReviewStatus.APPROVED,
    GarudaContentReviewStatus.APPROVED_FOR_APP -> GarudaPuranTextKey.SOURCE_REVIEW_APPROVED_FOR_APP

    GarudaContentReviewStatus.REJECTED -> GarudaPuranTextKey.SOURCE_REVIEW_REJECTED
    GarudaContentReviewStatus.REFERENCE_ONLY -> GarudaPuranTextKey.SOURCE_REFERENCE_ONLY
}
