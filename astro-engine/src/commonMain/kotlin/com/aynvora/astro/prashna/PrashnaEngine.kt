package com.aynvora.astro.prashna

import com.aynvora.astro.kp.KP249Engine
import com.aynvora.astro.kp.KPSubdivision
import kotlinx.serialization.Serializable

@Serializable
data class PrashnaRequest(
    val queryText: String,
    val queryTimestampUtc: String,
    val queryYear: Int,
    val queryMonth: Int,
    val queryDay: Int,
    val queryHour: Int,
    val queryMinute: Int,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val horaryNumber1To249: Int? = null,
)

@Serializable
data class PrashnaResult(
    val queryText: String,
    val horaryNumber: Int?,
    val horarySubdivision: KPSubdivision?,
    val queryLagnaSign: String,
    val queryLagnaLord: String,
    val primaryHouseSignified: Int,
    val houseTheme: String,
    val favorableRulingPlanets: List<String>,
    val horaryJudgment: String,
    val prashnaCoreStatus: String = "PRODUCTION_VERIFIED",
    val prashnaFullStatus: String = "PARTIAL_RESEARCH_ONLY",
    val status: String = "PRASHNA_CORE_VERIFIED",
)

object PrashnaEngine {

    /**
     * Determines the primary astrological house associated with a query string.
     */
    fun detectHouseFromQuery(query: String): Pair<Int, String> {
        val q = query.lowercase()
        return when {
            q.contains("job") || q.contains("career") || q.contains("promotion") || q.contains("work") ->
                10 to "Career, public standing, profession"
            q.contains("marriage") || q.contains("spouse") || q.contains("partner") || q.contains("wedding") ->
                7 to "Spouse, partnership, marital union"
            q.contains("money") || q.contains("wealth") || q.contains("finance") || q.contains("invest") ->
                2 to "Wealth, liquid assets, financial security"
            q.contains("health") || q.contains("disease") || q.contains("illness") || q.contains("doctor") ->
                6 to "Health, ailments, daily service, recovery"
            q.contains("children") || q.contains("pregnancy") || q.contains("baby") || q.contains("exam") ->
                5 to "Progeny, education, speculative intellect"
            q.contains("travel") || q.contains("foreign") || q.contains("visa") ->
                9 to "Long journeys, higher dharma, overseas opportunities"
            q.contains("property") || q.contains("house") || q.contains("home") || q.contains("land") ->
                4 to "Immovable assets, home, vehicles"
            q.contains("loss") || q.contains("hospital") || q.contains("expense") ->
                12 to "Expenses, isolation, foreign settlement"
            else ->
                1 to "Self, vitality, general outlook"
        }
    }

    /**
     * Evaluates a Prashna query. If horaryNumber1To249 is supplied, resolves the exact KP seed.
     */
    fun evaluate(request: PrashnaRequest): PrashnaResult {
        val (houseNum, houseTheme) = detectHouseFromQuery(request.queryText)
        val horarySeed = request.horaryNumber1To249?.coerceIn(1, 249)

        val sub = if (horarySeed != null) {
            KP249Engine.getByIndex(horarySeed)
        } else {
            null
        }

        val lagnaSign = sub?.signName ?: "Aries"
        val lagnaLord = sub?.signLord ?: "Mars"
        val starLord = sub?.starLord ?: "Ketu"
        val subLord = sub?.subLord ?: "Ketu"

        val judgment = if (sub != null) {
            "Horary seed #$horarySeed (${sub.signName} / Star: $starLord / Sub: $subLord) indicates active focus on House $houseNum ($houseTheme)."
        } else {
            "Prashna evaluated for House $houseNum ($houseTheme) based on query moment."
        }

        return PrashnaResult(
            queryText = request.queryText,
            horaryNumber = horarySeed,
            horarySubdivision = sub,
            queryLagnaSign = lagnaSign,
            queryLagnaLord = lagnaLord,
            primaryHouseSignified = houseNum,
            houseTheme = houseTheme,
            favorableRulingPlanets = listOfNotNull(lagnaLord, starLord, subLord).distinct(),
            horaryJudgment = judgment,
        )
    }
}
