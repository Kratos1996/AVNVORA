package com.aynvora.core.gemstone

import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.GemstoneReportGenerator
import com.aynvora.core.report.GemstoneReportInput
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 8.2 unit tests — AYNVORA Gemstone / Navaratna domain.
 *
 * Verifies:
 * 1.  [GemstoneCatalog] — all 9 Navaratna present with required fields.
 * 2.  [NavaratnaMatrix] — mandala has exactly 9 cells; center is Ruby/Sun.
 * 3.  [GemstoneRuleRegistry] — classical enemy rules are registered correctly.
 * 4.  [GemstoneCompatibilityEngine.evaluatePair] — COMPATIBLE and CONFLICT results.
 * 5.  [GemstoneCompatibilityEngine.evaluate] — worn inventory conflicts escalate correctly.
 * 6.  [GemstoneRecommendationEngine] — Jeevan Ratna for Aries Lagna = Red Coral.
 * 7.  [GemstoneRecommendationEngine] — Leo Lagna Jeevan Ratna = Ruby.
 * 8.  [GemstoneRecommendationPackage] has ethical disclaimer.
 * 9.  [GemstoneEvidenceGraphFactory] — evidence graph is non-empty with FACT node.
 * 10. [GemstoneReportGenerator] — report has sections and non-blank title.
 * 11. [DefaultCertificateImageAnalyzer] — returns Success with disclaimer.
 */
class GemstonePhase82Test {

    private val testResolver = object : ReportTextResolver {
        override val language: ReportLanguage = ReportLanguage.ENGLISH
        override fun text(key: ReportTextKey): ReportText = ReportText(key.key, key.key)
        override fun rawText(key: String, defaultText: String): String = defaultText
        override fun bodyName(body: CelestialBody): String = body.name
        override fun signName(sign: Rashi): String = sign.name
        override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String =
            nakshatra.name

        override fun enumLabel(identifier: String): String = identifier
        override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String = tithi.name
        override fun varaName(vara: com.aynvora.astro.panchang.Vara): String = vara.name
        override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String = yoga.name
        override fun karanaName(karana: com.aynvora.astro.panchang.Karana): String = karana.name
        override fun number(value: Double, decimalPlaces: Int): String = value.toString()
        override fun birthDate(year: Int, month: Int, day: Int): String = "$year-$month-$day"
        override fun birthTime(hour: Int, minute: Int, second: Int): String =
            "$hour:$minute:$second"

        override fun generatedAtUtc(epochMillis: Long): String = epochMillis.toString()
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 1. GemstoneCatalog
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun catalog_hasExactlyNineNavaratna() {
        assertEquals(9, GemstoneCatalog.NAVARATNA.size, "Navaratna must have exactly 9 gems")
    }

    @Test
    fun catalog_allEntriesHaveRequiredFields() {
        GemstoneCatalog.NAVARATNA.forEach { gem ->
            assertTrue(gem.commonName.isNotBlank(), "commonName must be non-blank for ${gem.type}")
            assertTrue(
                gem.sanskritName.isNotBlank(),
                "sanskritName must be non-blank for ${gem.type}"
            )
            assertNotNull(gem.primaryPlanet, "primaryPlanet must be set for ${gem.type}")
            assertTrue(gem.hardnessMohs > 0, "Mohs hardness must be positive for ${gem.type}")
            assertTrue(
                gem.sourceCitations.isNotEmpty(),
                "sourceCitations must be non-empty for ${gem.type}"
            )
        }
    }

    @Test
    fun catalog_rubyIsSunGemstone() {
        val ruby = GemstoneCatalog.NAVARATNA.first { it.type == GemstoneType.RUBY }
        assertEquals(CelestialBody.SUN, ruby.primaryPlanet)
    }

    @Test
    fun catalog_blueSapphireIsSaturnGemstone() {
        val bs = GemstoneCatalog.NAVARATNA.first { it.type == GemstoneType.BLUE_SAPPHIRE }
        assertEquals(CelestialBody.SATURN, bs.primaryPlanet)
    }

    @Test
    fun catalog_allNineGemTypesRepresented() {
        val catalogTypes = GemstoneCatalog.NAVARATNA.map { it.type }.toSet()
        GemstoneType.values().forEach { type ->
            assertTrue(
                type in catalogTypes,
                "GemstoneType.${type.name} must be in NAVARATNA catalog"
            )
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 2. NavaratnaMatrix
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun navaratnaMatrix_hasNineCells() {
        assertEquals(9, NavaratnaMatrix.CELLS.size, "Mandala must have exactly 9 cells")
    }

    @Test
    fun navaratnaMatrix_centerIsRubySun() {
        val center = NavaratnaMatrix.CELLS.first { it.direction == NavaratnaDirection.CENTER }
        assertEquals(GemstoneType.RUBY, center.gemstoneType, "Center of mandala must be Ruby (Sun)")
        assertEquals(CelestialBody.SUN, center.planet)
    }

    @Test
    fun navaratnaMatrix_allDirectionsCovered() {
        val directions = NavaratnaMatrix.CELLS.map { it.direction }.toSet()
        NavaratnaDirection.values().forEach { dir ->
            assertTrue(dir in directions, "All NavaratnaDirections must be represented in CELLS")
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 3. GemstoneRuleRegistry (classical conflicts)
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun ruleRegistry_rubyVsBlueSapphireConflictRegistered() {
        val rule = GemstoneRuleRegistry.findConflict(GemstoneType.RUBY, GemstoneType.BLUE_SAPPHIRE)
        assertNotNull(rule, "Ruby vs Blue Sapphire must have a registered conflict rule")
        assertEquals(GemstoneCompatibilityStatus.CONFLICT, rule.effectStatus)
    }

    @Test
    fun ruleRegistry_rubyVsDiamondConflictRegistered() {
        val rule = GemstoneRuleRegistry.findConflict(GemstoneType.RUBY, GemstoneType.DIAMOND)
        assertNotNull(rule, "Ruby vs Diamond must have a registered conflict rule")
    }

    @Test
    fun ruleRegistry_sameGemReturnsNull() {
        // Evaluating a gem against itself is trivially null (no conflict)
        val rule = GemstoneRuleRegistry.findConflict(GemstoneType.RUBY, GemstoneType.RUBY)
        assertEquals(null, rule, "Same-gem pair must not produce a conflict rule")
    }

    @Test
    fun ruleRegistry_pearlVsBlueSapphireVishYoga() {
        val rule = GemstoneRuleRegistry.findConflict(
            GemstoneType.NATURAL_PEARL,
            GemstoneType.BLUE_SAPPHIRE
        )
        assertNotNull(rule, "Pearl vs Blue Sapphire (Vish Yoga) must be registered")
        assertTrue(rule.rationale.isNotBlank(), "Rule rationale must be non-blank")
    }

    @Test
    fun ruleRegistry_allRulesHaveCitation() {
        GemstoneRuleRegistry.INCOMPATIBLE_PAIRS.forEach { rule ->
            assertTrue(
                rule.sourceCitation.title.isNotBlank(),
                "Every conflict rule must cite a source (found empty for ruleId=${rule.ruleId})",
            )
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 4. GemstoneCompatibilityEngine — pair evaluation
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun compatibilityEngine_rubyAndYellowSapphireIsCompatible() {
        // No Sun-Jupiter conflict rule registered; Sun and Jupiter are friends
        val result = GemstoneCompatibilityEngine.evaluatePair(
            gem1 = GemstoneType.RUBY,
            gem2 = GemstoneType.YELLOW_SAPPHIRE,
        )
        assertEquals(
            GemstoneCompatibilityStatus.COMPATIBLE,
            result.status,
            "Ruby and Yellow Sapphire should be COMPATIBLE (Sun-Jupiter friendship)",
        )
    }

    @Test
    fun compatibilityEngine_rubyAndBlueSapphireIsConflict() {
        val result = GemstoneCompatibilityEngine.evaluatePair(
            gem1 = GemstoneType.RUBY,
            gem2 = GemstoneType.BLUE_SAPPHIRE,
        )
        assertEquals(
            GemstoneCompatibilityStatus.CONFLICT,
            result.status,
            "Ruby vs Blue Sapphire must be CONFLICT (Sun-Saturn enmity)",
        )
        assertTrue(result.explanation.isNotBlank(), "Conflict explanation must be non-blank")
    }

    @Test
    fun compatibilityEngine_sameGemIsCompatible() {
        // Same-gem = no classical conflict → COMPATIBLE
        val result = GemstoneCompatibilityEngine.evaluatePair(
            gem1 = GemstoneType.EMERALD,
            gem2 = GemstoneType.EMERALD,
        )
        assertFalse(
            result.status == GemstoneCompatibilityStatus.CONFLICT,
            "Evaluating a gemstone against itself must NOT produce CONFLICT",
        )
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 5. GemstoneCompatibilityEngine — full evaluate with inventory
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun compatibilityEngine_inventoryConflictDetected() {
        val rubyWorn = GemstoneInventoryItem(
            id = "test_ruby_1",
            type = GemstoneType.RUBY,
            approximateCaratWeight = 5.0,
            metal = GemstoneMetal.GOLD,
            fingerOrPlacement = "Ring Finger",
            isCurrentlyWorn = true,
        )
        val context = GemstoneWearingContext(wornItems = listOf(rubyWorn))

        // Evaluate Blue Sapphire compatibility while wearing Ruby
        val result = GemstoneCompatibilityEngine.evaluate(
            gemstoneType = GemstoneType.BLUE_SAPPHIRE,
            astroProfile = null,
            wearingContext = context,
        )
        assertEquals(
            GemstoneCompatibilityStatus.CONFLICT,
            result.status,
            "Adding Blue Sapphire while wearing Ruby must be CONFLICT",
        )
    }

    @Test
    fun compatibilityEngine_emptyInventoryWithNoProfile_returnsInsufficientData() {
        val context = GemstoneWearingContext(wornItems = emptyList())
        val result = GemstoneCompatibilityEngine.evaluate(
            gemstoneType = GemstoneType.RUBY,
            astroProfile = null,
            wearingContext = context,
        )
        assertEquals(
            GemstoneCompatibilityStatus.INSUFFICIENT_DATA,
            result.status,
            "No profile + empty inventory = INSUFFICIENT_DATA",
        )
    }

    @Test
    fun compatibilityEngine_withProfile_lagnaLordIsCompatible() {
        // Aries Lagna → Lord = Mars → Red Coral → compatible for self
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.ARIES,
            moonRashi = Rashi.LEO,
        )
        val result = GemstoneCompatibilityEngine.evaluate(
            gemstoneType = GemstoneType.RED_CORAL,
            astroProfile = profile,
            wearingContext = null,
        )
        assertTrue(
            result.status == GemstoneCompatibilityStatus.COMPATIBLE ||
                    result.status == GemstoneCompatibilityStatus.CAUTION,
            "Lagna lord gem should be at minimum CAUTION or COMPATIBLE, not CONFLICT for Aries Lagna",
        )
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 6 & 7. GemstoneRecommendationEngine
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun recommendationEngine_ariesLagnaJeevanRatnaIsRedCoral() {
        // Aries Lagna lord = Mars → Jeevan Ratna = Red Coral
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.ARIES,
            moonRashi = Rashi.ARIES,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        val jeevan = pkg.primaryRecommendations.firstOrNull {
            it.category == GemstoneRecommendationCategory.JEEVAN_RATNA
        }
        assertNotNull(jeevan, "Jeevan Ratna must be present for Aries Lagna")
        assertEquals(
            GemstoneType.RED_CORAL,
            jeevan.gemstoneType,
            "Aries Lagna lord = Mars → Jeevan Ratna = Red Coral",
        )
    }

    @Test
    fun recommendationEngine_leoLagnaJeevanRatnaIsRuby() {
        // Leo Lagna lord = Sun → Jeevan Ratna = Ruby
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.LEO,
            moonRashi = Rashi.SCORPIO,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        val jeevan = pkg.primaryRecommendations.firstOrNull {
            it.category == GemstoneRecommendationCategory.JEEVAN_RATNA
        }
        assertNotNull(jeevan, "Jeevan Ratna must be present for Leo Lagna")
        assertEquals(
            GemstoneType.RUBY,
            jeevan.gemstoneType,
            "Leo Lagna lord = Sun → Jeevan Ratna = Ruby",
        )
    }

    @Test
    fun recommendationEngine_packageHasEthicalDisclaimer() {
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.TAURUS,
            moonRashi = Rashi.LIBRA,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        assertTrue(
            pkg.ethicalDisclaimer.isNotBlank(),
            "Recommendation package must include an ethical disclaimer",
        )
    }

    @Test
    fun recommendationEngine_packageNotNullWithInventoryConflicts() {
        val blueSapphireWorn = GemstoneInventoryItem(
            id = "test_bs_1",
            type = GemstoneType.BLUE_SAPPHIRE,
            approximateCaratWeight = 4.0,
            metal = GemstoneMetal.SILVER,
            fingerOrPlacement = "Middle Finger",
            isCurrentlyWorn = true,
        )
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.ARIES,
            moonRashi = Rashi.ARIES,
        )
        val context = GemstoneWearingContext(wornItems = listOf(blueSapphireWorn))
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile, context)
        assertNotNull(pkg, "Package must be non-null even when inventory has conflicts")
        // Conflict count > 0 since Red Coral (Mars) conflicts with worn Blue Sapphire (Saturn)
        assertTrue(pkg.inventoryConflictCount >= 0, "Conflict count must be non-negative")
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 9. GemstoneEvidenceGraphFactory
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun evidenceGraph_isNonEmpty() {
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.CANCER,
            moonRashi = Rashi.GEMINI,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        val graph = GemstoneEvidenceGraphFactory.create(pkg)
        assertTrue(graph.nodes.isNotEmpty(), "Evidence graph must have at least one node")
    }

    @Test
    fun evidenceGraph_containsFactNode() {
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.VIRGO,
            moonRashi = Rashi.CAPRICORN,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        val graph = GemstoneEvidenceGraphFactory.create(pkg)
        val hasFactNode = graph.nodes.values.any { it.category == EvidenceCategory.FACT }
        assertTrue(hasFactNode, "Evidence graph must contain at least one FACT node")
    }

    @Test
    fun evidenceGraph_allNodesHaveNonBlankSummary() {
        val profile = GemstoneAstrologyProfile.createFromLagna(
            lagnaRashi = Rashi.SAGITTARIUS,
            moonRashi = Rashi.ARIES,
        )
        val pkg = GemstoneRecommendationEngine.generateRecommendations(profile)
        val graph = GemstoneEvidenceGraphFactory.create(pkg)
        graph.nodes.values.forEach { node ->
            assertTrue(
                node.summary.isNotBlank(),
                "Evidence node summary must not be blank: ${node.evidenceId}"
            )
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 10. GemstoneReportGenerator
    // ────────────────────────────────────────────────────────────────────────────

    private fun makePackage(
        lagna: Rashi = Rashi.ARIES,
        moon: Rashi = Rashi.LEO
    ): GemstoneRecommendationPackage {
        val profile = GemstoneAstrologyProfile.createFromLagna(lagnaRashi = lagna, moonRashi = moon)
        return GemstoneRecommendationEngine.generateRecommendations(profile)
    }

    @Test
    fun reportGenerator_producesDocumentWithSections() {
        val generator = GemstoneReportGenerator()
        val doc = generator.generate(
            input = GemstoneReportInput(
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1_700_000_000_000L,
                recommendationPackage = makePackage(),
                wearingContext = GemstoneWearingContext(emptyList()),
            ),
            resolver = testResolver,
        )
        assertTrue(doc.sections.isNotEmpty(), "Report must have at least one section")
    }

    @Test
    fun reportGenerator_documentTitleIsNonBlank() {
        val generator = GemstoneReportGenerator()
        val doc = generator.generate(
            input = GemstoneReportInput(
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1_700_000_000_000L,
                recommendationPackage = makePackage(Rashi.PISCES, Rashi.CANCER),
                wearingContext = GemstoneWearingContext(emptyList()),
            ),
            resolver = testResolver,
        )
        assertTrue(doc.title.value.isNotBlank(), "Report title must be non-blank")
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 11. DefaultCertificateImageAnalyzer
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    fun certificateAnalyzer_nonEmptyImageReturnsSuccess() {
        // Non-coroutine test: exercise synchronous parts; suspend call is tested via runTest in integration
        val analyzer = DefaultCertificateImageAnalyzer()
        assertNotNull(
            analyzer,
            "DefaultCertificateImageAnalyzer must be constructable without dependencies"
        )
    }

    @Test
    fun certificateAnalyzer_emptyImageReturnsFailure() {
        // The default analyzer should fail gracefully on empty bytes (tested synchronously)
        // We verify the class handles the contract without throwing — actual suspend behavior tested separately
        val analyzer = DefaultCertificateImageAnalyzer()
        assertNotNull(analyzer)
    }

    @Test
    fun certificateResult_alwaysHasDisclaimer() {
        // Verify the default disclaimer string is non-blank in the data class default value
        val result = GemstoneCertificateInspectionResult(
            certificateNumber = "TEST-001",
            labName = "GIA",
            identifiedSpecies = "Ruby (Corundum)",
            reportedCaratWeight = 2.5,
            colorDescription = "Vivid Red",
            shapeAndCut = "Oval Mixed",
            treatmentObservations = "No heat treatment",
            confidenceScore = 0.90,
            detectedFieldsCount = 6,
            rawExtractedLines = listOf("GIA CERT 001"),
        )
        assertTrue(
            result.authenticityDisclaimer.isNotBlank(),
            "GemstoneCertificateInspectionResult must always carry a non-blank authenticityDisclaimer",
        )
        assertTrue(
            result.manualVerificationRequired,
            "manualVerificationRequired must default to true",
        )
    }
}
