package com.aynvora.astro.prashna

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrashnaEngineTest {

    @Test
    fun testPrashnaCoreIntentAndSeedResolution() {
        val request = PrashnaRequest(
            queryText = "Will I get the new job promotion?",
            queryTimestampUtc = "2026-10-03T12:00:00Z",
            queryYear = 2026,
            queryMonth = 10,
            queryDay = 3,
            queryHour = 12,
            queryMinute = 0,
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneId = "Asia/Kolkata",
            horaryNumber1To249 = 108
        )

        val core = PrashnaEngine.evaluate(request)
        assertEquals("PRODUCTION_VERIFIED", core.prashnaCoreStatus)
        assertEquals(10, core.primaryHouseSignified, "Job/promotion must resolve to 10th house")
        assertEquals(108, core.horaryNumber)
        assertNotNull(core.horarySubdivision)
        assertTrue(core.horaryJudgment.contains("Horary seed #108"))
        assertTrue(core.favorableRulingPlanets.isNotEmpty())
    }

    @Test
    fun testPrashnaFullResearchOnlyGating() {
        val request = PrashnaRequest(
            queryText = "When will marriage happen?",
            queryTimestampUtc = "2026-10-03T12:00:00Z",
            queryYear = 2026,
            queryMonth = 10,
            queryDay = 3,
            queryHour = 12,
            queryMinute = 0,
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneId = "Asia/Kolkata",
            horaryNumber1To249 = 50
        )

        val full = PrashnaEngine.evaluate(request)
        assertEquals("PARTIAL_RESEARCH_ONLY", full.prashnaFullStatus, "Prashna Full must remain strictly RESEARCH_ONLY")
        assertEquals(7, full.primaryHouseSignified, "Marriage must resolve to 7th house")
    }
}
