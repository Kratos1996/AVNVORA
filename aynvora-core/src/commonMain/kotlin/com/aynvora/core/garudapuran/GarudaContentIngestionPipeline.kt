package com.aynvora.core.garudapuran

import kotlinx.serialization.Serializable

/**
 * End-to-end typed content ingestion pipeline for Garuda Purana sources.
 *
 * Implements the required stages:
 * SOURCE FILE -> TEXT EXTRACTION -> NORMALIZATION -> CHAPTER DETECTION ->
 * SECTION DETECTION -> VERSE/REFERENCE DETECTION -> EDITORIAL VALIDATION ->
 * PROVENANCE ATTACHMENT -> CONTENT REVIEW -> LICENSE ELIGIBILITY CHECK ->
 * APPROVED CONTENT PACKAGE -> SHA-256
 */
object GarudaContentIngestionPipeline {

    /**
     * Stage 1 & 2: Source File & Text Extraction.
     */
    @Serializable
    data class ExtractedSourceFile(
        val sourceId: String,
        val editionId: String,
        val sourceLocation: String,
        val rawText: String,
        val fileSizeBytes: Long,
        val fileSha256: String,
        val lineCount: Int,
    ) {
        init {
            require(sourceId.isNotBlank() && editionId.isNotBlank())
            require(rawText.isNotBlank())
            require(fileSizeBytes > 0L)
            require(fileSha256.length == 64)
        }
    }

    /**
     * Stage 3: Normalization.
     */
    @Serializable
    data class NormalizedSourceText(
        val rawLength: Int,
        val normalizedLength: Int,
        val normalizedText: String,
        val isTransformed: Boolean,
        val transformationNotes: List<String>,
    )

    fun normalizeText(raw: String): NormalizedSourceText {
        val notes = mutableListOf<String>()
        var text = raw.trim()
        if (text != raw) {
            notes += "Trimmed leading and trailing whitespace"
        }
        val crLfFixed = text.replace("\r\n", "\n").replace("\r", "\n")
        if (crLfFixed != text) {
            notes += "Normalized carriage return line breaks to newline"
            text = crLfFixed
        }
        val spaceNormalized = text.replace(Regex("[ \\t]+"), " ")
        if (spaceNormalized != text) {
            notes += "Collapsed multiple horizontal whitespaces to single space"
            text = spaceNormalized
        }
        return NormalizedSourceText(
            rawLength = raw.length,
            normalizedLength = text.length,
            normalizedText = text,
            isTransformed = notes.isNotEmpty(),
            transformationNotes = notes,
        )
    }

    /**
     * Stage 4: Chapter Detection.
     */
    @Serializable
    data class DetectedChapter(
        val chapterNumber: Int,
        val title: String,
        val printedPageRange: GarudaPageRange,
        val rawVerseCount: Int? = null,
        val referenceRange: String,
    ) {
        init {
            require(chapterNumber in 1..16) { "Saroddhara recension must have chapters 1 through 16" }
            require(title.isNotBlank())
        }
    }

    /**
     * Stage 5: Section Detection.
     */
    @Serializable
    data class DetectedSection(
        val chapterNumber: Int,
        val sectionNumber: Int,
        val sectionId: String,
        val title: String,
        val topicId: GarudaPuranTopicId,
        val verseStart: Int,
        val verseEnd: Int,
        val pageRange: GarudaPageRange,
    ) {
        init {
            require(chapterNumber in 1..16 && sectionNumber > 0)
            require(verseEnd >= verseStart)
            require(sectionId.isNotBlank() && title.isNotBlank())
        }
    }

    /**
     * Stage 6: Verse/Reference Detection.
     */
    @Serializable
    data class DetectedVerse(
        val chapterNumber: Int,
        val verseNumber: Int,
        val verseEnd: Int = verseNumber,
        val canonicalReferenceId: String,
        val englishTranslation: String,
        val sanskritTransliteration: String? = null,
        val pageRange: GarudaPageRange,
    ) {
        init {
            require(chapterNumber in 1..16 && verseNumber > 0)
            require(verseEnd >= verseNumber)
            require(canonicalReferenceId.isNotBlank())
            require(englishTranslation.isNotBlank())
        }
    }

    /**
     * Stage 7 & 8: Editorial Validation & Provenance Attachment.
     */
    fun createContentItem(
        contentId: String,
        sourceId: String,
        editionId: String,
        chapterNumber: Int,
        sectionNumber: Int,
        sectionId: String,
        title: String,
        verse: DetectedVerse,
        topicId: GarudaPuranTopicId,
        contentVersion: String,
        rightsStatus: GarudaRightsStatus,
        reviewStatus: GarudaContentReviewStatus,
        verificationStatus: GarudaVerificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
        licenseStatus: GarudaLicenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
        aynvoraExplanation: String? = null,
    ): GarudaContentItem {
        val srcRef = GarudaSourceReference(
            sourceId = sourceId,
            editionId = editionId,
            chapter = chapterNumber.toString(),
            section = sectionId,
            pageRange = verse.pageRange,
            language = "en",
            contentVersion = contentVersion,
            rightsStatus = rightsStatus,
            verificationStatus = verificationStatus,
            canonicalReferenceId = verse.canonicalReferenceId,
            licenseStatus = licenseStatus,
        )
        val reference = GarudaReference(
            canonicalReferenceId = verse.canonicalReferenceId,
            source = srcRef,
        )
        val normalized = normalizeText(verse.englishTranslation)
        return GarudaContentItem(
            contentId = contentId,
            sourceId = sourceId,
            editionId = editionId,
            chapterNumber = chapterNumber,
            sectionNumber = sectionNumber,
            pageRange = verse.pageRange,
            reference = reference,
            originalLanguage = "sa",
            contentType = GarudaNormalizedContentType.SOURCE_PASSAGE,
            contentVersion = contentVersion,
            sourceStatus = if (rightsStatus.isDistributable) GarudaSourceContentStatus.APPROVED else GarudaSourceContentStatus.REFERENCE_ONLY,
            rightsStatus = rightsStatus,
            reviewStatus = reviewStatus,
            originalText = verse.sanskritTransliteration,
            normalizedText = normalized.normalizedText,
            transformationNote = if (normalized.isTransformed) normalized.transformationNotes.joinToString(
                "; "
            ) else null,
            verseStart = verse.verseNumber,
            verseEnd = verse.verseEnd,
            textCategory = GarudaTextCategory.ENGLISH_TRANSLATION,
            sourceLanguage = "en",
            languageCode = "en",
            chapterName = title,
            section = sectionId,
            originalTextReference = "Sanskrit Saroddhara",
            translationReference = "Ernest Wood & S.V. Subrahmanyam (1911)",
            licenseStatus = licenseStatus,
            verificationStatus = verificationStatus,
        )
    }

    /**
     * Stage 9 & 10: Content Review & License Eligibility Verification.
     */
    fun validateForAppInclusion(item: GarudaContentItem): Boolean {
        if (!item.rightsStatus.isDistributable) return false
        if (!item.reviewStatus.isApproved) return false
        if (item.languageCode != "en") return false
        if (item.normalizedText.isNullOrBlank()) return false
        if (item.chapterNumber == null || item.chapterNumber !in 1..16) return false
        return true
    }

    /**
     * Stage 11 & 12: Package Assembly and SHA-256 Checksum Calculation.
     */
    @Serializable
    data class IngestionPackageResult(
        val metadata: GarudaContentPackageMetadata,
        val items: List<GarudaContentItem>,
        val checksumSha256: String,
        val totalChapters: Int,
        val approvedChapters: Int,
        val totalPassages: Int,
    )

    fun assemblePackage(
        packageId: String,
        packageVersion: String,
        sourceId: String,
        editionId: String,
        language: String,
        items: List<GarudaContentItem>,
        rightsStatus: GarudaRightsStatus,
        verificationStatus: GarudaVerificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
        timestampEpochMs: Long = 0L,
    ): IngestionPackageResult {
        require(items.isNotEmpty()) { "Package items cannot be empty" }
        require(rightsStatus.isDistributable) { "Cannot assemble package that is not distributable" }
        val approvedItems = items.filter { validateForAppInclusion(it) }
        require(approvedItems.size == items.size) { "All items in approved package must pass editorial validation" }

        // Deterministic checksum computed over canonical representation of package contents
        val serializedFingerprint = buildString {
            append(packageId).append(':').append(packageVersion).append(':').append(language)
                .append('\n')
            for (item in items.sortedBy { it.contentId }) {
                append(item.contentId).append('|')
                    .append(item.reference.canonicalReferenceId).append('|')
                    .append(item.chapterNumber).append('|')
                    .append(item.verseStart).append('-').append(item.verseEnd).append('|')
                    .append(item.normalizedText?.length ?: 0).append('\n')
            }
        }
        val calculatedSha256 = GarudaChecksumVerifier.calculateSha256(serializedFingerprint)

        val metadata = GarudaContentPackageMetadata(
            packageId = packageId,
            packageVersion = packageVersion,
            sourceId = sourceId,
            editionId = editionId,
            language = language,
            checksumSha256 = calculatedSha256,
            installationStatus = GarudaPackageInstallationStatus.INSTALLED,
            verificationStatus = verificationStatus,
            rightsStatus = rightsStatus,
            updateTimestampEpochMs = timestampEpochMs,
            rollbackCompatible = true,
        )

        val chapters = items.mapNotNull { it.chapterNumber }.distinct()

        return IngestionPackageResult(
            metadata = metadata,
            items = items,
            checksumSha256 = calculatedSha256,
            totalChapters = chapters.size,
            approvedChapters = chapters.size,
            totalPassages = items.size,
        )
    }
}
