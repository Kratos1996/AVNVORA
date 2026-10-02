package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.AstroChart

/**
 * Classical Sahams (संवेदनशील बिन्दु / Arabic Parts in Tajika) Engine based on Tajika Neelakanthi (1907):
 * - Samjna Tantra, Saham Chapter, vv. 5–6, 12; PDF pp. 87–89 (printed pp. 79–81).
 * Features exact Shodhya-Shuddhyashraya arc inclusion (+30° / सैकभम्) correction.
 */
object SahamEngine {

    val SOURCE_REF = "Tājika Nīlakaṇṭhī (1907), Samjna tantra, Saham chapter, Verses 5, 6, 12; PDF pp.87–89 (printed pp.79–81)"

    /**
     * Applies the classical Shodhya-Shuddhyashraya arc rule:
     * "शोध्यर्क्षशुद्ध्याश्रयभान्तराले लग्नं न चेत्सैकभमेतदुक्तम् ॥ ५ ॥"
     *
     * @param pointA Shuddhyashraya (the point from which B is subtracted)
     * @param pointB Shodhya (the subtracted point)
     * @param lagna Ascendant longitude
     * @return corrected final longitude in [0, 360)
     */
    fun calculateSahamLongitude(pointA: Double, pointB: Double, lagna: Double): Pair<Double, Boolean> {
        val normA = ((pointA % 360.0) + 360.0) % 360.0
        val normB = ((pointB % 360.0) + 360.0) % 360.0
        val normLagna = ((lagna % 360.0) + 360.0) % 360.0

        val base = ((normA - normB + normLagna) % 360.0 + 360.0) % 360.0

        // Forward zodiacal arc from B to A
        val arcBtoA = ((normA - normB + 360.0) % 360.0)
        // Forward zodiacal arc from B to Lagna
        val arcBtoLagna = ((normLagna - normB + 360.0) % 360.0)

        // Lagna is inside the arc if 0 < arcBtoLagna < arcBtoA
        val lagnaInArc = arcBtoLagna > 0.00001 && arcBtoLagna < (arcBtoA - 0.00001)

        // If Lagna is NOT in the arc, add 1 sign (30 degrees / सैकभम्)
        val correction = if (!lagnaInArc) 30.0 else 0.0
        val finalLong = ((base + correction) % 360.0 + 360.0) % 360.0
        return finalLong to (!lagnaInArc)
    }

    fun calculate(
        chart: AstroChart,
        isDay: Boolean = VarsheshwaraEngine.isDayReturn(chart),
    ): List<SahamResult> {
        val lagna = chart.ascendant?.longitude ?: chart.ascendant?.sign?.let { it.index * 30.0 } ?: 0.0

        fun getPlanetLong(name: String): Double {
            return chart.houses.asSequence()
                .flatMap { it.planets }
                .firstOrNull { it.planetId.equals(name, ignoreCase = true) }
                ?.longitude ?: 0.0
        }

        val sun = getPlanetLong("SUN")
        val moon = getPlanetLong("MOON")
        val mars = getPlanetLong("MARS")
        val mercury = getPlanetLong("MERCURY")
        val jupiter = getPlanetLong("JUPITER")

        // 1. Punya Saham (Fortune):
        // Day: Moon - Sun + Lagna; Night: Sun - Moon + Lagna
        val (punyaA, punyaB) = if (isDay) moon to sun else sun to moon
        val (punyaNameA, punyaNameB) = if (isDay) "MOON" to "SUN" else "SUN" to "MOON"
        val (punyaLong, punyaCorr) = calculateSahamLongitude(punyaA, punyaB, lagna)
        val punya = SahamResult(
            id = "PUNYA",
            name = "Punya Saham (Fortune / Merit)",
            longitude = punyaLong,
            formula = if (isDay) "Lagna + Moon - Sun${if (punyaCorr) " + 30° (Saika-bham)" else ""}"
                      else "Lagna + Sun - Moon${if (punyaCorr) " + 30° (Saika-bham)" else ""}",
            pointA = punyaNameA,
            pointB = punyaNameB,
            pointC = "LAGNA",
            dayFormula = "Lagna + Moon - Sun",
            nightFormula = "Lagna + Sun - Moon",
            specialConditions = "If Lagna is not in forward arc between Shodhya and Shuddhyashraya, +30° is added.",
            source = "Tajika Neelakanthi (1907)",
            chapter = "Samjna tantra, Saham chapter",
            page = "PDF p.87 (printed p.79), Verse 5",
            sourceRef = SOURCE_REF,
            validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
        )

        // 2. Vidya / Guru Saham (Knowledge/Education):
        // Day: Sun - Moon + Lagna; Night: Moon - Sun + Lagna
        val (vidyaA, vidyaB) = if (isDay) sun to moon else moon to sun
        val (vidyaNameA, vidyaNameB) = if (isDay) "SUN" to "MOON" else "MOON" to "SUN"
        val (vidyaLong, vidyaCorr) = calculateSahamLongitude(vidyaA, vidyaB, lagna)
        val vidya = SahamResult(
            id = "VIDYA",
            name = "Vidya / Guru Saham (Knowledge / Wisdom)",
            longitude = vidyaLong,
            formula = if (isDay) "Lagna + Sun - Moon${if (vidyaCorr) " + 30° (Saika-bham)" else ""}"
                      else "Lagna + Moon - Sun${if (vidyaCorr) " + 30° (Saika-bham)" else ""}",
            pointA = vidyaNameA,
            pointB = vidyaNameB,
            pointC = "LAGNA",
            dayFormula = "Lagna + Sun - Moon",
            nightFormula = "Lagna + Moon - Sun",
            specialConditions = "Inverse of Punya Saham; +30° arc correction applies.",
            source = "Tajika Neelakanthi (1907)",
            chapter = "Samjna tantra, Saham chapter",
            page = "PDF p.88 (printed p.80), Verse 6",
            sourceRef = SOURCE_REF,
            validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
        )

        // 3. Yasas Saham (Fame / Glory):
        // Day: Jupiter - Punya + Lagna; Night: Punya - Jupiter + Lagna
        val (yasasA, yasasB) = if (isDay) jupiter to punyaLong else punyaLong to jupiter
        val (yasasNameA, yasasNameB) = if (isDay) "JUPITER" to "PUNYA" else "PUNYA" to "JUPITER"
        val (yasasLong, yasasCorr) = calculateSahamLongitude(yasasA, yasasB, lagna)
        val yasas = SahamResult(
            id = "YASAS",
            name = "Yasas Saham (Fame / Renown)",
            longitude = yasasLong,
            formula = if (isDay) "Lagna + Jupiter - Punya${if (yasasCorr) " + 30° (Saika-bham)" else ""}"
                      else "Lagna + Punya - Jupiter${if (yasasCorr) " + 30° (Saika-bham)" else ""}",
            pointA = yasasNameA,
            pointB = yasasNameB,
            pointC = "LAGNA",
            dayFormula = "Lagna + Jupiter - Punya",
            nightFormula = "Lagna + Punya - Jupiter",
            specialConditions = "Depends on Punya Saham; +30° arc correction applies.",
            source = "Tajika Neelakanthi (1907)",
            chapter = "Samjna tantra, Saham chapter",
            page = "PDF p.88 (printed p.80), Verse 6",
            sourceRef = SOURCE_REF,
            validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
        )

        // 4. Karma Saham (Profession / Action):
        // Day: Mars - Mercury + Lagna; Night: Mercury - Mars + Lagna
        val (karmaA, karmaB) = if (isDay) mars to mercury else mercury to mars
        val (karmaNameA, karmaNameB) = if (isDay) "MARS" to "MERCURY" else "MERCURY" to "MARS"
        val (karmaLong, karmaCorr) = calculateSahamLongitude(karmaA, karmaB, lagna)
        val karma = SahamResult(
            id = "KARMA",
            name = "Karma Saham (Profession / Deeds)",
            longitude = karmaLong,
            formula = if (isDay) "Lagna + Mars - Mercury${if (karmaCorr) " + 30° (Saika-bham)" else ""}"
                      else "Lagna + Mercury - Mars${if (karmaCorr) " + 30° (Saika-bham)" else ""}",
            pointA = karmaNameA,
            pointB = karmaNameB,
            pointC = "LAGNA",
            dayFormula = "Lagna + Mars - Mercury",
            nightFormula = "Lagna + Mercury - Mars",
            specialConditions = "+30° arc correction applies.",
            source = "Tajika Neelakanthi (1907)",
            chapter = "Samjna tantra, Saham chapter",
            page = "PDF p.89 (printed p.81), Verse 12",
            sourceRef = SOURCE_REF,
            validationStatus = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
        )

        return listOf(punya, vidya, yasas, karma)
    }
}
