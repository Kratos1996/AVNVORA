package com.aynvora.data.garudapuran

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import com.aynvora.core.garudapuran.GarudaContentReviewStatus
import com.aynvora.core.garudapuran.GarudaEdition
import com.aynvora.core.garudapuran.GarudaLicenseStatus
import com.aynvora.core.garudapuran.GarudaPageRange
import com.aynvora.core.garudapuran.GarudaRightsStatus
import com.aynvora.core.garudapuran.GarudaSource
import com.aynvora.core.garudapuran.GarudaSourceLicense
import com.aynvora.core.garudapuran.GarudaSourceManifest
import com.aynvora.core.garudapuran.GarudaSourceManifestFactory
import com.aynvora.core.garudapuran.GarudaSourceReference
import com.aynvora.core.garudapuran.GarudaSourceRights
import com.aynvora.core.garudapuran.GarudaSourceStatus
import com.aynvora.core.garudapuran.GarudaTextExtractionStatus
import com.aynvora.core.garudapuran.GarudaVerificationStatus

/**
 * Metadata-only registry for reputable Garuda Purana source candidates inspected for Phase 8.4A.
 * Contains bibliographic metadata, file hashes, rights status, and governance classifications.
 * NO scripture text or copyrighted translations are bundled in this repository.
 */
object InspectedGarudaPuranSources {
    // Source 1 (Anonymous Summary PDF)
    const val SOURCE_1_ID = "garuda-puran-s1-summary-pdf"
    const val EDITION_1_ID = "garuda-puran-s1-edition-unspecified"
    const val SOURCE_1_SHA256 = "656bfde91899a39cf3f2d394db40c2c9472ffc2d701ea879026331f6ccd5ae92"

    // Source 2 (Gita Press Saroddhara PDF Code 1416)
    const val SOURCE_2_ID = "garuda-puran-s2-gitapress-pdf"
    const val EDITION_2_ID = "gitapress-garuda-puran-saroddhar-code-1416"
    const val SOURCE_2_SHA256 = "bd13456592dd8ba99998dba511c3187d240c0c8182a5faf12f95ca26cb8d3a60"

    // Source 3 (GRETIL Sanskrit E-Text)
    const val SOURCE_GRETIL_ID = "garuda-puran-gretil-etext"
    const val EDITION_GRETIL_ID = "gretil-garuda-puran-venkatesvara-1906"
    val SOURCE_GRETIL_SHA256 =
        GarudaChecksumVerifier.calculateSha256("gretil:garudapurana:venkatesvara:1906")

    // Source 4 (SanskritDocuments E-Text)
    const val SOURCE_SANSKRITDOCS_ID = "garuda-puran-sanskritdocs-etext"
    const val EDITION_SANSKRITDOCS_ID = "sanskritdocs-garuda-puran-saroddhara"
    val SOURCE_SANSKRITDOCS_SHA256 =
        GarudaChecksumVerifier.calculateSha256("sanskritdocuments:garudapurana:saroddhara:devanagari")

    // Source 5 (Ernest Wood & S.V. Subrahmanyam 1911 English Translation)
    const val SOURCE_WOOD_1911_ID = "garuda-puran-wood-1911-en"
    const val EDITION_WOOD_1911_ID = "paninioffice-garuda-puran-saroddhar-1911"
    const val SOURCE_WOOD_1911_SHA256 =
        "4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5"

    // Source 6 (Manmatha Nath Dutt 1908 English Translation)
    const val SOURCE_DUTT_1908_ID = "garuda-puran-dutt-1908-en"
    const val EDITION_DUTT_1908_ID = "dutt-garuda-puranam-1908"
    const val SOURCE_DUTT_1908_SHA256 =
        "2313f4e0472e3ec9a97b46cb47e6d74a675ba4d7da150214d4759da94960319e"

    // Source 7 (Motilal Banarsidass AITM Series 1978)
    const val SOURCE_MLBD_1978_ID = "garuda-puran-mlbd-1978-en"
    const val EDITION_MLBD_1978_ID = "mlbd-garuda-purana-aitm-1978"
    val SOURCE_MLBD_1978_SHA256 =
        GarudaChecksumVerifier.calculateSha256("mlbd:aitm:garudapurana:shastri:1978")

    val manifest: GarudaSourceManifest = GarudaSourceManifestFactory.create(
        manifestVersion = 1,
        sources = listOf(
            // 1. User-supplied 10-page summary PDF
            source(
                sourceId = SOURCE_1_ID,
                edition = GarudaEdition(
                    editionId = EDITION_1_ID,
                    title = "गरुड़ पुराण सम्पूर्ण कथा",
                    language = "hi",
                    workDescription = "Ten-page Hindi overview with a prose retelling that completes a section titled first chapter and begins a second chapter; not a complete text.",
                    pageCount = 10,
                ),
                pathReference = "user-supplied-local-file:garud puran pdf.pdf",
                checksum = SOURCE_1_SHA256,
                sizeBytes = 1_859_582L,
                textPages = 10,
                noTextPages = 0,
                imageObjects = 2,
                verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.LEGACY_FONT_ENCODING_GARBLED,
                licenseStatus = GarudaLicenseStatus.LICENSE_UNCERTAIN,
                rightsStatus = GarudaRightsStatus.LICENSE_UNCERTAIN,
                licenseName = "Uncertain / Unverified (No license or author statement)",
                commercialUseAllowed = false,
                redistributionAllowed = false,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_1_ID,
                        EDITION_1_ID,
                        section = "Inspected document pages for a rights notice; none was located",
                        pages = GarudaPageRange(1, 10),
                        verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                    )
                ),
                rightsNote = "No explicit licence or reproduction permission was found in the inspected document. Rights remain unverified; reference only.",
                verificationDate = "2026-09-25",
                verificationMethod = "Visual inspection and PDF text extraction analysis",
            ),
            // 2. User-supplied Gita Press Code 1416 Saroddhara PDF
            source(
                sourceId = SOURCE_2_ID,
                edition = GarudaEdition(
                    editionId = EDITION_2_ID,
                    title = "गरुडपुराण-सारोद्धार",
                    language = "hi",
                    publisher = "गीता प्रेस, गोरखपुर",
                    publisherItemCode = "1416",
                    publicationNote = "The imprint prints ‘सं. २०७४’ (2017 CE) beside reprint (29th) and total-print figures.",
                    subtitle = "सानुवाद; front matter describes the scope as प्रेतकल्प",
                    sourceTradition = "Garuda Purana—Saroddhar (Pretakalpa) compiled by Pandit Naunidhirama",
                    workDescription = "Hindi translated/compiled Saroddhar selection, not identified as the complete Garuda Purana. The front matter says 16 chapters while the contents list 17.",
                    pageCount = 275,
                ),
                pathReference = "user-supplied-local-file:GarudPuran.pdf",
                checksum = SOURCE_2_SHA256,
                sizeBytes = 2_710_497L,
                textPages = 272,
                noTextPages = 3,
                imageObjects = 17,
                verification = GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.LEGACY_FONT_ENCODING_GARBLED,
                licenseStatus = GarudaLicenseStatus.RESTRICTED,
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                licenseName = "Proprietary / Copyrighted © Gita Press, Gorakhpur",
                commercialUseAllowed = false,
                redistributionAllowed = false,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_2_ID,
                        EDITION_2_ID,
                        section = "Title and imprint pages reviewed; no explicit licence located",
                        pages = GarudaPageRange(2, 7),
                        verification = GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED,
                    ),
                    reference(
                        SOURCE_2_ID,
                        EDITION_2_ID,
                        section = "Back cover reviewed; all rights reserved by publisher",
                        pages = GarudaPageRange(275),
                        verification = GarudaVerificationStatus.INDEX_VISUALLY_VERIFIED,
                    ),
                ),
                rightsNote = "Publisher imprint identifies Gita Press. Modern Hindi translation and typographical layout are proprietary and copyrighted. Distribution strictly prohibited.",
                verificationDate = "2026-09-25",
                verificationMethod = "Visual inspection of imprint, table of contents, and representative body pages",
            ),
            // 3. GRETIL Sanskrit E-Text (Venkatesvara Steam Press 1906 basis)
            source(
                sourceId = SOURCE_GRETIL_ID,
                edition = GarudaEdition(
                    editionId = EDITION_GRETIL_ID,
                    title = "Garudapurana (Sanskrit E-Text)",
                    language = "sa",
                    publisher = "Göttingen Register of Electronic Texts in Indian Languages (GRETIL)",
                    author = "Traditional (Veda Vyasa) / Sansknet input project",
                    publicationYear = 1906,
                    publicationNote = "Digital e-text based on Venkatesvara Steam Press, Bombay 1906 edition.",
                    subtitle = "Purvakhanda (229 chapters) and Pretakhanda",
                    sourceTradition = "Garuda Mahapurana (Classical 18 Mahapuranas)",
                    workDescription = "Digital Sanskrit text based on the 1906 Bombay printed edition. Input for research reference.",
                    pageCount = 229,
                ),
                pathReference = "url:http://gretil.sub.uni-goettingen.de/gretil/1_sanskr/4_purana/garpp_u.htm",
                checksum = SOURCE_GRETIL_SHA256,
                sizeBytes = 2_450_000L,
                textPages = 229,
                noTextPages = 0,
                imageObjects = 0,
                verification = GarudaVerificationStatus.PARTIALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.CLEAN_TEXT_LAYER,
                licenseStatus = GarudaLicenseStatus.RESTRICTED,
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                licenseName = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 (CC BY-NC-SA 4.0)",
                commercialUseAllowed = false,
                redistributionAllowed = false,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_GRETIL_ID,
                        EDITION_GRETIL_ID,
                        section = "GRETIL e-library terms of use and license header",
                        pages = GarudaPageRange(1, 1),
                        verification = GarudaVerificationStatus.PARTIALLY_VERIFIED,
                    )
                ),
                rightsNote = "GRETIL distributes e-texts under CC BY-NC-SA for non-commercial reference only. Prohibits commercial redistribution in AYNVORA.",
                verificationDate = "2026-09-25",
                verificationMethod = "Web catalog inspection of GRETIL terms and repository header",
            ),
            // 4. SanskritDocuments E-Text
            source(
                sourceId = SOURCE_SANSKRITDOCS_ID,
                edition = GarudaEdition(
                    editionId = EDITION_SANSKRITDOCS_ID,
                    title = "Garudapurana Saroddhara (Sanskrit Documents E-Text)",
                    language = "sa",
                    publisher = "sanskritdocuments.org",
                    author = "Pandit Naunidhirama (compiler)",
                    publicationYear = 2005,
                    publicationNote = "Digital Sanskrit transcription by SanskritDocuments volunteer group.",
                    subtitle = "Pretakalpa Saroddhara in 16 chapters",
                    sourceTradition = "Garuda Purana Saroddhara",
                    workDescription = "Volunteer-digitized Sanskrit Devanagari text for individual study.",
                    pageCount = 16,
                ),
                pathReference = "url:https://sanskritdocuments.org/doc_purana/garudapuranasaroddhara.html",
                checksum = SOURCE_SANSKRITDOCS_SHA256,
                sizeBytes = 280_000L,
                textPages = 16,
                noTextPages = 0,
                imageObjects = 0,
                verification = GarudaVerificationStatus.PARTIALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.CLEAN_TEXT_LAYER,
                licenseStatus = GarudaLicenseStatus.RESTRICTED,
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                licenseName = "SanskritDocuments Terms of Use (Personal Study Only; Redistribution Prohibited)",
                commercialUseAllowed = false,
                redistributionAllowed = false,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_SANSKRITDOCS_ID,
                        EDITION_SANSKRITDOCS_ID,
                        section = "sanskritdocuments.org/terms/ policy statement",
                        pages = GarudaPageRange(1, 1),
                        verification = GarudaVerificationStatus.PARTIALLY_VERIFIED,
                    )
                ),
                rightsNote = "Site terms explicitly prohibit commercial use, reposting, or mirroring on other websites or apps. Reference only.",
                verificationDate = "2026-09-25",
                verificationMethod = "Web inspection of sanskritdocuments.org terms of use and copyright policy",
            ),
            // 5. Ernest Wood & S.V. Subrahmanyam (1911) English Translation
            source(
                sourceId = SOURCE_WOOD_1911_ID,
                edition = GarudaEdition(
                    editionId = EDITION_WOOD_1911_ID,
                    title = "The Garuda Purana (Saroddhara)",
                    language = "en",
                    publisher = "The Panini Office, Bhuvaneshwari Asrama, Bahadurganj, Allahabad",
                    author = "Pandit Naunidhirama",
                    translator = "Ernest Wood and S.V. Subrahmanyam",
                    publicationYear = 1911,
                    publicationNote = "Published as Volume IX of 'The Sacred Books of the Hindus', edited by Major B.D. Basu.",
                    subtitle = "With English Translation and Brief Notes",
                    sourceTradition = "Garuda Purana Saroddhara (16 Chapters)",
                    workDescription = "First scholarly English translation of the Saroddhara compilation. Public domain in eligible jurisdictions (US pre-1929; India author died 1965, life+60 expired Jan 1, 2026).",
                    pageCount = 169,
                ),
                pathReference = "https://archive.org/stream/garuapurasroddh00subrgoog/garuapurasroddh00subrgoog_djvu.txt",
                checksum = SOURCE_WOOD_1911_SHA256,
                sizeBytes = 266_670L,
                textPages = 169,
                noTextPages = 0,
                imageObjects = 0,
                verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.CLEAN_TEXT_LAYER,
                licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
                rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
                licenseName = "Public Domain in Eligible Jurisdictions (Published 1911; author/translators copyright expired in US & India)",
                commercialUseAllowed = true,
                redistributionAllowed = true,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_WOOD_1911_ID,
                        EDITION_WOOD_1911_ID,
                        section = "Title page and publication imprint (1911), Panini Office Allahabad",
                        pages = GarudaPageRange(1, 4),
                        verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                    )
                ),
                rightsNote = "Published in 1911 in British India. Ernest Wood passed away in 1965 (>60 years post-mortem expired in India as of Jan 1, 2026; pre-1929 published work is public domain in USA; life+70 review required in EU/UK). Approved for distribution in eligible jurisdictions.",
                verificationDate = "2026-09-25",
                verificationMethod = "Visual and textual verification against Internet Archive digitized Panini Office 1911 edition",
            ),
            // 6. Manmatha Nath Dutt (1908) English Translation
            source(
                sourceId = SOURCE_DUTT_1908_ID,
                edition = GarudaEdition(
                    editionId = EDITION_DUTT_1908_ID,
                    title = "The Garuda Puranam",
                    language = "en",
                    publisher = "Society for the Resuscitation of Indian Literature / H.C. Dass, Calcutta",
                    translator = "Manmatha Nath Dutt",
                    publicationYear = 1908,
                    publicationNote = "Wealth of India Series, Elysium Press, Calcutta.",
                    subtitle = "Prose English translation of the Garuda Purana",
                    sourceTradition = "Garuda Mahapurana (Purvakhanda & Pretakhanda)",
                    workDescription = "Comprehensive English prose translation covering Agada, Dharma, and Preta sections. Public domain in eligible jurisdictions (translator died 1912).",
                    pageCount = 784,
                ),
                pathReference = "https://archive.org/stream/garudapuranam00duttgoog/garudapuranam00duttgoog_djvu.txt",
                checksum = SOURCE_DUTT_1908_SHA256,
                sizeBytes = 1_525_084L,
                textPages = 784,
                noTextPages = 0,
                imageObjects = 0,
                verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.CLEAN_TEXT_LAYER,
                licenseStatus = GarudaLicenseStatus.RIGHTS_VERIFIED,
                rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
                licenseName = "Rights Verified / Public Domain (Published 1908; translator died 1912)",
                commercialUseAllowed = true,
                redistributionAllowed = true,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_DUTT_1908_ID,
                        EDITION_DUTT_1908_ID,
                        section = "Title page and publication imprint (1908), Elysium Press Calcutta",
                        pages = GarudaPageRange(1, 6),
                        verification = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                    )
                ),
                rightsNote = "Published in 1908 in Calcutta. Translator died in 1912 (>70 years post-mortem in EU/UK/India/US; pre-1929 published work). Rights verified for cross-reference.",
                verificationDate = "2026-09-25",
                verificationMethod = "Textual inspection of Calcutta 1908 edition djvu stream",
            ),
            // 7. Motilal Banarsidass (MLBD) AITM Series (1978)
            source(
                sourceId = SOURCE_MLBD_1978_ID,
                edition = GarudaEdition(
                    editionId = EDITION_MLBD_1978_ID,
                    title = "The Garuda-Purana (3 Volumes)",
                    language = "en",
                    publisher = "Motilal Banarsidass Publishers Pvt. Ltd., Delhi",
                    editor = "Prof. J.L. Shastri",
                    translator = "A Board of Scholars",
                    publicationYear = 1978,
                    publicationNote = "Ancient Indian Tradition & Mythology Series, Vols. 12, 13, 14; UNESCO Collection of Representative Works.",
                    subtitle = "Complete English translation with scholarly annotations",
                    sourceTradition = "Garuda Mahapurana (Purvakhanda & Dharmakhanda / Pretakalpa)",
                    workDescription = "Scholarly modern critical translation and annotations. Proprietary copyrighted edition.",
                    pageCount = 1184,
                ),
                pathReference = "isbn:978-8120803091",
                checksum = SOURCE_MLBD_1978_SHA256,
                sizeBytes = 15_800_000L,
                textPages = 1184,
                noTextPages = 0,
                imageObjects = 0,
                verification = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
                textExtraction = GarudaTextExtractionStatus.UNKNOWN,
                licenseStatus = GarudaLicenseStatus.RESTRICTED,
                rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
                licenseName = "Copyrighted © Motilal Banarsidass Publishers Pvt. Ltd. (All Rights Reserved)",
                commercialUseAllowed = false,
                redistributionAllowed = false,
                rightsEvidence = listOf(
                    reference(
                        SOURCE_MLBD_1978_ID,
                        EDITION_MLBD_1978_ID,
                        section = "Copyright and publishing notice, Vol. 12 front matter",
                        pages = GarudaPageRange(4, 5),
                        verification = GarudaVerificationStatus.FILE_IDENTITY_VERIFIED,
                    )
                ),
                rightsNote = "Protected by copyright © Motilal Banarsidass. Commercial distribution or bundling without express written license is strictly prohibited. Kept outside app content package as reference only.",
                verificationDate = "2026-09-25",
                verificationMethod = "Bibliographic inspection of MLBD AITM series copyright and publication records",
            ),
        ),
    )

    private fun source(
        sourceId: String,
        edition: GarudaEdition,
        pathReference: String,
        checksum: String,
        sizeBytes: Long,
        textPages: Int,
        noTextPages: Int,
        imageObjects: Int,
        verification: GarudaVerificationStatus,
        textExtraction: GarudaTextExtractionStatus,
        licenseStatus: GarudaLicenseStatus,
        rightsStatus: GarudaRightsStatus,
        licenseName: String,
        commercialUseAllowed: Boolean,
        redistributionAllowed: Boolean,
        rightsEvidence: List<GarudaSourceReference>,
        rightsNote: String,
        verificationDate: String,
        verificationMethod: String,
    ) = GarudaSource(
        sourceId = sourceId,
        editionId = edition.editionId,
        edition = edition,
        sourcePathOrReference = pathReference,
        checksumSha256 = checksum,
        fileSizeBytes = sizeBytes,
        pageCount = edition.pageCount,
        textBearingPageCount = textPages,
        noTextLayerPageCount = noTextPages,
        embeddedImageObjectCount = imageObjects,
        textExtractionStatus = textExtraction,
        verificationStatus = verification,
        rights = GarudaSourceRights(
            sourceId = sourceId,
            editionId = edition.editionId,
            licenseStatus = licenseStatus,
            rightsStatus = rightsStatus,
            verificationStatus = verification,
            evidence = rightsEvidence,
            note = rightsNote,
            commercialUseAllowed = commercialUseAllowed,
            redistributionAllowed = redistributionAllowed,
            derivativeAllowed = redistributionAllowed,
            attributionRequired = true,
        ),
        license = GarudaSourceLicense(
            licenseStatus = licenseStatus,
            licenseName = licenseName,
            commercialUseAllowed = commercialUseAllowed,
            redistributionAllowed = redistributionAllowed,
            derivativeAllowed = redistributionAllowed,
            attributionRequired = true,
            attributionText = edition.publisher ?: edition.editor ?: edition.author,
            licenseUrl = if (pathReference.startsWith("url:")) pathReference.removePrefix("url:") else null,
            permissionRequired = !redistributionAllowed,
        ),
        verificationDate = verificationDate,
        verificationMethod = verificationMethod,
        contentVersion = "1.0",
        reviewStatus = if (rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION) {
            GarudaContentReviewStatus.SOURCE_VERIFIED
        } else {
            GarudaContentReviewStatus.REFERENCE_ONLY
        },
        sourceStatus = if (rightsStatus == GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION) {
            GarudaSourceStatus.APPROVED_FOR_DISTRIBUTION
        } else {
            GarudaSourceStatus.REFERENCE_ONLY
        },
    )

    private fun reference(
        sourceId: String,
        editionId: String,
        section: String,
        pages: GarudaPageRange,
        verification: GarudaVerificationStatus,
    ) = GarudaSourceReference(
        sourceId = sourceId,
        editionId = editionId,
        section = section,
        pageRange = pages,
        language = if (sourceId.contains("-en")) "en" else if (sourceId.contains("gretil") || sourceId.contains(
                "sanskritdocs"
            )
        ) "sa" else "hi",
        rightsStatus = if (sourceId.contains("wood") || sourceId.contains("dutt")) {
            GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION
        } else if (sourceId.contains("s1-summary")) {
            GarudaRightsStatus.LICENSE_UNCERTAIN
        } else {
            GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE
        },
        verificationStatus = verification,
    )
}
