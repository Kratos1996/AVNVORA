package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.astro.varshaphal.SolarReturnMoment
import com.aynvora.core.models.AstroChart
import kotlinx.serialization.Serializable

@Serializable
enum class VarshaphalComponentStatus { CALCULATED, UNSUPPORTED, NOT_VERIFIED }

/** A deliberately partial result: only the astronomical return and annual chart are calculated today. */
@Serializable
data class VarshaphalResult(
    val targetYear: Int,
    val solarReturn: SolarReturnMoment,
    val annualChart: AstroChart?,
    val calculationProfile: String,
    val muntha: MunthaCalculation? = null,
    val munthaStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.NOT_VERIFIED,
    val munthaLordStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.NOT_VERIFIED,
    val varsheshwaraStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val sahamsStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val tajikaAspectsStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val muddaDashaStatus: VarshaphalComponentStatus = VarshaphalComponentStatus.UNSUPPORTED,
    val diagnostics: List<String>,
) {
    val status: String get() = if (solarReturn.status.name == "CALCULATED" && annualChart != null) "PARTIAL" else "NOT_VERIFIED"
}
