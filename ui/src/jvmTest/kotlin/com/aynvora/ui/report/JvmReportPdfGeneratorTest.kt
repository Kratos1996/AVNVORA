package com.aynvora.ui.report

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.report.ReportContentKind
import com.aynvora.core.report.ReportDisclaimer
import com.aynvora.core.report.ReportDocumentFactory
import com.aynvora.core.report.ReportFeatureStatus
import com.aynvora.core.report.ReportIdentity
import com.aynvora.core.report.ReportKeyValue
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportMetadata
import com.aynvora.core.report.ReportPdfResult
import com.aynvora.core.report.ReportSection
import com.aynvora.core.report.ReportTable
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportVersion
import com.aynvora.localization.report.AynvoraReportTextResolver
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JvmReportPdfGeneratorTest {
    @Test
    fun generatesPortablePdfFromLocalizedDocumentLines() {
        val resolver = AynvoraReportTextResolver(ReportLanguage.HINDI)
        val document = ReportDocumentFactory.create(
            metadata = ReportMetadata(
                "test-report",
                "kundali",
                1_700_000_000_000,
                ReportLanguage.HINDI,
                ReportVersion("1.0.0", "engine-1", "content-1"),
                ReportIdentity(),
                CoreFeatureId.ASTROLOGY,
                ReportFeatureStatus.IMPLEMENTED
            ),
            title = resolver.text(ReportTextKey.KUNDALI_TITLE),
            sections = listOf(
                ReportSection(
                    "birth_details", resolver.text(ReportTextKey.BIRTH_DETAILS), listOf(
                        ReportKeyValue(resolver.text(ReportTextKey.BIRTH_DATE), "15/05/1990"),
                        ReportTable(
                            listOf(
                                resolver.text(ReportTextKey.BODY),
                                resolver.text(ReportTextKey.SIGN)
                            ),
                            (1..120).map { listOf("सूर्य $it", "मेष") },
                            ReportContentKind.CALCULATION,
                        ),
                    )
                )
            ),
            availability = emptyList(),
            disclaimer = ReportDisclaimer(
                resolver.text(ReportTextKey.DISCLAIMER_TITLE),
                resolver.text(ReportTextKey.DISCLAIMER_TEXT)
            ),
        )

        val lines = document.toReportPdfLines(resolver)
        assertTrue(lines.any { it.text.contains("15/05/1990") })
        assertTrue(lines.any { it.text.contains("1.0.0") })
        assertTrue(lines.any { it.text.contains("सूर्य 120") })
        val generated =
            assertIs<ReportPdfResult.Generated>(JvmReportPdfGenerator(resolver).generate(document))
        val bytes = generated.artifact.bytes
        assertTrue(bytes.take(8).toByteArray().decodeToString().startsWith("%PDF-1.4"))
        val pdfText = bytes.decodeToString()
        assertTrue(pdfText.contains("/Subtype /Image"))
        assertTrue(Regex("/Count ([2-9]|[1-9][0-9]+)").containsMatchIn(pdfText))
        assertTrue(pdfText.contains("%%EOF"))
        assertTrue(generated.artifact.fileName.startsWith("kundali-"))
    }

    @Test
    fun testGeneratesPortablePdfAcrossMultipleSupportedLanguages() {
        val sampleLanguages = listOf(
            ReportLanguage.ENGLISH,
            ReportLanguage.HINDI,
            ReportLanguage.ARABIC,
            ReportLanguage.GUJARATI,
            ReportLanguage.BENGALI,
            ReportLanguage.TAMIL
        )

        for (language in sampleLanguages) {
            val resolver = AynvoraReportTextResolver(language)
            val document = ReportDocumentFactory.create(
                metadata = ReportMetadata(
                    reportId = "test-report-${language.code}",
                    reportTypeId = "kundali",
                    generatedAtEpochMs = 1_700_000_000_000L,
                    language = language,
                    version = ReportVersion("1.0.0", "engine-1", "content-1"),
                    identity = ReportIdentity(),
                    feature = CoreFeatureId.ASTROLOGY,
                    featureStatus = ReportFeatureStatus.IMPLEMENTED
                ),
                title = resolver.text(ReportTextKey.KUNDALI_TITLE),
                sections = listOf(
                    ReportSection(
                        id = "birth_details",
                        title = resolver.text(ReportTextKey.BIRTH_DETAILS),
                        blocks = listOf(
                            ReportKeyValue(resolver.text(ReportTextKey.BIRTH_DATE), "15/05/1990"),
                            ReportKeyValue(
                                resolver.text(ReportTextKey.ASCENDANT),
                                resolver.text(ReportTextKey.ASCENDANT).value
                            )
                        )
                    )
                ),
                availability = emptyList(),
                disclaimer = ReportDisclaimer(
                    title = resolver.text(ReportTextKey.DISCLAIMER_TITLE),
                    body = resolver.text(ReportTextKey.DISCLAIMER_TEXT)
                ),
            )

            val lines = document.toReportPdfLines(resolver)
            assertTrue(lines.isNotEmpty(), "PDF lines must not be empty for $language")
            // Ensure no raw keys appear in lines
            for (line in lines) {
                assertFalse(
                    line.text.startsWith("report.section.") || line.text.startsWith("report.kundali."),
                    "Line should be localized, not raw key: '${line.text}'"
                )
            }

            val generator = JvmReportPdfGenerator(resolver)
            val result = assertIs<ReportPdfResult.Generated>(generator.generate(document))
            val bytes = result.artifact.bytes
            assertTrue(bytes.take(8).toByteArray().decodeToString().startsWith("%PDF-1.4"))
            val pdfText = bytes.decodeToString()
            assertTrue(pdfText.contains("%%EOF"))
            assertTrue(result.artifact.fileName.endsWith(".pdf"))
        }
    }
}
