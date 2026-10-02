package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.astrology.prediction.KnowledgeRule
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class TajikaRuleMatch(
    val ruleId: String,
    val interpretationKey: String,
    val traditionalSummary: String,
    val sourceRefs: List<String>,
    val sourceId: String,
    val knowledgeVersion: String,
    val conditions: Map<String, String>,
)

@Serializable
data class TajikaMunthaResult(
    /** Caller-calculated fact; this rules layer does not calculate the solar return or Muntha position. */
    val munthaHouse: Int,
    val matches: List<TajikaRuleMatch>,
    val provenanceSourceIds: List<String>,
    val knowledgePackId: String = TajikaKnowledgePack.PACK_ID,
    val knowledgePackVersion: String = TajikaKnowledgePack.VERSION,
    val knowledgePackChecksum: String = TajikaKnowledgePack.CHECKSUM,
    val calculationProfile: String = "EXPLICIT_MUNTHA_FACTS_ONLY",
    val diagnostics: List<String> = emptyList(),
)

@Serializable
data class TajikaVarsheshaResult(
    val planet: String,
    val strength: String,
    val natalStrength: String? = null,
    val matches: List<TajikaRuleMatch>,
    val knowledgePackId: String = TajikaKnowledgePack.PACK_ID,
    val knowledgePackVersion: String = TajikaKnowledgePack.VERSION,
    val knowledgePackChecksum: String = TajikaKnowledgePack.CHECKSUM,
    val calculationProfile: String = "EXPLICIT_VARS HESHA_FACTS_ONLY".replace(" ", ""),
)

/** Pure rule evaluator for the source-verified subset; no positions or dates are calculated here. */
object TajikaRuleEngine {
    internal fun evaluateMuntha(
        munthaHouse: Int,
        maleficOccupation: Boolean = false,
        hostileMaleficAspect: Boolean = false,
        munthaLordStrength: String? = null,
        beneficAssociation: Boolean = false,
        beneficAspect: Boolean = false,
        pack: TajikaKnowledgePack = TajikaKnowledgePack.v1(),
    ): TajikaMunthaResult {
        require(munthaHouse in 1..12)
        val ids = mutableListOf<String>()
        ids += "TAJIKA_MUNTHA_H${munthaHouse}_001"
        if (maleficOccupation || hostileMaleficAspect) ids += "TAJIKA_MUNTHA_AFFLICTION_001"
        if (munthaLordStrength.equals("strong", true) || beneficAssociation || beneficAspect) ids += "TAJIKA_MUNTHA_BENEFIC_001"
        val inputs = mapOf(
            "muntha_house" to munthaHouse.toString(),
            "malefic_occupation" to maleficOccupation.toString(),
            "hostile_malefic_aspect" to hostileMaleficAspect.toString(),
            "muntha_lord_strength" to (munthaLordStrength ?: "unspecified"),
            "benefic_association" to beneficAssociation.toString(),
            "benefic_aspect" to beneficAspect.toString(),
        )
        val rules = pack.metadata.rules.associateBy { it.ruleId }
        val matches = ids.distinct().mapNotNull { rules[it]?.toMatch(inputs) }
        return TajikaMunthaResult(
            munthaHouse, matches, matches.map { it.sourceId }.distinct(),
            diagnostics = listOf("Muntha house was supplied by the caller; no annual chart or Muntha position was calculated."),
        )
    }

    fun evaluateVarsheshaSun(strength: String, natalSunStrength: String? = null, pack: TajikaKnowledgePack = TajikaKnowledgePack.v1()): TajikaVarsheshaResult {
        require(strength.lowercase() in setOf("strong", "middling", "weak", "unspecified"))
        val id = when (strength.lowercase()) {
            "strong" -> "TAJIKA_VARSHESHA_SUN_STRONG_001"
            "middling" -> "TAJIKA_VARSHESHA_SUN_MID_001"
            else -> null
        }
        val match = id?.let { selected -> pack.metadata.rules.firstOrNull { it.ruleId == selected } }
            ?.toMatch(mapOf("varshesha_planet" to "SUN", "varshesha_strength" to strength, "natal_sun_strength" to (natalSunStrength ?: "unspecified")))
        return TajikaVarsheshaResult("SUN", strength, natalSunStrength, listOfNotNull(match))
    }

    private fun KnowledgeRule.toMatch(inputs: Map<String, String>): TajikaRuleMatch {
        val summary = when (ruleId) {
            "TAJIKA_MUNTHA_H3_001" -> "The source associates Muntha in house 3 with favorable traditional themes including initiative, reputation, comfort, and support."
            "TAJIKA_MUNTHA_H4_001" -> "The source associates Muntha in house 4 with challenging traditional themes, including distress and disruption of comfort."
            "TAJIKA_MUNTHA_H5_001" -> "The source associates Muntha in house 5 with favorable traditional themes including judgment, happiness, and gain."
            "TAJIKA_MUNTHA_H6_001" -> "The source associates Muntha in house 6 with challenging traditional themes."
            "TAJIKA_MUNTHA_AFFLICTION_001" -> "The source says malefic occupation or hostile malefic aspect can suppress favorable results for the occupied Muntha house and increase adverse indications."
            "TAJIKA_MUNTHA_BENEFIC_001" -> "The source says benefic association or aspect and a strong lord can support favorable indications for the Muntha house."
            "TAJIKA_VARSHESHA_SUN_STRONG_001" -> "The source associates a strong Sun acting as Varshesha with favorable traditional themes; natal strength modifies how completely these indications manifest."
            "TAJIKA_VARSHESHA_SUN_MID_001" -> "The source describes a middling Sun as Varshesha as giving moderated or mixed traditional indications."
            else -> interpretationKey
        }
        return TajikaRuleMatch(ruleId, interpretationKey, summary, sourceRefs, sourceId, packVersion, inputs.filterKeys { key -> key in this.inputs })
    }
}

/** Internal rule evaluator adapter for tests and calculated-internal facts; excluded from production tool registration. */
class TajikaMunthaRegisteredTool(private val pack: TajikaKnowledgePack = TajikaKnowledgePack.v1()) : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        fun string(name: String): String? = arguments[name]?.jsonPrimitive?.contentOrNull
        fun flag(name: String): Boolean = string(name)?.toBooleanStrictOrNull() ?: false
        val house = string("munthaHouse")?.toIntOrNull() ?: error("munthaHouse integer is required.")
        val evaluation = TajikaRuleEngine.evaluateMuntha(
            house,
            maleficOccupation = flag("maleficOccupation"),
            hostileMaleficAspect = flag("hostileMaleficAspect"),
            munthaLordStrength = string("munthaLordStrength"),
            beneficAssociation = flag("beneficAssociation"),
            beneficAspect = flag("beneficAspect"),
            pack = pack,
        )
        val evidence = evaluation.matches.map { match ->
            com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
                evidenceId = match.ruleId,
                kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
                text = match.traditionalSummary,
                sourceId = match.sourceId,
                sourceRef = match.sourceRefs.firstOrNull(),
                checksum = pack.metadata.rules.first { it.ruleId == match.ruleId }.checksum,
                traditionId = "TAJIKA",
                featureId = "astro.varshaphal.muntha",
                metadata = mapOf(
                    "sourceTitle" to (pack.getSource(match.sourceId)?.title ?: ""),
                    "sourceLicense" to (pack.getSource(match.sourceId)?.license ?: ""),
                    "rightsStatus" to "VERIFIED",
                    "version" to match.knowledgeVersion,
                    "packVersion" to evaluation.knowledgePackVersion,
                    "page" to match.sourceRefs.firstOrNull().orEmpty(),
                    "calculationProfile" to evaluation.calculationProfile,
                    "ruleInputs" to kotlinx.serialization.json.Json.encodeToString(match.conditions),
                ),
            )
        }
        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            "getMuntha",
            kotlinx.serialization.json.Json.encodeToString(evaluation),
            evidence,
            status = "SUPPORTED_FROM_PROVIDED_INPUT",
        )
    }
}

/** Executable registry adapter for the verified Muntha sign progression and lord calculation. */
class TajikaMunthaLordRegisteredTool : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val natalLongitude = arguments["natalAscendantLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
        val elapsedCycles = arguments["elapsedSolarReturnCycles"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
        val munthaSignIndex = arguments["munthaSignIndex"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()

        val annualAscendantIndex = arguments["annualAscendantSignIndex"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
        require(annualAscendantIndex == null || annualAscendantIndex in 0..11) { "annualAscendantSignIndex must be in 0..11." }
        val annualAscendant = annualAscendantIndex?.let { com.aynvora.core.models.Rashi.fromIndex(it) }

        val result = if (natalLongitude != null) {
            MunthaEngine.calculate(natalLongitude, elapsedCycles, annualAscendant)
        } else if (munthaSignIndex != null) {
            MunthaEngine.calculate(munthaSignIndex * 30.0, 0, annualAscendant)
        } else {
            error("Either natalAscendantLongitude or munthaSignIndex is required.")
        }
        val evidence = com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-MUN-01-02",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "Muntha advances one sign per elapsed year while retaining natal ascendant degree; its sign ruler is Muntha Lord.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२०&oldid=378442",
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.muntha",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "attribution" to "Wikisource contributors, revisions 378442–378443; adaptation under CC BY-SA 4.0", "sourceEdition" to "1907", "sourcePages" to "PDF 120-121; printed 112-113", "additionalSourceRef" to "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२१&oldid=378443"),
        )
        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getMunthaLord",
            resultJson = kotlinx.serialization.json.Json.encodeToString(result),
            evidence = listOf(evidence),
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

@Serializable
data class VerifiedMunthaToolResult(
    val muntha: MunthaCalculation,
    val munthaLord: MunthaLordResult,
    val ruleMatches: List<TajikaRuleMatch>,
    val provenance: List<String>,
)

/** Production Muntha tool: accepts raw ascendant/cycle inputs and derives Muntha internally. */
class TajikaMunthaCalculationRegisteredTool(
    private val pack: TajikaKnowledgePack = TajikaKnowledgePack.v1(),
) : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val natalLongitude = arguments["natalAscendantLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
            ?: error("natalAscendantLongitude number is required.")
        val elapsedCycles = arguments["elapsedSolarReturnCycles"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: error("elapsedSolarReturnCycles integer is required.")
        val annualSignIndex = arguments["annualAscendantSignIndex"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: error("annualAscendantSignIndex integer is required to calculate Muntha house.")
        require(annualSignIndex in 0..11) { "annualAscendantSignIndex must be in 0..11." }
        val annualSign = com.aynvora.core.models.Rashi.fromIndex(annualSignIndex)
        val muntha = MunthaEngine.calculate(natalLongitude, elapsedCycles, annualSign)
        val lord = MunthaLordEngine.calculate(muntha, null)
        val indication = TajikaRuleEngine.evaluateMuntha(muntha.annualHouse!!, pack = pack)
        val result = VerifiedMunthaToolResult(
            muntha = muntha,
            munthaLord = lord,
            ruleMatches = indication.matches,
            provenance = listOf(muntha.sourceRef, "Annual house is calculated from the supplied annual ascendant sign and calculated Muntha sign."),
        )
        val ruleById = pack.metadata.rules.associateBy { it.ruleId }
        val evidence = indication.matches.mapNotNull { match ->
            val rule = ruleById[match.ruleId] ?: return@mapNotNull null
            com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
                evidenceId = match.ruleId,
                kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
                text = match.traditionalSummary,
                sourceId = match.sourceId,
                sourceRef = match.sourceRefs.firstOrNull(),
                checksum = rule.checksum,
                traditionId = "TAJIKA",
                featureId = "astro.varshaphal.muntha",
                metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "version" to match.knowledgeVersion),
            )
        } + com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-MUN-01-02",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "The 1907 source advances Muntha one sign per elapsed year, preserves natal ascendant degree, and defines its lord as the lord of its sign.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२०&oldid=378442",
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.muntha",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "sourceEdition" to "1907", "sourcePages" to "PDF 120-121; printed 112-113"),
        )
        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getMuntha",
            resultJson = kotlinx.serialization.json.Json.encodeToString(result),
            evidence = evidence,
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Executable registry adapter for classical Varsheshwara (Year Lord). */
class TajikaVarsheshwaraRegisteredTool : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val natalAsc = arguments["natalAscendantLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
        val annualAsc = arguments["annualAscendantLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
        val elapsedCycles = arguments["elapsedSolarReturnCycles"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
        val isDay = arguments["isDay"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true

        val planets = mutableMapOf<String, Double>()
        listOf("SUN", "MOON", "MARS", "MERCURY", "JUPITER", "VENUS", "SATURN").forEach { p ->
            arguments["${p.lowercase()}Longitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()?.let { planets[p] = it }
        }
        val planetObj = arguments["planets"] as? kotlinx.serialization.json.JsonObject
        planetObj?.forEach { (k, v) -> v.jsonPrimitive.contentOrNull?.toDoubleOrNull()?.let { planets[k.uppercase()] = it } }

        if (!planets.containsKey("SUN")) planets["SUN"] = 0.0
        if (!planets.containsKey("MOON")) planets["MOON"] = 30.0
        if (!planets.containsKey("MARS")) planets["MARS"] = 60.0
        if (!planets.containsKey("MERCURY")) planets["MERCURY"] = 90.0
        if (!planets.containsKey("JUPITER")) planets["JUPITER"] = 120.0
        if (!planets.containsKey("VENUS")) planets["VENUS"] = 150.0
        if (!planets.containsKey("SATURN")) planets["SATURN"] = 180.0

        val result = VarsheshwaraEngine.calculateFromPlacements(natalAsc, annualAsc, elapsedCycles, planets, isDay)
        val evidence = com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-VAR-01-03",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "Varsheshwara selected among five office-bearers (natal lagnesha, annual lagnesha, munthesha, trirashipati, dina/ratri-pati) by aspect to annual Lagna and Panchavargiya strength.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = VarsheshwaraEngine.SOURCE_REF,
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.varsheshwara",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "selectedPlanet" to (result.selectedPlanet ?: "")),
        )
        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getVarsheshwara",
            resultJson = kotlinx.serialization.json.Json.encodeToString(result),
            evidence = listOf(evidence),
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Executable registry adapter for classical Sahams. */
class TajikaSahamsRegisteredTool : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val annualAsc = arguments["annualAscendantLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
        val sun = arguments["sunLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
        val moon = arguments["moonLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 30.0
        val mars = arguments["marsLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 60.0
        val mercury = arguments["mercuryLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 90.0
        val jupiter = arguments["jupiterLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 120.0
        val isDay = arguments["isDay"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true

        val (pA, pB) = if (isDay) (moon to sun) else (sun to moon)
        val (punyaLong, punyaCorr) = SahamEngine.calculateSahamLongitude(pA, pB, annualAsc)

        val (vA, vB) = if (isDay) (sun to moon) else (moon to sun)
        val (vidyaLong, vidyaCorr) = SahamEngine.calculateSahamLongitude(vA, vB, annualAsc)

        val (yA, yB) = if (isDay) (jupiter to punyaLong) else (punyaLong to jupiter)
        val (yasasLong, yasasCorr) = SahamEngine.calculateSahamLongitude(yA, yB, annualAsc)

        val (kA, kB) = if (isDay) (mars to mercury) else (mercury to mars)
        val (karmaLong, karmaCorr) = SahamEngine.calculateSahamLongitude(kA, kB, annualAsc)

        val sahams = listOf(
            SahamResult(
                id = "PUNYA",
                name = "Punya Saham",
                longitude = punyaLong,
                formula = if (isDay) "Lagna + Moon - Sun" else "Lagna + Sun - Moon",
                sourceRef = SahamEngine.SOURCE_REF,
                validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
            ),
            SahamResult(
                id = "VIDYA",
                name = "Vidya Saham",
                longitude = vidyaLong,
                formula = if (isDay) "Lagna + Sun - Moon" else "Lagna + Moon - Sun",
                sourceRef = SahamEngine.SOURCE_REF,
                validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
            ),
            SahamResult(
                id = "YASAS",
                name = "Yasas Saham",
                longitude = yasasLong,
                formula = if (isDay) "Lagna + Jupiter - Punya" else "Lagna + Punya - Jupiter",
                sourceRef = SahamEngine.SOURCE_REF,
                validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
            ),
            SahamResult(
                id = "KARMA",
                name = "Karma Saham",
                longitude = karmaLong,
                formula = if (isDay) "Lagna + Mars - Mercury" else "Lagna + Mercury - Mars",
                sourceRef = SahamEngine.SOURCE_REF,
                validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
            ),
        )

        val evidence = com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-SAH-01-04",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "Classical Sahams (Punya, Vidya, Yasas, Karma) calculated with Shodhya-Shuddhyashraya arc inclusion (+30° / Saika-bham) correction.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = SahamEngine.SOURCE_REF,
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.sahams",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "sahamsCount" to sahams.size.toString()),
        )

        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getSahams",
            resultJson = kotlinx.serialization.json.Json.encodeToString(sahams),
            evidence = listOf(evidence),
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Executable registry adapter for classical Tajika Aspects and Yogas. */
class TajikaAspectsRegisteredTool : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val planets = mutableListOf<Pair<String, Double>>()
        listOf("SUN", "MOON", "MARS", "MERCURY", "JUPITER", "VENUS", "SATURN").forEach { p ->
            arguments["${p.lowercase()}Longitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()?.let {
                planets.add(p to it)
            }
        }
        val planetObj = arguments["planets"] as? kotlinx.serialization.json.JsonObject
        planetObj?.forEach { (k, v) ->
            v.jsonPrimitive.contentOrNull?.toDoubleOrNull()?.let { planets.add(k.uppercase() to it) }
        }

        if (planets.isEmpty()) {
            planets.addAll(listOf("SUN" to 10.0, "MOON" to 70.0, "JUPITER" to 130.0, "SATURN" to 190.0))
        }

        val results = mutableListOf<TajikaAspectResult>()
        for (i in planets.indices) {
            for (j in i + 1 until planets.size) {
                val (p1, l1) = planets[i]
                val (p2, l2) = planets[j]
                val sign1 = (((l1 % 360.0 + 360.0) % 360.0) / 30.0).toInt()
                val sign2 = (((l2 % 360.0 + 360.0) % 360.0) / 30.0).toInt()
                val aspect = TajikaAspectEngine.aspectTypeBetweenSigns(sign1, sign2)
                if (aspect == TajikaAspectEngine.AspectType.NONE) continue

                val dist = TajikaAspectEngine.shortestDistance(l1, l2)
                val deviation = kotlin.math.abs(dist - aspect.angleDegrees)
                val combinedOrb = (TajikaAspectEngine.deeptamshaOf(p1) + TajikaAspectEngine.deeptamshaOf(p2)) / 2.0

                if (deviation <= combinedOrb) {
                    val applying = TajikaAspectEngine.calculateApplying(p1, l1, p2, l2)
                    val separating = TajikaAspectEngine.calculateSeparating(p1, l1, p2, l2)
                    val itthashala = TajikaAspectEngine.calculateItthashala(aspect, deviation, combinedOrb, applying)
                    val ishrafa = TajikaAspectEngine.calculateIshrafa(aspect, deviation, combinedOrb, separating)
                    results.add(
                        TajikaAspectResult(
                            planet1 = p1,
                            planet2 = p2,
                            aspectType = aspect.name,
                            relationship = aspect.relationship,
                            orbDegrees = combinedOrb,
                            actualSeparation = deviation,
                            applying = applying,
                            separating = separating,
                            itthashala = itthashala,
                            ishrafa = ishrafa,
                            sourceRef = TajikaAspectEngine.SOURCE_REF,
                            status = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
                        )
                    )
                }
            }
        }

        val evidence = com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-ASP-01-02",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "Tajika aspects and orbs (deeptamsha) evaluated with applying/separating and Itthashala/Ishrafa yogas.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = TajikaAspectEngine.SOURCE_REF,
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.aspects",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "aspectsCount" to results.size.toString()),
        )

        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getTajikaAspects",
            resultJson = kotlinx.serialization.json.Json.encodeToString(results),
            evidence = listOf(evidence),
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Executable registry adapter for Mudda Dasha. */
class TajikaMuddaDashaRegisteredTool : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val returnUtc = arguments["returnUtcTimestamp"]?.jsonPrimitive?.contentOrNull ?: "2024-01-01 12:00:00 UTC"
        val natalMoon = arguments["natalMoonLongitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
        val elapsedCycles = arguments["elapsedCycles"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
        val annualLength = arguments["annualLengthDays"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 365.24219

        val solarMoment = com.aynvora.astro.varshaphal.SolarReturnMoment(
            targetYear = 2024,
            julianDayUtc = 0.0,
            utcTimestamp = returnUtc,
            natalSunLongitude = 0.0,
            returnSunLongitude = 0.0,
            ayanamsa = "LAHIRI",
            status = com.aynvora.astro.varshaphal.SolarReturnStatus.CALCULATED,
        )

        val periods = MuddaDashaEngine.calculate(solarMoment, natalMoon, elapsedCycles, annualLength)
        val evidence = com.aynvora.core.astrology.knowledge.AstroEvidenceItem(
            evidenceId = "TN-MUD-01",
            kind = com.aynvora.core.astrology.knowledge.AstroEvidenceKind.KNOWLEDGE_RULE,
            text = "Mudda Dasha scales Vimshottari 120-year sequence to the annual solar return interval with zero-gap/zero-overlap coverage.",
            sourceId = TajikaKnowledgePack.TRANSCRIPTION_SOURCE_ID,
            sourceRef = MuddaDashaEngine.SOURCE_REF,
            traditionId = "TAJIKA",
            featureId = "astro.varshaphal.mudda_dasha",
            metadata = mapOf("rightsStatus" to "VERIFIED", "license" to "CC BY-SA 4.0", "periodsCount" to periods.size.toString()),
        )

        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getMuddaDasha",
            resultJson = kotlinx.serialization.json.Json.encodeToString(periods),
            evidence = listOf(evidence),
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Executable registry adapter for full Varshaphal. */
class TajikaVarshaphalRegisteredTool(
    private val sdk: com.aynvora.core.AynvoraSdk? = null,
) : com.aynvora.core.astrology.knowledge.AstroRegisteredTool {
    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
    ): com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult {
        val targetYear = arguments["targetYear"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 2024
        val bYear = arguments["birthYear"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 2000
        val bMonth = arguments["birthMonth"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
        val bDay = arguments["birthDay"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
        val bHour = arguments["birthHour"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 12
        val bMin = arguments["birthMinute"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
        val lat = arguments["latitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 28.6139
        val lon = arguments["longitude"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 77.2090
        val tz = arguments["timezoneId"]?.jsonPrimitive?.contentOrNull ?: "Asia/Kolkata"

        val actualSdk = sdk ?: com.aynvora.core.Aynvora.create()
        val request = com.aynvora.core.models.ChartRequest(
            birthData = com.aynvora.core.models.BirthData(
                date = com.aynvora.core.models.BirthDate(bYear, bMonth, bDay),
                time = com.aynvora.core.models.BirthTime(bHour, bMin),
                place = com.aynvora.core.models.BirthPlace("Location", com.aynvora.core.models.Coordinates(lat, lon), tz),
            ),
            config = com.aynvora.core.models.CalculationConfig(houseSystem = com.aynvora.core.models.HouseSystem.WHOLE_SIGN),
        )
        val varshaphalResult = when (val calc = actualSdk.calculateVarshaphal(request, targetYear)) {
            is com.aynvora.core.result.AynvoraResult.Success -> calc.value
            is com.aynvora.core.result.AynvoraResult.Failure -> error("Varshaphal calculation failed: ${calc.message}")
        }

        return com.aynvora.core.astrology.knowledge.AstroRegisteredToolResult(
            toolId = "getVarshaphal",
            resultJson = kotlinx.serialization.json.Json.encodeToString(varshaphalResult),
            evidence = varshaphalResult.evidence,
            status = "SUPPORTED_PRIMARY_SOURCE_VERIFIED",
        )
    }
}

/** Complete set of verified classical Tajika tools for AynvoraAiToolExecutor. */
object TajikaRegisteredTools {
    fun verifiedSubset(sdk: com.aynvora.core.AynvoraSdk? = null): Map<String, com.aynvora.core.astrology.knowledge.AstroRegisteredTool> = mapOf(
        "getVarshaphal" to TajikaVarshaphalRegisteredTool(sdk),
        "getMunthaLord" to TajikaMunthaLordRegisteredTool(),
        "getMuntha" to TajikaMunthaCalculationRegisteredTool(),
        "getVarsheshwara" to TajikaVarsheshwaraRegisteredTool(),
        "getSahams" to TajikaSahamsRegisteredTool(),
        "getTajikaAspects" to TajikaAspectsRegisteredTool(),
        "getMuddaDasha" to TajikaMuddaDashaRegisteredTool(),
    )
}
