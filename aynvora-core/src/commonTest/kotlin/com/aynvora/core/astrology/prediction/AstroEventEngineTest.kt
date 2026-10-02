package com.aynvora.core.astrology.prediction

import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.context.AstroObservationContext
import com.aynvora.core.models.AstroFeatureStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AstroEventEngineTest {
    @Test
    fun matchingRequiresAllTypedEvidenceAndApprovedSourceAndIsDeterministic() {
        val definition = AstroEventDefinition(
            eventId = "fixture.event",
            nameKey = "event.fixture",
            tradition = "FIXTURE_ONLY",
            conditions = emptyList(),
            evidenceRequirements = listOf("transit-house"),
            sourceRefs = listOf("fixture:rule-1"),
            ruleVersion = "test-1",
            licenseStatus = "CLEARED",
            status = AstroEventDefinitionStatus.SUPPORTED,
            typedConditions = listOf(
                AstroEventCondition("transit.house", AstroEventComparison.EQUALS, "10", "transit-house"),
            ),
        )
        val period = AstroEventTimeRange("2027-01-01T00:00:00Z", "2027-01-31T23:59:59Z")
        val facts = listOf(AstroEventFact("evidence-1", "transit.house", "10", "transit-house", listOf("calculation:fixture")))
        val metadata = CalculationMetadata(calculationProfileId = "FIXTURE_ONLY", calculationModel = "TEST")

        val first = AstroEventEngine.evaluate(definition, period, facts, metadata)
        val second = AstroEventEngine.evaluate(definition, period, facts, metadata)

        assertEquals(AstroEventOccurrenceStatus.SUPPORTED, first.status)
        assertEquals(listOf("evidence-1"), first.matchedEvidenceIds)
        assertEquals(listOf("calculation:fixture", "fixture:rule-1"), first.sourceRefs)
        assertEquals(first, second)
        assertTrue(Json.encodeToString(first).contains("evidence-1"))
    }

    @Test
    fun missingEvidenceAndUnverifiedRulesNeverProduceSupportedEvents() {
        val definition = AstroEventDefinition(
            eventId = "fixture.event",
            nameKey = "event.fixture",
            tradition = "FIXTURE_ONLY",
            conditions = emptyList(),
            evidenceRequirements = listOf("transit-house"),
            sourceRefs = emptyList(),
            ruleVersion = "test-1",
            typedConditions = listOf(
                AstroEventCondition("transit.house", AstroEventComparison.GREATER_THAN, "5", "transit-house"),
            ),
        )
        val occurrence = AstroEventEngine.evaluate(
            definition,
            AstroEventTimeRange("2027-01-01", "2027-01-31"),
            emptyList(),
            CalculationMetadata(calculationProfileId = "FIXTURE_ONLY", calculationModel = "TEST"),
        )
        assertEquals(AstroEventOccurrenceStatus.NOT_VERIFIED, occurrence.status)
        assertTrue(occurrence.evidence.isEmpty())
        assertTrue(occurrence.sourceRefs.isEmpty())
    }

    @Test
    fun knowledgeRuleMatcherPreservesTraditionSourceAndMatchedEvidence() {
        val source = KnowledgeSource(
            sourceId = "fixture:source", title = "Test fixture", language = "en",
            licenseStatus = "CLEARED", version = "1", checksum = "fixture-checksum",
        )
        val rule = KnowledgeRule(
            ruleId = "fixture:rule", tradition = "FIXTURE_ONLY", topic = "test",
            conditions = listOf(AstroEventCondition("planet.house", AstroEventComparison.EQUALS, "5", "placement")),
            evidenceRequirements = listOf("placement"), interpretationKey = "fixture.effect",
            sourceId = source.sourceId, sourceVersion = "1", licenseStatus = "CLEARED",
            packVersion = "1", checksum = "fixture-rule-checksum",
        )
        val result = KnowledgeRuleMatcher.evaluate(
            rule, source, AstroEventTimeRange("2027-01-01", "2027-01-31"),
            listOf(AstroEventFact("fact-1", "planet.house", "5", "placement")),
            CalculationMetadata(calculationProfileId = "FIXTURE_ONLY", calculationModel = "TEST"),
        )

        assertEquals(AstroEventOccurrenceStatus.SUPPORTED, result?.status)
        assertEquals("knowledge.rule_match", result?.featureId)
        assertEquals("fixture:rule", result?.eventId)
        assertEquals(listOf("fact-1"), result?.matchedEvidenceIds)
        assertEquals(listOf(source.sourceId), result?.sourceRefs)
        assertEquals(null, KnowledgeRuleMatcher.evaluate(
            rule.copy(licenseStatus = "NOT_VERIFIED"), source,
            AstroEventTimeRange("2027-01-01", "2027-01-31"),
            listOf(AstroEventFact("fact-1", "planet.house", "5", "placement")),
            CalculationMetadata(calculationProfileId = "FIXTURE_ONLY", calculationModel = "TEST"),
        )?.takeIf { it.status == AstroEventOccurrenceStatus.SUPPORTED })
    }

    @Test
    fun advancedRegistryDoesNotAdvertiseUnverifiedEnginesAsSupported() {
        val capabilities = AdvancedAstrologyFeatureRegistry.capabilities.associateBy { it.featureId }
        assertEquals(AstroFeatureStatus.NOT_VERIFIED, capabilities.getValue("KP_ANALYSIS").status)
        assertEquals(AstroFeatureStatus.NOT_VERIFIED, capabilities.getValue("LAL_KITAB_ANALYSIS").status)
        assertEquals(AstroFeatureStatus.NOT_VERIFIED, capabilities.getValue("VARSHAPHAL").status)
        assertEquals(AstroFeatureStatus.UNSUPPORTED, capabilities.getValue("PHALADEESH").status)
        assertTrue(capabilities.getValue("KP_ANALYSIS").missingRequirements.isNotEmpty())
    }

    @Test
    fun eventRangesReuseOneBaseFactSetAcrossDayWeekMonthAndYearSlices() {
        val definition = AstroEventDefinition(
            eventId = "fixture.daily", nameKey = "event.daily", tradition = "FIXTURE_ONLY",
            conditions = emptyList(), evidenceRequirements = listOf("transit"),
            sourceRefs = listOf("fixture:source"), ruleVersion = "1", licenseStatus = "CLEARED",
            status = AstroEventDefinitionStatus.SUPPORTED,
            typedConditions = listOf(AstroEventCondition("transit.flag", AstroEventComparison.EQUALS, "yes", "transit")),
        )
        val catalog = AstroEventCatalog("fixture", "1", listOf(definition))
        val baseFacts = listOf(AstroEventFact("natal", "natal.marker", "kept", "natal", featureId = "natal"))
        val provenance = CalculationMetadata(calculationProfileId = "FIXTURE_ONLY", calculationModel = "TEST")
        listOf(1, 7, 30, 365).forEach { dayCount ->
            var dynamicCalls = 0
            val start = observation(2_460_000.0)
            val range = AstroEventObservationRange(start, observation(start.julianDay + dayCount), AstroObservationPurpose.EVENT_SCAN)
            val result = AstroEventGenerator.generateEvents(catalog, range, baseFacts, provenance) { _, slice ->
                dynamicCalls++
                listOf(AstroEventFact("transit-${slice.index}", "transit.flag", "yes", "transit", featureId = "transit"))
            }
            assertEquals(dayCount, dynamicCalls)
            assertEquals(dayCount, result.trace.sliceCount)
            assertEquals(dayCount, result.trace.dynamicFactCalculationCount)
            assertEquals(dayCount, result.trace.reusedBaseFactCount)
            assertEquals(1, result.occurrences.size)
            assertEquals(dayCount.toDouble(), result.occurrences.single().endJulianDay!! - result.occurrences.single().startJulianDay!!, 1e-7)
        }
    }

    private fun observation(julianDay: Double) = AstroObservationContext(
        localDate = "2027-01-01", localTime = "00:00:00", utcTimestamp = "2027-01-01T00:00:00Z",
        timeZoneId = "UTC", timezoneOffsetMinutes = 0, julianDay = julianDay,
        latitude = 0.0, longitude = 0.0, timezoneDataVersion = "test-tz-v1",
    )
}
