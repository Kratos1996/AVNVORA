package com.aynvora.core.garudapuran

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GarudaPuranSourceModelsTest {

    private val json = Json { prettyPrint = true }

    // 1. source manifest parsing
    @Test
    fun test1_sourceManifestParsing() {
        val manifest = sampleManifest()
        val encoded = json.encodeToString(manifest)
        val decoded = json.decodeFromString<GarudaSourceManifest>(encoded)

        assertEquals(manifest.manifestVersion, decoded.manifestVersion)
        assertEquals(manifest.sources.size, decoded.sources.size)
        assertEquals(manifest.sources[0].sourceId, decoded.sources[0].sourceId)
        assertEquals(
            manifest.sources[0].license.licenseName,
            decoded.sources[0].license.licenseName
        )
        assertEquals(manifest.packages.size, decoded.packages.size)
    }

    // 2. immutable metadata
    @Test
    fun test2_immutableMetadata() {
        val originalSources = mutableListOf(
            sampleSource(
                "src-1",
                "ed-1",
                GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
            )
        )
        val originalPackages = mutableListOf<GarudaContentPackageMetadata>()
        val manifest = GarudaSourceManifestFactory.create(1, originalSources, originalPackages)

        originalSources.clear()
        originalPackages.add(samplePackage("pkg-1", "src-1", "ed-1"))

        assertEquals(1, manifest.sources.size)
        assertTrue(manifest.packages.isEmpty())
        assertNotSame(originalSources, manifest.sources)
    }

    // 3. license-status modeling
    @Test
    fun test3_licenseStatusModeling() {
        val pdStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT
        assertTrue(pdStatus.isPermittedForRedistribution)

        val explicitStatus = GarudaLicenseStatus.LICENSE_EXPLICIT
        assertTrue(explicitStatus.isPermittedForRedistribution)

        val restrictedStatus = GarudaLicenseStatus.RESTRICTED
        assertFalse(restrictedStatus.isPermittedForRedistribution)

        val uncertainStatus = GarudaLicenseStatus.LICENSE_UNCERTAIN
        assertFalse(uncertainStatus.isPermittedForRedistribution)

        val license = GarudaSourceLicense(
            licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT,
            licenseName = "Public Domain (1911)",
            commercialUseAllowed = true,
            redistributionAllowed = true,
            derivativeAllowed = true,
            attributionRequired = true,
            attributionText = "The Panini Office, Allahabad",
        )
        assertTrue(license.commercialUseAllowed)
        assertTrue(license.redistributionAllowed)
        assertFalse(license.permissionRequired)
    }

    // 4. uncertain-license handling
    @Test
    fun test4_uncertainLicenseHandling() {
        val uncertainRights = GarudaSourceRights(
            sourceId = "src-uncertain",
            editionId = "ed-uncertain",
            licenseStatus = GarudaLicenseStatus.LICENSE_UNCERTAIN,
            rightsStatus = GarudaRightsStatus.LICENSE_UNCERTAIN,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            note = "No rights statement located in file",
        )

        assertFalse(uncertainRights.rightsStatus.isDistributable)
        assertTrue(uncertainRights.rightsStatus.isReferenceOnly)
        assertFalse(uncertainRights.redistributionAllowed)
        assertFalse(uncertainRights.commercialUseAllowed)

        // Cannot install package with LICENSE_UNCERTAIN
        assertFailsWith<IllegalArgumentException> {
            GarudaContentPackageMetadata(
                packageId = "pkg-uncertain",
                packageVersion = "1.0.0",
                sourceId = "src-uncertain",
                editionId = "ed-uncertain",
                language = "hi",
                checksumSha256 = "a".repeat(64),
                installationStatus = GarudaPackageInstallationStatus.INSTALLED,
                verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
                rightsStatus = GarudaRightsStatus.LICENSE_UNCERTAIN,
            )
        }
    }

    // 5. non-distributable source exclusion
    @Test
    fun test5_nonDistributableSourceExclusion() {
        listOf(
            GarudaRightsStatus.RESTRICTED,
            GarudaRightsStatus.REFERENCE_ONLY,
            GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
            GarudaRightsStatus.RIGHTS_UNVERIFIED,
        ).forEach { nonDistributableStatus ->
            assertFalse(nonDistributableStatus.isDistributable)
            assertTrue(nonDistributableStatus.isReferenceOnly)

            assertFailsWith<IllegalArgumentException> {
                GarudaContentPackageMetadata(
                    packageId = "pkg-excluded",
                    packageVersion = "1.0.0",
                    sourceId = "src-1",
                    editionId = "ed-1",
                    language = "hi",
                    checksumSha256 = "b".repeat(64),
                    installationStatus = GarudaPackageInstallationStatus.INSTALLED,
                    verificationStatus = GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED,
                    rightsStatus = nonDistributableStatus,
                )
            }
        }
    }

    // 6. approved source eligibility
    @Test
    fun test6_approvedSourceEligibility() {
        // Valid approved source succeeds
        val validApproved =
            sampleSource("src-wood", "ed-wood", GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
        assertEquals(GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION, validApproved.rightsStatus)
        assertTrue(validApproved.redistributionAllowed)
        assertTrue(validApproved.commercialUse)

        // Cannot be APPROVED_FOR_DISTRIBUTION with RESTRICTED license
        assertFailsWith<IllegalArgumentException> {
            GarudaSourceRights(
                sourceId = "src-invalid",
                editionId = "ed-invalid",
                licenseStatus = GarudaLicenseStatus.RESTRICTED,
                rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
                verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            )
        }

        // Cannot be APPROVED_FOR_DISTRIBUTION with LICENSE_UNCERTAIN
        assertFailsWith<IllegalArgumentException> {
            GarudaSourceRights(
                sourceId = "src-invalid",
                editionId = "ed-invalid",
                licenseStatus = GarudaLicenseStatus.LICENSE_UNCERTAIN,
                rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
                verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            )
        }
    }

    // 7. checksum calculation
    @Test
    fun test7_checksumCalculation() {
        // FIPS 180-4 standard test vector: empty string
        val emptyDigest = GarudaChecksumVerifier.calculateSha256(ByteArray(0))
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            emptyDigest
        )

        // FIPS 180-4 standard test vector: "abc"
        val abcDigest = GarudaChecksumVerifier.calculateSha256("abc")
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", abcDigest)
    }

    // 8. checksum mismatch detection
    @Test
    fun test8_checksumMismatchDetection() {
        val payload = "Garuda Purana Package Test Payload".encodeToByteArray()
        val actualSha256 = GarudaChecksumVerifier.calculateSha256(payload)
        val tamperedSha256 = "0".repeat(64)

        assertTrue(GarudaChecksumVerifier.verifyChecksum(actualSha256, payload))
        assertFalse(GarudaChecksumVerifier.verifyChecksum(tamperedSha256, payload))

        assertFailsWith<IllegalArgumentException> {
            GarudaChecksumVerifier.verifyChecksum("too_short", payload)
        }
    }

    // 9. version metadata
    @Test
    fun test9_versionMetadata() {
        val packageMetadata = GarudaContentPackageMetadata(
            packageId = "pkg-garuda-en-v1",
            packageVersion = "1.0.0",
            sourceId = "src-wood",
            editionId = "ed-wood",
            language = "en",
            checksumSha256 = "c".repeat(64),
            installationStatus = GarudaPackageInstallationStatus.NOT_INSTALLED,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
            updateTimestampEpochMs = 1774000000000L,
            rollbackCompatible = true,
        )

        assertEquals("1.0.0", packageMetadata.packageVersion)
        assertTrue(packageMetadata.rollbackCompatible)
        assertEquals(1774000000000L, packageMetadata.updateTimestampEpochMs)

        // Blank version throws
        assertFailsWith<IllegalArgumentException> {
            packageMetadata.copy(packageVersion = "   ")
        }

        // Invalid manifest version throws
        assertFailsWith<IllegalArgumentException> {
            GarudaSourceManifest(manifestVersion = 0, sources = emptyList())
        }
    }

    // 10. edition-specific chapter mapping
    @Test
    fun test10_editionSpecificChapterMapping() {
        // Gita Press Code 1416 Saroddhara has 17 TOC entries (or 16 chapters in preface)
        val gpChapter17 = GarudaChapter(
            sourceId = "garuda-puran-s2-gitapress-pdf",
            editionId = "gitapress-garuda-puran-saroddhar-code-1416",
            chapterNumber = 17,
            title = "गरुडपुराण-श्रवणका फल",
            language = "hi",
            pageRange = GarudaPageRange(
                pdfPageStart = 270,
                pdfPageEnd = 273,
                printedPageStart = 269,
                printedPageEnd = 272
            ),
            referenceRange = "17.1-17.16",
            licenseStatus = GarudaLicenseStatus.RESTRICTED,
            rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
        )

        // Wood & Subrahmanyam 1911 Saroddhara has 16 chapters (closing verses within Ch 16)
        val woodChapter16 = GarudaChapter(
            sourceId = "garuda-puran-wood-1911-en",
            editionId = "paninioffice-garuda-puran-saroddhar-1911",
            chapterNumber = 16,
            title = "An Account of the Liberation of the Soul",
            language = "en",
            pageRange = GarudaPageRange(pdfPageStart = 150, pdfPageEnd = 169),
            referenceRange = "16.1-16.120",
            licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
        )

        // Summary PDF only has 2 chapters (ch 2 incomplete)
        val summaryChapter1 = GarudaChapter(
            sourceId = "garuda-puran-s1-summary-pdf",
            editionId = "garuda-puran-s1-edition-unspecified",
            chapterNumber = 1,
            title = "पहला अध्याय",
            language = "hi",
            pageRange = GarudaPageRange(pdfPageStart = 3, pdfPageEnd = 8),
            licenseStatus = GarudaLicenseStatus.LICENSE_UNCERTAIN,
            rightsStatus = GarudaRightsStatus.LICENSE_UNCERTAIN,
        )

        // Edition-specific numbering and chapter IDs are strictly distinct
        assertEquals("ch_garuda-puran-s2-gitapress-pdf_17", gpChapter17.chapterId)
        assertEquals("ch_garuda-puran-wood-1911-en_16", woodChapter16.chapterId)
        assertEquals("ch_garuda-puran-s1-summary-pdf_1", summaryChapter1.chapterId)
        assertFalse(gpChapter17.chapterId == woodChapter16.chapterId)
        assertFalse(gpChapter17.rightsStatus == woodChapter16.rightsStatus)
    }

    // 11. duplicate source IDs
    @Test
    fun test11_duplicateSourceIds() {
        val s1 =
            sampleSource("duplicate-id", "edition-1", GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
        val s2 = sampleSource("duplicate-id", "edition-2", GarudaRightsStatus.REFERENCE_ONLY)

        assertFailsWith<IllegalArgumentException> {
            GarudaSourceManifest(manifestVersion = 1, sources = listOf(s1, s2))
        }

        val s3 = sampleSource(
            "source-3",
            "duplicate-edition",
            GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
        )
        val s4 = sampleSource("source-4", "duplicate-edition", GarudaRightsStatus.REFERENCE_ONLY)

        assertFailsWith<IllegalArgumentException> {
            GarudaSourceManifest(manifestVersion = 1, sources = listOf(s3, s4))
        }
    }

    // 12. missing mandatory metadata
    @Test
    fun test12_missingMandatoryMetadata() {
        // Blank source ID
        assertFailsWith<IllegalArgumentException> {
            sampleSource("   ", "edition-valid", GarudaRightsStatus.REFERENCE_ONLY)
        }

        // Blank edition ID
        assertFailsWith<IllegalArgumentException> {
            sampleSource("source-valid", "   ", GarudaRightsStatus.REFERENCE_ONLY)
        }

        // Invalid checksum (not 64 hex characters)
        assertFailsWith<IllegalArgumentException> {
            sampleSource(
                "source-valid",
                "edition-valid",
                GarudaRightsStatus.REFERENCE_ONLY,
                checksum = "invalid_hash"
            )
        }

        // Zero page count
        assertFailsWith<IllegalArgumentException> {
            GarudaEdition(editionId = "ed", title = "Title", language = "hi", pageCount = 0)
        }
    }

    // 13. provenance preservation
    @Test
    fun test13_provenancePreservation() {
        val pageRange = GarudaPageRange(
            pdfPageStart = 24,
            pdfPageEnd = 40,
            printedPageStart = 23,
            printedPageEnd = 39
        )
        val reference = GarudaSourceReference(
            sourceId = "garuda-puran-s2-gitapress-pdf",
            editionId = "gitapress-garuda-puran-saroddhar-code-1416",
            chapter = "2",
            section = "yamamarga",
            pageRange = pageRange,
            language = "hi",
            contentVersion = "1.0",
            rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
            verificationStatus = GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED,
            canonicalReferenceId = "GP_SARODDHARA_CH2",
            licenseStatus = GarudaLicenseStatus.RESTRICTED,
        )

        val map = reference.toProvenanceMap()
        assertEquals("GARUDA_PURAN", map["feature"])
        assertEquals("garuda-puran-s2-gitapress-pdf", map["sourceId"])
        assertEquals("gitapress-garuda-puran-saroddhar-code-1416", map["editionId"])
        assertEquals("2", map["chapter"])
        assertEquals("yamamarga", map["section"])
        assertEquals("GP_SARODDHARA_CH2", map["reference"])
        assertEquals("hi", map["language"])
        assertEquals("1.0", map["contentVersion"])
        assertEquals("RESTRICTED", map["licenseStatus"])
        assertEquals("INDEX_VISUALLY_VERIFIED", map["verificationStatus"])
    }

    // 14. language preservation
    @Test
    fun test14_languagePreservation() {
        val validPuranItem = samplePuranContentItem(language = "en")
        assertEquals("en", validPuranItem.languageCode)

        // Mismatched language between item and text throws in GarudaPuranContentItem
        assertFailsWith<IllegalArgumentException> {
            validPuranItem.copy(
                languageCode = "hi",
                text = validPuranItem.text.copy(languageCode = "en")
            )
        }

        // Unsupported language throws in GarudaPuranContentItem
        assertFailsWith<IllegalArgumentException> {
            validPuranItem.copy(
                languageCode = "fr",
                text = validPuranItem.text.copy(languageCode = "fr")
            )
        }

        // Also verify GarudaContentItem preserves language and originalLanguage
        val validContentItem = sampleContentItem(sourceLanguage = "en", originalLanguage = "sa")
        assertEquals("en", validContentItem.sourceLanguage)
        assertEquals("en", validContentItem.languageCode)
        assertEquals("sa", validContentItem.originalLanguage)
    }

    // 15. reference-only content never becoming distributable
    @Test
    fun test15_referenceOnlyContentNeverBecomingDistributable() {
        val pageRange = GarudaPageRange(10)
        val reference = GarudaReference(
            "REF_1",
            GarudaSourceReference(
                "src",
                "ed",
                chapter = "1",
                pageRange = pageRange,
                language = "hi",
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED
            )
        )

        // Attempting to mark reference-only content as APPROVED_FOR_APP throws
        assertFailsWith<IllegalArgumentException> {
            GarudaContentItem(
                contentId = "c1",
                sourceId = "src",
                editionId = "ed",
                chapterNumber = 1,
                sectionNumber = null,
                pageRange = pageRange,
                reference = reference,
                originalLanguage = "hi",
                contentType = GarudaNormalizedContentType.SOURCE_PASSAGE,
                contentVersion = "1.0",
                sourceStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                reviewStatus = GarudaContentReviewStatus.APPROVED_FOR_APP,
            )
        }

        // Attempting to mark uncertain license content as APPROVED throws
        assertFailsWith<IllegalArgumentException> {
            GarudaContentItem(
                contentId = "c2",
                sourceId = "src",
                editionId = "ed",
                chapterNumber = 1,
                sectionNumber = null,
                pageRange = pageRange,
                reference = reference,
                originalLanguage = "hi",
                contentType = GarudaNormalizedContentType.SOURCE_PASSAGE,
                contentVersion = "1.0",
                sourceStatus = GarudaSourceContentStatus.REFERENCE_ONLY,
                rightsStatus = GarudaRightsStatus.LICENSE_UNCERTAIN,
                reviewStatus = GarudaContentReviewStatus.APPROVED,
            )
        }
    }

    // 16. deterministic normalization
    @Test
    fun test16_deterministicNormalization() {
        val messyText = "   धर्मदृढबद्धमूलो    वेदस्कन्धः \r\n  पुराणशाखाढ्यः   "
        val result = GarudaTextNormalizer.normalize(messyText)

        assertEquals("धर्मदृढबद्धमूलो वेदस्कन्धः \n पुराणशाखाढ्यः", result.normalizedText)
        assertTrue(result.isTransformed)
        assertEquals(
            "Deterministic whitespace and newline normalization applied.",
            result.transformationNote
        )

        // Normalizing again is completely idempotent and reports isTransformed = false
        val cleanResult = GarudaTextNormalizer.normalize(result.normalizedText)
        assertEquals(result.normalizedText, cleanResult.normalizedText)
        assertFalse(cleanResult.isTransformed)
        assertNull(cleanResult.transformationNote)
    }

    // 17. no silent source substitution
    @Test
    fun test17_noSilentSourceSubstitution() {
        val woodSource = sampleSource(
            "garuda-puran-wood-1911-en",
            "paninioffice-garuda-puran-saroddhar-1911",
            GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
        )
        val gpSource = sampleSource(
            "garuda-puran-s2-gitapress-pdf",
            "gitapress-garuda-puran-saroddhar-code-1416",
            GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE
        )

        val manifest = GarudaSourceManifest(1, listOf(woodSource, gpSource))

        val retrievedWood = manifest.getSource("garuda-puran-wood-1911-en")
        assertEquals("paninioffice-garuda-puran-saroddhar-1911", retrievedWood?.editionId)

        // Querying Wood source must never return Gita Press edition
        assertFalse(retrievedWood?.editionId == gpSource.editionId)

        val approvedSources = manifest.getApprovedSources()
        assertEquals(1, approvedSources.size)
        assertEquals("garuda-puran-wood-1911-en", approvedSources.single().sourceId)

        val referenceOnly = manifest.getReferenceOnlySources()
        assertEquals(1, referenceOnly.size)
        assertEquals("garuda-puran-s2-gitapress-pdf", referenceOnly.single().sourceId)
    }

    // Additional helper test for Verse & Sanskrit/Translation concept separation
    @Test
    fun verseRejectsAynvoraExplanationRepresentedAsScripture() {
        assertFailsWith<IllegalArgumentException> {
            GarudaVerse(
                verseId = "v1",
                sourceId = "src",
                editionId = "ed",
                chapterNumber = 1,
                verseNumber = 1,
                originalText = "Some Sanskrit shloka text",
                textCategory = GarudaTextCategory.AYNVORA_EXPLANATION,
            )
        }

        val legitimateVerse = GarudaVerse(
            verseId = "v1",
            sourceId = "src-wood",
            editionId = "ed-wood",
            chapterNumber = 1,
            verseNumber = 1,
            originalText = "धर्मदृढबद्धमूलो वेदस्कन्धः...",
            textCategory = GarudaTextCategory.ORIGINAL_SOURCE_TEXT,
            licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT,
        )
        assertTrue(legitimateVerse.textCategory.isScriptureText)
        assertFalse(legitimateVerse.textCategory.isExplanation)
    }

    private fun sampleManifest(): GarudaSourceManifest {
        val src = sampleSource("src-test", "ed-test", GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION)
        val pkg = samplePackage("pkg-test", "src-test", "ed-test")
        return GarudaSourceManifest(1, listOf(src), listOf(pkg))
    }

    private fun sampleSource(
        sourceId: String,
        editionId: String,
        rightsStatus: GarudaRightsStatus,
        checksum: String = "d".repeat(64),
    ): GarudaSource {
        val licenseStatus = if (rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION) {
            GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT
        } else {
            GarudaLicenseStatus.RESTRICTED
        }
        val isDistributable = rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
        return GarudaSource(
            sourceId = sourceId,
            editionId = editionId,
            edition = GarudaEdition(editionId, "Edition Title", "en", pageCount = 100),
            sourcePathOrReference = "ref:$sourceId",
            checksumSha256 = checksum,
            fileSizeBytes = 500_000L,
            pageCount = 100,
            textBearingPageCount = 100,
            noTextLayerPageCount = 0,
            embeddedImageObjectCount = 0,
            textExtractionStatus = GarudaTextExtractionStatus.CLEAN_TEXT_LAYER,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            rights = GarudaSourceRights(
                sourceId = sourceId,
                editionId = editionId,
                licenseStatus = licenseStatus,
                rightsStatus = rightsStatus,
                verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
                commercialUseAllowed = isDistributable,
                redistributionAllowed = isDistributable,
                derivativeAllowed = isDistributable,
            ),
            license = GarudaSourceLicense(
                licenseStatus = licenseStatus,
                licenseName = if (isDistributable) "Public Domain" else "Restricted",
                commercialUseAllowed = isDistributable,
                redistributionAllowed = isDistributable,
                derivativeAllowed = isDistributable,
                attributionRequired = true,
            ),
        )
    }

    private fun samplePackage(packageId: String, sourceId: String, editionId: String) =
        GarudaContentPackageMetadata(
            packageId = packageId,
            packageVersion = "1.0.0",
            sourceId = sourceId,
            editionId = editionId,
            language = "en",
            checksumSha256 = "e".repeat(64),
            installationStatus = GarudaPackageInstallationStatus.NOT_INSTALLED,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
        )

    private fun samplePuranContentItem(
        language: String = "en",
    ): GarudaPuranContentItem {
        val pageRange = GarudaPageRange(5)
        val srcRef = GarudaSourceReference(
            sourceId = "garuda-puran-wood-1911-en",
            editionId = "wood-1911-saroddhara",
            chapter = "1",
            pageRange = pageRange,
            language = language,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
        )
        return GarudaPuranContentItem(
            contentId = "puran-test-1",
            topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
            section = GarudaPuranSection("sec-1", "Section 1", 1),
            reference = GarudaPuranReference(
                canonicalReferenceId = "REF_1",
                chapterNumber = 1,
                sourceProvenance = srcRef,
            ),
            sourceEdition = GarudaPuranSourceEdition(
                "wood-1911-saroddhara",
                "Wood 1911",
                "Panini Office",
                "en"
            ),
            text = GarudaPuranText(language, "मूल श्लोक", "Mula Shloka", "Meaning", "Presentation"),
            contentVersion = 1,
            languageCode = language,
            contentType = GarudaPuranContentType.SOURCE_PASSAGE,
        )
    }

    private fun sampleContentItem(
        sourceLanguage: String = "en",
        originalLanguage: String = "sa",
    ): GarudaContentItem {
        val pageRange = GarudaPageRange(5)
        val srcRef = GarudaSourceReference(
            sourceId = "garuda-puran-wood-1911-en",
            editionId = "wood-1911-saroddhara",
            chapter = "1",
            pageRange = pageRange,
            language = sourceLanguage,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
            verificationStatus = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
        )
        return GarudaContentItem(
            contentId = "content-test-1",
            sourceId = "garuda-puran-wood-1911-en",
            editionId = "wood-1911-saroddhara",
            chapterNumber = 1,
            sectionNumber = 1,
            pageRange = pageRange,
            reference = GarudaReference("REF_1", srcRef),
            originalLanguage = originalLanguage,
            sourceLanguage = sourceLanguage,
            languageCode = sourceLanguage,
            contentType = GarudaNormalizedContentType.SOURCE_PASSAGE,
            contentVersion = "1.0",
            sourceStatus = GarudaSourceContentStatus.APPROVED,
            rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
            reviewStatus = GarudaContentReviewStatus.APPROVED_FOR_APP,
            originalText = "Some Sanskrit shloka",
            normalizedText = "Some Sanskrit shloka",
            textCategory = GarudaTextCategory.ORIGINAL_SOURCE_TEXT,
            licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_CLAIM_EXPLICIT,
        )
    }
}
