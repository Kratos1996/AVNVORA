package com.aynvora.core.orchestration

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.EngineLifecycleState
import com.aynvora.contracts.FeatureAccessState
import com.aynvora.contracts.engineProvider
import com.aynvora.core.access.FeatureAccessController
import com.aynvora.core.registry.AynvoraEngineRegistry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AynvoraOrchestrationTest {

    private fun mockEngine(id: AynvoraFeatureId): AynvoraFeatureEngine = object : AynvoraFeatureEngine {
        override val featureId: AynvoraFeatureId = id
        override val version: String = "1.0.0"
        override val capabilities: Set<AynvoraFeatureCapability> = emptySet()
        override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse =
            AynvoraEventResponse.success(event.eventId, event.requestId, id, """{"status":"OK"}""")
    }

    @Test
    fun testAllEnginesRegisteredInRegistry() {
        val providers = listOf(
            engineProvider(AynvoraFeatureId.ASTROLOGY) { mockEngine(AynvoraFeatureId.ASTROLOGY) },
            engineProvider(AynvoraFeatureId.PALMISTRY) { mockEngine(AynvoraFeatureId.PALMISTRY) },
            engineProvider(AynvoraFeatureId.NUMEROLOGY) { mockEngine(AynvoraFeatureId.NUMEROLOGY) },
            engineProvider(AynvoraFeatureId.TAROT) { mockEngine(AynvoraFeatureId.TAROT) },
            engineProvider(AynvoraFeatureId.GEMSTONE) { mockEngine(AynvoraFeatureId.GEMSTONE) },
            engineProvider(AynvoraFeatureId.GITA) { mockEngine(AynvoraFeatureId.GITA) },
            engineProvider(AynvoraFeatureId.GARUDA_PURAN) { mockEngine(AynvoraFeatureId.GARUDA_PURAN) },
            engineProvider(AynvoraFeatureId.RUDRAKSHA) { mockEngine(AynvoraFeatureId.RUDRAKSHA) },
            engineProvider(AynvoraFeatureId.JADI) { mockEngine(AynvoraFeatureId.JADI) },
            engineProvider(AynvoraFeatureId.YANTRA) { mockEngine(AynvoraFeatureId.YANTRA) },
            engineProvider(AynvoraFeatureId.DAILY_GUIDANCE) { mockEngine(AynvoraFeatureId.DAILY_GUIDANCE) },
            engineProvider(AynvoraFeatureId.AI_ASSISTANT) { mockEngine(AynvoraFeatureId.AI_ASSISTANT) },
            engineProvider(AynvoraFeatureId.REPORT) { mockEngine(AynvoraFeatureId.REPORT) },
        )
        val registry = AynvoraEngineRegistry(providers = providers)
        val features = registry.getRegisteredFeatures()

        assertTrue(features.contains(AynvoraFeatureId.ASTROLOGY))
        assertTrue(features.contains(AynvoraFeatureId.PALMISTRY))
        assertTrue(features.contains(AynvoraFeatureId.NUMEROLOGY))
        assertTrue(features.contains(AynvoraFeatureId.TAROT))
        assertTrue(features.contains(AynvoraFeatureId.GEMSTONE))
        assertTrue(features.contains(AynvoraFeatureId.GITA))
        assertTrue(features.contains(AynvoraFeatureId.GARUDA_PURAN))
        assertTrue(features.contains(AynvoraFeatureId.RUDRAKSHA))
        assertTrue(features.contains(AynvoraFeatureId.JADI))
        assertTrue(features.contains(AynvoraFeatureId.YANTRA))
        assertTrue(features.contains(AynvoraFeatureId.DAILY_GUIDANCE))
        assertTrue(features.contains(AynvoraFeatureId.AI_ASSISTANT))
        assertTrue(features.contains(AynvoraFeatureId.REPORT))

        // Lazy verification: Not yet instantiated
        assertEquals(EngineLifecycleState.REGISTERED, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))
        assertNotNull(registry.getEngine(AynvoraFeatureId.ASTROLOGY))
        assertEquals(EngineLifecycleState.READY, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))
    }

    @Test
    fun testLifecycleTransitionsAndRelease() {
        var createCount = 0
        val provider = engineProvider(AynvoraFeatureId.ASTROLOGY) {
            createCount++
            mockEngine(AynvoraFeatureId.ASTROLOGY)
        }
        val registry = AynvoraEngineRegistry(providers = listOf(provider))

        assertEquals(EngineLifecycleState.REGISTERED, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))
        assertEquals(0, createCount)

        val engine = registry.getEngine(AynvoraFeatureId.ASTROLOGY)
        assertNotNull(engine)
        assertEquals(1, createCount)
        assertEquals(EngineLifecycleState.READY, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))

        // Releasing active engine to free resources
        registry.release(AynvoraFeatureId.ASTROLOGY)
        assertEquals(EngineLifecycleState.RELEASED, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))

        // Demand reloads
        val reloaded = registry.getEngine(AynvoraFeatureId.ASTROLOGY)
        assertNotNull(reloaded)
        assertEquals(2, createCount)
        assertEquals(EngineLifecycleState.READY, registry.getLifecycleState(AynvoraFeatureId.ASTROLOGY))
    }

    @Test
    fun testUnprovidedFeatureReturnsFeatureNotIncluded() = runTest {
        val registry = AynvoraEngineRegistry() // empty
        val router = AynvoraEventRouter(registry = registry)

        val event = AynvoraEvent(
            eventId = "evt_unprovided",
            featureId = AynvoraFeatureId.PALMISTRY,
            eventType = "ANALYZE_PALM",
            timestampEpochMs = 1774000000000L,
            requestId = "req_1",
            payloadJson = "{}",
        )

        val response = router.route(event)
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, response.status)
    }

    @Test
    fun testNotIncludedAccessStateReturnsFeatureNotIncluded() = runTest {
        val accessController = FeatureAccessController(
            settings = AynvoraFeatureSettings(
                featureStates = mapOf(
                    AynvoraFeatureId.PALMISTRY to FeatureAccessState.NOT_INCLUDED,
                ),
            )
        )
        val router = AynvoraEventRouter(accessController = accessController)

        val event = AynvoraEvent(
            eventId = "evt_not_included",
            featureId = AynvoraFeatureId.PALMISTRY,
            eventType = "ANALYZE_PALM",
            timestampEpochMs = 1774000000000L,
            requestId = "req_1",
            payloadJson = "{}",
        )

        val response = router.route(event)
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, response.status)
    }

    @Test
    fun testDisabledFeatureReturnsFeatureDisabled() = runTest {
        val accessController = FeatureAccessController(
            settings = AynvoraFeatureSettings(
                featureStates = mapOf(
                    AynvoraFeatureId.ASTROLOGY to FeatureAccessState.ENABLED,
                    AynvoraFeatureId.PALMISTRY to FeatureAccessState.DISABLED,
                ),
            )
        )
        val router = AynvoraEventRouter(accessController = accessController)

        val event = AynvoraEvent(
            eventId = "evt_palm_disabled",
            featureId = AynvoraFeatureId.PALMISTRY,
            eventType = "ANALYZE_PALM",
            timestampEpochMs = 1774000000000L,
            requestId = "req_1",
            payloadJson = "{}",
        )

        val response = router.route(event)
        assertEquals(AynvoraStatus.FEATURE_DISABLED, response.status)
    }

    @Test
    fun testLockedFeatureReturnsFeatureLocked() = runTest {
        val accessController = FeatureAccessController(
            settings = AynvoraFeatureSettings(
                featureStates = mapOf(
                    AynvoraFeatureId.ASTROLOGY to FeatureAccessState.ENABLED,
                    AynvoraFeatureId.AI_ASSISTANT to FeatureAccessState.LOCKED,
                ),
            )
        )
        val router = AynvoraEventRouter(accessController = accessController)

        val event = AynvoraEvent(
            eventId = "evt_ai_locked",
            featureId = AynvoraFeatureId.AI_ASSISTANT,
            eventType = "GROUNDING",
            timestampEpochMs = 1774000000000L,
            requestId = "req_2",
            payloadJson = "{}",
        )

        val response = router.route(event)
        assertEquals(AynvoraStatus.FEATURE_LOCKED, response.status)
    }
}
