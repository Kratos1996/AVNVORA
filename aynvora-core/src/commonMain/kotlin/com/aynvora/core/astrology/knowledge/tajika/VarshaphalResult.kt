package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.astro.varshaphal.SolarReturnMoment
import com.aynvora.core.models.AstroChart
import kotlinx.serialization.Serializable

@Serializable
enum class VarshaphalComponentStatus { CALCULATED, UNSUPPORTED, NOT_VERIFIED }

@Serializable
data class VarsheshwaraCandidateResult(val planet: String, val eligible: Boolean?, val strengthBreakdown: Map<String, String> = emptyMap(), val ruleRefs: List<String> = emptyList())

@Serializable
data class VarsheshwaraResult(
    val candidates: List<VarsheshwaraCandidateResult>,
    val selectedPlanet: String? = null,
    val tieBreak: String? = null,
    val ruleRefs: List<String> = emptyList(),
    val provenance: List<String> = emptyList(),
    val status: VarshaphalComponentStatus = VarshaphalComponentStatus.NOT_VERIFIED,
)

@Serializable
data class SahamResult(
    val id: String,
    val name: String,
    val longitude: Double? = null,
    val formula: String? = null,
    val pointA: String? = null,
    val pointB: String? = null,
    val pointC: String? = null,
    val dayFormula: String? = null,
    val nightFormula: String? = null,
    val specialConditions: String? = null,
    val source: String? = null,
    val chapter: String? = null,
    val page: String? = null,
    val sourceRef: String? = null,
    val validationStatus: String = "NOT_VERIFIED",
)

@Serializable
data class TajikaAspectResult(
    val planet1: String? = null,
    val planet2: String? = null,
    val aspectType: String? = null,
    val relationship: String? = null,
    val orbDegrees: Double? = null,
    val actualSeparation: Double? = null,
    val applying: Boolean?,
    val separating: Boolean?,
    val itthashala: Boolean?,
    val ishrafa: Boolean?,
    val sourceRef: String? = null,
    val status: String = "NOT_VERIFIED",
)

@Serializable
data class MuddaDashaPeriodResult(val planet: String, val start: String, val end: String, val sequenceIndex: Int, val durationDays: Double, val sourceRef: String?, val calculationProfile: String)

/** A deliberately partial result: only the astronomical return and annual chart are calculated today. */
@Serializable
data class VarshaphalResult(
    val targetYear: Int,
    val solarReturn: SolarReturnMoment,
    val annualChart: AstroChart?,
    val calculationProfile: String,
    val muntha: MunthaCalculation? = null,
    val munthaLord: MunthaLordResult? = null,
    val varsheshwara: VarsheshwaraResult? = null,
    val sahams: List<SahamResult> = emptyList(),
    val tajikaAspects: List<TajikaAspectResult> = emptyList(),
    val tajikaYogas: List<TajikaAspectResult> = emptyList(),
    val muddaDasha: List<MuddaDashaPeriodResult> = emptyList(),
    val ruleMatches: List<TajikaRuleMatch> = emptyList(),
    val evidence: List<com.aynvora.core.astrology.knowledge.AstroEvidenceItem> = emptyList(),
    val provenance: List<String> = emptyList(),
    val munthaStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.NOT_VERIFIED,
    val munthaLordStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.NOT_VERIFIED,
    val varsheshwaraStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val sahamsStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val tajikaAspectsStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val muddaDashaStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val diagnostics: List<String>,
) {
    val status: String get() = if (solarReturn.status.name == "CALCULATED" && annualChart != null) {
        if (munthaStatus == VarshaphalComponentStatus.CALCULATED &&
            varsheshwaraStatus == VarshaphalComponentStatus.CALCULATED &&
            sahamsStatus == VarshaphalComponentStatus.CALCULATED &&
            muddaDashaStatus == VarshaphalComponentStatus.CALCULATED) {
            "IMPLEMENTED"
        } else {
            "PARTIAL"
        }
    } else "NOT_VERIFIED"
}
