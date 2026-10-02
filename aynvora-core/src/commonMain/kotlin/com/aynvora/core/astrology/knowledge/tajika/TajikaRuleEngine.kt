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
    fun evaluateMuntha(
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

/** Registered local tool adapter for explicit, caller-calculated Muntha facts. */
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
            ?: error("natalAscendantLongitude number is required.")
        val elapsedCycles = arguments["elapsedSolarReturnCycles"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: error("elapsedSolarReturnCycles integer is required.")
        val annualAscendantIndex = arguments["annualAscendantSignIndex"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
        require(annualAscendantIndex == null || annualAscendantIndex in 0..11) { "annualAscendantSignIndex must be in 0..11." }
        val annualAscendant = annualAscendantIndex?.let { com.aynvora.core.models.Rashi.fromIndex(it) }
        val result = MunthaEngine.calculate(natalLongitude, elapsedCycles, annualAscendant)
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

/** Add these executable tools to AynvoraAiToolExecutor's `tools` map. */
object TajikaRegisteredTools {
    fun verifiedSubset(): Map<String, com.aynvora.core.astrology.knowledge.AstroRegisteredTool> = mapOf(
        "getMunthaLord" to TajikaMunthaLordRegisteredTool(),
        "getMuntha" to TajikaMunthaRegisteredTool(),
    )
}
