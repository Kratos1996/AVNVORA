package com.aynvora.report

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.report.engine.ReportFeatureEngine
import com.aynvora.report.engine.ReportGenerateRequest
import com.aynvora.report.engine.ReportResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportFeatureEngineTest {

    private val engine = ReportFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testReportGenerationWithFeatureResults() = runTest {
        val request = ReportGenerateRequest(
            reportType = "COMBINED",
            title = "Comprehensive Astrological & Palmistry Life Review",
            sections = listOf("Planetary Kundali", "Major Palm Creases", "Numerology Cycle"),
            featureResults = mapOf(
                "astrology" to """{"lagnaRashi":"ARIES","moonRashi":"LEO"}""",
                "palmistry" to """{"validation":"PASS","lines":["HEART","HEAD"]}""",
            ),
        )

        val event = AynvoraEvent(
            eventId = "evt_rep_1",
            featureId = AynvoraFeatureId.REPORT,
            eventType = "GENERATE_REPORT",
            timestampEpochMs = 1774000000000L,
            requestId = "req_rep_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<ReportResult>(response.resultJson)
        assertEquals("COMBINED", result.reportType)
        assertEquals(3, result.sectionCount)
        assertTrue(result.artifactPath?.endsWith(".pdf") == true)
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "GENERATE_REPORT",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
