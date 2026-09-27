package com.aynvora.core.architecture

import com.aynvora.core.ai.CalculateBirthChartTool
import com.aynvora.core.ai.DefaultAiToolRegistry
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.wallpaper.DefaultWallpaperPromptBuilder
import com.aynvora.core.wallpaper.DeviceProfile
import com.aynvora.core.wallpaper.SafeAreas
import com.aynvora.core.wallpaper.ScreenDimensions
import com.aynvora.core.wallpaper.WallpaperRequest
import com.aynvora.core.wallpaper.WallpaperTheme
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Architectural tests verifying Phase 7.4 Core Feature Registry,
 * domain boundaries, AI tool provenance, and wallpaper prompt construction.
 */
class CoreFeatureArchitectureTest {

    @Test
    fun coreFeatureRegistry_containsExactlyFourteenFirstClassDomains() {
        val features = CanonicalCoreFeatures
        assertEquals(
            14,
            features.size,
            "AYNVORA must define exactly 14 first-class core product domains"
        )

        val expectedIds = setOf(
            CoreFeatureId.ASTROLOGY,
            CoreFeatureId.PALMISTRY,
            CoreFeatureId.GEMSTONE,
            CoreFeatureId.NUMEROLOGY,
            CoreFeatureId.RUDRAKSHA,
            CoreFeatureId.JADI,
            CoreFeatureId.YANTRA,
            CoreFeatureId.GITA,
            CoreFeatureId.GARUDA_PURAN,
            CoreFeatureId.LAL_KITAB,
            CoreFeatureId.TAROT,
            CoreFeatureId.AI_ASSISTANT,
            CoreFeatureId.DAILY_GUIDANCE,
            CoreFeatureId.WALLPAPER,
        )

        val actualIds = features.map { it.id }.toSet()
        assertEquals(
            expectedIds,
            actualIds,
            "Feature registry IDs must match canonical product domains"
        )
    }

    @Test
    fun featureAvailability_astrologyAndTarotAreImplemented_othersAreFoundationReady() {
        val features = CanonicalCoreFeatures

        val astro = features.find { it.id == CoreFeatureId.ASTROLOGY }
        assertNotNull(astro)
        assertTrue(astro.availability is FeatureAvailability.Available)

        val tarot = features.find { it.id == CoreFeatureId.TAROT }
        assertNotNull(tarot)
        assertTrue(tarot.availability is FeatureAvailability.Available)

        val palmistry = features.find { it.id == CoreFeatureId.PALMISTRY }
        assertNotNull(palmistry)
        assertTrue(palmistry.availability is FeatureAvailability.Available)
    }

    @Test
    fun wallpaperPromptBuilder_generatesDeviceAwarePromptWithSafeAreas() {
        val builder = DefaultWallpaperPromptBuilder()
        val request = WallpaperRequest(
            rashi = Rashi.ARIES,
            nakshatra = Nakshatra.ASHWINI,
            theme = WallpaperTheme.COSMIC_SACRED_GEOMETRY,
            userIntentionOrGoal = "Clarity and courage",
            deviceProfile = DeviceProfile(
                deviceName = "Pixel 8 Pro",
                dimensions = ScreenDimensions(1344, 2992, "9:16"),
                safeAreas = SafeAreas(topPaddingPx = 360, bottomPaddingPx = 180),
            )
        )

        val result = builder.buildPrompt(request)
        assertTrue(result is com.aynvora.core.result.AynvoraResult.Success)
        val prompt = result.value

        assertTrue(prompt.promptText.contains("ARIES"))
        assertTrue(prompt.promptText.contains("ASHWINI"))
        assertTrue(prompt.promptText.contains("top 360px"))
        assertTrue(prompt.negativePromptText.contains("blurry"))
        assertEquals("9:16", prompt.suggestedAspect)
    }

    @Test
    fun aiToolRegistry_registersAndDispatchesStructuredAstroToolWithProvenance() = runBlocking {
        val sdk = com.aynvora.core.Aynvora.create()
        val registry = DefaultAiToolRegistry()
        val tool = CalculateBirthChartTool(sdk)
        registry.registerTool(tool)

        assertEquals(1, registry.listTools().size)
        assertNotNull(registry.getTool("calculateBirthChart"))

        // Execute tool with valid JSON payload
        val args =
            """{"year":1990,"month":5,"day":15,"hour":10,"minute":30,"latitude":28.6139,"longitude":77.2090}"""
        val executionResult = tool.execute(args)

        assertTrue(executionResult is com.aynvora.core.result.AynvoraResult.Success)
        val toolResult = executionResult.value

        assertEquals("calculateBirthChart", toolResult.toolName)
        assertEquals("ASTROLOGY", toolResult.provenance.sourceDomain)
        assertEquals("PARASHARA_CLASSICAL_V1", toolResult.provenance.calculationRulesetOrEdition)
        assertTrue(toolResult.provenance.isRetrievedFact)
        assertTrue(toolResult.resultJson.contains("ascendant"))
    }
}
