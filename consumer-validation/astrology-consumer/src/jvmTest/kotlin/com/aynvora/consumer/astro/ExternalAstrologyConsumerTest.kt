package com.aynvora.consumer.astro

import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExternalAstrologyConsumerTest {

    @Test
    fun test1_CalculateAstrology_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        val request = AstrologyRequest(
            name = "External Consumer Native",
            dateOfBirth = "1995-10-24",
            timeOfBirth = "11:20:00",
            location = AstrologyLocation(latitude = 19.0760, longitude = 72.8777),
        )

        val result = sdk.astrology.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val data = result.value
        assertTrue(data.julianDay > 0.0)
        assertNotNull(data.lagnaSign)
        assertTrue(data.planets.isNotEmpty())
    }

    @Test
    fun test2_PalmistryEvent_ReturnsFeatureNotIncluded() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        // Feature is reported as unavailable
        assertEquals(false, sdk.isFeatureAvailable(AynvoraFeatureId.PALMISTRY))

        // Mode B Raw JSON dispatch returns FEATURE_NOT_INCLUDED
        val rawResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_HAND",
                payloadJson = """{"selectedHand":"RIGHT"}"""
            )
        )
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, rawResponse.status)
    }

    @Test
    fun testClasspath_PhysicalExclusion_ZeroPalmistryEngineClasses() {
        // Verifies Palmistry implementation class is physically absent from the consumer classpath
        assertFailsWith<ClassNotFoundException> {
            Class.forName("com.aynvora.palmistry.engine.PalmFeatureEngine")
        }
    }
}
