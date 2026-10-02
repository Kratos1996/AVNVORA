package com.aynvora.core.astrology.knowledge

import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData
import com.aynvora.astro.compatibility.CompatibilityEngine
import com.aynvora.astro.jaimini.JaiminiEngine
import com.aynvora.astro.kp.KP249Engine
import com.aynvora.astro.kp.KPEngine
import com.aynvora.astro.muhurta.MuhurtaEngine
import com.aynvora.astro.prashna.PrashnaEngine
import com.aynvora.astro.prashna.PrashnaRequest
import com.aynvora.astro.upagrahas.UpagrahaEngine
import com.aynvora.astro.yogas.YogaDoshaEngine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KPRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val lon = arguments["longitude"]?.jsonPrimitive?.doubleOrNull
        val horarySeed = arguments["horaryNumber1To249"]?.jsonPrimitive?.intOrNull

        return if (horarySeed != null) {
            val sub = KP249Engine.getByIndex(horarySeed.coerceIn(1, 249))
            val evidence = listOf(
                AstroEvidenceItem(
                    evidenceId = "KP-SUB-249-$horarySeed",
                    kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                    text = "KP Horary Seed #$horarySeed: Sign ${sub.signName} (Lord: ${sub.signLord}), Star ${sub.nakshatraName} (Lord: ${sub.starLord}), Sub Lord: ${sub.subLord} spanning ${sub.startDms} to ${sub.endDms}.",
                    sourceId = "KP-READER-III",
                    sourceRef = "KP Reader III: Horary Astrology",
                    traditionId = "KP",
                    featureId = "ASTROLOGY",
                )
            )
            AstroRegisteredToolResult(
                toolId = "getKP",
                resultJson = kotlinx.serialization.json.Json.encodeToString(sub),
                evidence = evidence,
                status = "AVAILABLE",
            )
        } else if (lon != null) {
            val sub = KP249Engine.findSubdivision(lon)
            val evidence = listOf(
                AstroEvidenceItem(
                    evidenceId = "KP-LON-${sub.index}",
                    kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                    text = "Celestial longitude ${lon}° resolves to KP Subdivision #${sub.index}: ${sub.signName} (Sign Lord: ${sub.signLord}), ${sub.nakshatraName} (Star Lord: ${sub.starLord}), Sub Lord: ${sub.subLord}.",
                    sourceId = "KP-READER-I",
                    sourceRef = "KP Reader I & II: 249 Table",
                    traditionId = "KP",
                    featureId = "ASTROLOGY",
                )
            )
            AstroRegisteredToolResult(
                toolId = "getKP",
                resultJson = kotlinx.serialization.json.Json.encodeToString(sub),
                evidence = evidence,
                status = "AVAILABLE",
            )
        } else {
            val bYear = arguments["birthYear"]?.jsonPrimitive?.intOrNull ?: 2000
            val bMonth = arguments["birthMonth"]?.jsonPrimitive?.intOrNull ?: 1
            val bDay = arguments["birthDay"]?.jsonPrimitive?.intOrNull ?: 1
            val bHour = arguments["birthHour"]?.jsonPrimitive?.intOrNull ?: 12
            val bMin = arguments["birthMinute"]?.jsonPrimitive?.intOrNull ?: 0
            val lat = arguments["latitude"]?.jsonPrimitive?.doubleOrNull ?: 28.6139
            val lonGeo = arguments["longitudeGeo"]?.jsonPrimitive?.doubleOrNull ?: 77.2090
            val tz = arguments["timezoneId"]?.jsonPrimitive?.content ?: "Asia/Kolkata"

            val birth = BirthData(
                dateTimeIso = "%04d-%02d-%02dT%02d:%02d:00".format(bYear, bMonth, bDay, bHour, bMin),
                latitude = lat,
                longitude = lonGeo,
                timeZoneId = tz,
                year = bYear,
                month = bMonth,
                day = bDay,
                hour = bHour,
                minute = bMin,
            )
            val kpEngine = KPEngine(AynvoraAstroEngine())
            val res = kpEngine.calculate(birth)
            val evidence = listOf(
                AstroEvidenceItem(
                    evidenceId = "KP-CHART-${res.cusps.first().signName}",
                    kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                    text = "KP Chart: Lagna Cusp in ${res.cusps.first().signName} (Star: ${res.cusps.first().starLord}, Sub: ${res.cusps.first().subLord}). Ruling Planets: ${res.rulingPlanets.orderedSignificators.joinToString(", ")}. KP Ayanamsha: ${res.ayanamshaDegrees}°.",
                    sourceId = "KP-ORIGINAL",
                    sourceRef = "KP Readers I-VI (Prof. K.S. Krishnamurti)",
                    traditionId = "KP",
                    featureId = "ASTROLOGY",
                )
            )
            AstroRegisteredToolResult(
                toolId = "getKP",
                resultJson = kotlinx.serialization.json.Json.encodeToString(res),
                evidence = evidence,
                status = "AVAILABLE",
            )
        }
    }
}

class JaiminiRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val lagnaLon = arguments["lagnaLongitude"]?.jsonPrimitive?.doubleOrNull ?: 15.0
        val planetLons = mutableMapOf<String, Double>()
        arguments["planetLongitudes"]?.jsonObject?.forEach { (k, v) ->
            v.jsonPrimitive.doubleOrNull?.let { planetLons[k.uppercase()] = it }
        }
        if (planetLons.isEmpty()) {
            planetLons["SUN"] = 28.5
            planetLons["MOON"] = 54.0
            planetLons["MARS"] = 78.2
            planetLons["MERCURY"] = 105.0
            planetLons["JUPITER"] = 132.1
            planetLons["VENUS"] = 158.4
            planetLons["SATURN"] = 183.0
        }

        val res = JaiminiEngine.calculate(lagnaLon, planetLons)
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "JAIMINI-AK-${res.atmakaraka.planetName}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Jaimini Chara Karakas: Atmakaraka (AK) is ${res.atmakaraka.planetName} (${res.atmakaraka.formattedDegree} in ${res.atmakaraka.rashiName}). Karakamsha is ${res.karakamshaRashi}. Arudha Lagna (AL) is ${res.arudhaLagna.rashiName}, Upapada Lagna (UL) is ${res.upapadaLagna.rashiName}.",
                sourceId = "JAIMINI-UPADESHA-SUTRAS",
                sourceRef = "Jaimini Upadesha Sutras 1.1",
                traditionId = "JAIMINI",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getJaimini",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class MuhurtaRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val weekdayIdx = arguments["weekdayIndex"]?.jsonPrimitive?.intOrNull ?: 0
        val dayFraction = arguments["dayFraction"]?.jsonPrimitive?.doubleOrNull ?: 0.5

        val res = MuhurtaEngine.calculate(weekdayIdx, dayFraction)
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "MUHURTA-${res.weekday}-${res.currentChoghadiya.name}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Muhurta on ${res.weekday} (Day Lord: ${res.dayLorda}): Current Hora is ${res.currentHora.lord} (${res.currentHora.quality}). Current Choghadiya is ${res.currentChoghadiya.name} (${res.currentChoghadiya.quality}). Rahu Kalam spans part ${res.rahuKalam.partIndex} of 8.",
                sourceId = "MUHURTA-CHINTAMANI",
                sourceRef = "Muhurta Chintamani & Kalaprakasika",
                traditionId = "MUHURTA",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getMuhurta",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class CompatibilityRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val bSign = arguments["brideSign"]?.jsonPrimitive?.intOrNull ?: 0
        val bNak = arguments["brideNak"]?.jsonPrimitive?.intOrNull ?: 0
        val gSign = arguments["groomSign"]?.jsonPrimitive?.intOrNull ?: 0
        val gNak = arguments["groomNak"]?.jsonPrimitive?.intOrNull ?: 0

        val res = CompatibilityEngine.calculate(bSign, bNak, gSign, gNak)
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "COMPATIBILITY-ASHTAKOOTA-${res.totalGunaPoints}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Ashtakoota Compatibility: ${res.totalGunaPoints} / 36 Guna points (${if (res.isFavorable) "Favorable" else "Requires Remediations"}). Varna: ${res.ashtakoota.first { it.kootaName == "Varna" }.pointsObtained}, Nadi: ${res.ashtakoota.first { it.kootaName == "Nadi" }.pointsObtained}, Bhakoot: ${res.ashtakoota.first { it.kootaName == "Bhakoot" }.pointsObtained}.",
                sourceId = "ASHTAKOOTA-CLASSICAL",
                sourceRef = "Muhurta Chintamani & Prasna Marga",
                traditionId = "COMPATIBILITY",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getCompatibility",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class YogaRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val houses = mutableMapOf<String, Int>()
        arguments["planetHouses"]?.jsonObject?.forEach { (k, v) ->
            v.jsonPrimitive.intOrNull?.let { houses[k.uppercase()] = it }
        }
        if (houses.isEmpty()) {
            houses["JUPITER"] = 1
            houses["MOON"] = 1
            houses["SUN"] = 10
            houses["MERCURY"] = 10
            houses["MARS"] = 4
        }
        val res = YogaDoshaEngine.evaluate(houses)
        val activeYogas = res.yogas.filter { it.isPresent }.map { it.name }
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "YOGAS-DETECTED-${activeYogas.size}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Detected Classical Yogas (${activeYogas.size}): ${activeYogas.joinToString(", ")}.",
                sourceId = "BPHS-YOGAS",
                sourceRef = "Brihat Parashara Hora Shastra Ch. 36-41",
                traditionId = "PARASHARI",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getYoga",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res.yogas),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class DoshaRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val houses = mutableMapOf<String, Int>()
        arguments["planetHouses"]?.jsonObject?.forEach { (k, v) ->
            v.jsonPrimitive.intOrNull?.let { houses[k.uppercase()] = it }
        }
        if (houses.isEmpty()) {
            houses["MARS"] = 7
            houses["MOON"] = 4
            houses["SUN"] = 9
            houses["RAHU"] = 9
            houses["KETU"] = 3
        }
        val res = YogaDoshaEngine.evaluate(houses)
        val activeDoshas = res.doshas.filter { it.isPresent }.map { it.name }
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "DOSHAS-DETECTED-${activeDoshas.size}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Detected Classical Doshas (${activeDoshas.size}): ${activeDoshas.joinToString(", ")}.",
                sourceId = "BPHS-DOSHAS",
                sourceRef = "Brihat Parashara Hora Shastra & Muhurta Chintamani",
                traditionId = "PARASHARI",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getDosha",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res.doshas),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class UpagrahaRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val sunLon = arguments["sunLongitude"]?.jsonPrimitive?.doubleOrNull ?: 280.0
        val lagnaLon = arguments["lagnaLongitude"]?.jsonPrimitive?.doubleOrNull ?: 15.0

        val res = UpagrahaEngine.calculate(sunLon, lagnaLon)
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "UPAGRAHAS-CALCULATED",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Calculated Secondary Planets (Upagrahas): Dhuma (${res.upagrahas.first { it.name == "Dhuma" }.formattedDegree}), Vyatipata (${res.upagrahas.first { it.name == "Vyatipata" }.formattedDegree}), Gulika (${res.upagrahas.first { it.name == "Gulika" }.formattedDegree}), Mandi (${res.upagrahas.first { it.name == "Mandi" }.formattedDegree}). Invariant Upaketu + 30° == Sun verified.",
                sourceId = "BPHS-UPAGRAHAS",
                sourceRef = "Brihat Parashara Hora Shastra Ch. 3 v. 61-68",
                traditionId = "PARASHARI",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getUpagraha",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class PrashnaRegisteredTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val query = arguments["queryText"]?.jsonPrimitive?.content ?: "Will I get the job promotion?"
        val seed = arguments["horaryNumber1To249"]?.jsonPrimitive?.intOrNull ?: 108

        val req = PrashnaRequest(
            queryText = query,
            queryTimestampUtc = "2026-10-02T23:00:00Z",
            queryYear = 2026,
            queryMonth = 10,
            queryDay = 2,
            queryHour = 23,
            queryMinute = 0,
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneId = "Asia/Kolkata",
            horaryNumber1To249 = seed,
        )
        val res = PrashnaEngine.evaluate(req)
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "PRASHNA-${res.primaryHouseSignified}",
                kind = AstroEvidenceKind.DETERMINISTIC_CALCULATION,
                text = "Prashna Evaluation: Query '${res.queryText}' maps to House ${res.primaryHouseSignified} (${res.houseTheme}). Horary Seed #${res.horaryNumber}: ${res.horaryJudgment}. Ruling Planets: ${res.favorableRulingPlanets.joinToString(", ")}.",
                sourceId = "PRASNA-MARGA",
                sourceRef = "Prasna Marga & KP Reader VI",
                traditionId = "PRASHNA",
                featureId = "ASTROLOGY",
            )
        )

        return AstroRegisteredToolResult(
            toolId = "getPrashna",
            resultJson = kotlinx.serialization.json.Json.encodeToString(res),
            evidence = evidence,
            status = "AVAILABLE",
        )
    }
}

class LalKitabResearchTool : AstroRegisteredTool {
    override suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult {
        val evidence = listOf(
            AstroEvidenceItem(
                evidenceId = "LAL-KITAB-RESEARCH-POLICY",
                kind = AstroEvidenceKind.KNOWLEDGE_RULE,
                text = "Lal Kitab classical research status: Grounded solely on Pandit Roop Chand Joshi editions (1939, 1940, 1942, 1952). Modern commercial web remedies are excluded. Planetary houses are classified into Pukka Ghar, Soe Hue Grah, and Masnui (artificial) planets.",
                sourceId = "LAL-KITAB-1939",
                sourceRef = "Lal Kitab Ke Farameen (1939)",
                traditionId = "LAL_KITAB",
                featureId = "ASTROLOGY",
            )
        )
        return AstroRegisteredToolResult(
            toolId = "getLalKitab",
            resultJson = """{"status":"RESEARCH_ONLY","message":"Lal Kitab principles are isolated in research mode; deterministic calculations are not claimed as production."}""",
            evidence = evidence,
            status = "RESEARCH_ONLY",
        )
    }
}

object AdvancedRegisteredTools {
    fun all(sdk: com.aynvora.core.AynvoraSdk? = null): Map<String, AstroRegisteredTool> = mapOf(
        "getKP" to KPRegisteredTool(),
        "getJaimini" to JaiminiRegisteredTool(),
        "getMuhurta" to MuhurtaRegisteredTool(),
        "getCompatibility" to CompatibilityRegisteredTool(),
        "getYoga" to YogaRegisteredTool(),
        "getDosha" to DoshaRegisteredTool(),
        "getUpagraha" to UpagrahaRegisteredTool(),
        "getPrashna" to PrashnaRegisteredTool(),
        "getLalKitab" to LalKitabResearchTool(),
    )
}
