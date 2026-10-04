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

    @Test
    fun testGenerateSaveAndReopenDesktopPdfReport() {
        val resolver = AynvoraReportTextResolver(ReportLanguage.ENGLISH)
        val document = ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "desktop-e2e-report",
                reportTypeId = "full_life_report",
                generatedAtEpochMs = 1_700_000_000_000L,
                language = ReportLanguage.ENGLISH,
                version = ReportVersion("1.0.0", "engine-1", "content-1"),
                identity = ReportIdentity(),
                feature = CoreFeatureId.ASTROLOGY,
                featureStatus = ReportFeatureStatus.IMPLEMENTED
            ),
            title = resolver.text(ReportTextKey.KUNDALI_TITLE),
            sections = listOf(
                ReportSection(
                    id = "kp_astrology",
                    title = resolver.text(ReportTextKey.KP),
                    blocks = listOf(
                        ReportKeyValue(resolver.text(ReportTextKey.KP), "Sub-Lord Calculations Complete"),
                        ReportTable(
                            headers = listOf(resolver.text(ReportTextKey.BODY), resolver.text(ReportTextKey.SIGN)),
                            rows = listOf(listOf("Sun", "Aries"), listOf("Moon", "Taurus")),
                            kind = ReportContentKind.CALCULATION
                        )
                    )
                ),
                ReportSection(
                    id = "capture_evidence",
                    title = resolver.text(ReportTextKey.PALMISTRY_TITLE),
                    blocks = listOf(
                        ReportKeyValue(resolver.text(ReportTextKey.PALMISTRY_TITLE), "MediaPipe Hand Landmarker (Apache-2.0)"),
                        ReportTable(
                            headers = listOf(resolver.text(ReportTextKey.BODY), resolver.text(ReportTextKey.SIGN)),
                            rows = listOf(
                                listOf("Heart Line", "Confidence: 0.78, Continuity: 0.85"),
                                listOf("Head Line", "Confidence: 0.74, Continuity: 0.82"),
                                listOf("Life Line", "Confidence: 0.82, Continuity: 0.88"),
                                listOf("Fate Line", "Confidence: 0.68, Continuity: 0.75")
                            ),
                            kind = ReportContentKind.CALCULATION
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

        val generator = JvmReportPdfGenerator(resolver)
        val result = assertIs<ReportPdfResult.Generated>(generator.generate(document))
        val artifact = result.artifact
        val tempPdfFile = java.io.File.createTempFile("aynvora_report_test_", ".pdf")
        try {
            tempPdfFile.writeBytes(artifact.bytes)
            assertTrue(tempPdfFile.exists())
            assertTrue(tempPdfFile.length() > 500)

            // Re-read file from disk and inspect contents
            val readBytes = tempPdfFile.readBytes()
            kotlin.test.assertEquals(artifact.bytes.size, readBytes.size)
            assertTrue(readBytes.take(8).toByteArray().decodeToString().startsWith("%PDF-1.4"))
            val textContent = readBytes.decodeToString()
            assertTrue(textContent.contains("%%EOF"))
            assertTrue(textContent.contains("xref"))
            assertTrue(textContent.contains("/Type /Catalog"))
            assertTrue(textContent.contains("/Type /Pages"))
        } finally {
            tempPdfFile.delete()
        }
    }
}
