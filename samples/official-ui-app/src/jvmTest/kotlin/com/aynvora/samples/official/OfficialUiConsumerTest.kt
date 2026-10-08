package com.aynvora.samples.official

import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraResult
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OfficialUiConsumerTest {

    @Test
    fun testOfficialUiConsumerStack() = runTest {
        // App E: Official AYNVORA App consuming typed SDK with UI
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    PalmEngineProvider(),
                ),
            )
        )

        val astroResult = sdk.astrology.calculate(
            AstrologyRequest(
                name = "AynvoraUser",
                dateOfBirth = "1992-03-25",
                timeOfBirth = "17:45:00",
                location = AstrologyLocation(city = "Mumbai", latitude = 19.0760, longitude = 72.8777),
            )
        )
        assertTrue(astroResult is AynvoraResult.Success)

        val palmResult = sdk.palmistry.analyze(
            PalmistryRequest(
                selectedHand = HandType.LEFT,
                widthPx = 320,
                heightPx = 320,
            )
        )
        assertTrue(palmResult is AynvoraResult.Success)
    }
}
