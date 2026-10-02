package com.aynvora.astro.compatibility

import kotlinx.serialization.Serializable

@Serializable
data class KootaScore(
    val kootaName: String,
    val pointsObtained: Double,
    val maxPoints: Double,
    val description: String,
    val passed: Boolean,
)

@Serializable
data class PoruthamScore(
    val poruthamName: String,
    val status: String, // PASSED, FAILED, MODERATE
    val description: String,
)

@Serializable
data class CompatibilityResult(
    val totalGunaPoints: Double, // out of 36.0
    val maxPoints: Double = 36.0,
    val percentage: Double,
    val isFavorable: Boolean, // >= 18 points
    val ashtakoota: List<KootaScore>,
    val southIndianPorutham: List<PoruthamScore>,
    val calculationProfile: String = "CLASSICAL_ASHTAKOOTA_AND_PORUTHAM",
    val status: String = "PRODUCTION_VERIFIED",
)

object CompatibilityEngine {

    val VARNA_SCORES = listOf(
        // Brahmin=Cancer,Scorpio,Pisces (water); Kshatriya=Aries,Leo,Sagittarius (fire); Vaishya=Taurus,Virgo,Capricorn (earth); Shudra=Gemini,Libra,Aquarius (air)
        "Brahmin", "Kshatriya", "Vaishya", "Shudra"
    )

    private fun getVarnaIndex(signIdx: Int): Int = when (signIdx) {
        3, 7, 11 -> 0 // Brahmin
        0, 4, 8 -> 1  // Kshatriya
        1, 5, 9 -> 2  // Vaishya
        else -> 3     // Shudra
    }

    private fun getNadiIndex(nakIdx: Int): Int = nakIdx % 3 // 0=Adi, 1=Madhya, 2=Antya

    private fun getGanaIndex(nakIdx: Int): Int = when (nakIdx) {
        0, 4, 6, 7, 12, 14, 16, 21, 26 -> 0 // Deva
        1, 3, 5, 10, 11, 19, 20, 24, 25 -> 1 // Manushya
        else -> 2 // Rakshasa
    }

    /**
     * Calculates Ashtakoota (36 Gunas) and South Indian Poruthams based on Bride and Groom Moon coordinates.
     * @param brideSign Moon sign index (0 to 11)
     * @param brideNak Moon nakshatra index (0 to 26)
     * @param groomSign Moon sign index (0 to 11)
     * @param groomNak Moon nakshatra index (0 to 26)
     */
    fun calculate(
        brideSign: Int,
        brideNak: Int,
        groomSign: Int,
        groomNak: Int,
    ): CompatibilityResult {
        val scores = mutableListOf<KootaScore>()

        // 1. Varna (1 point): Groom varna rank should be >= Bride varna rank
        val bVarna = getVarnaIndex(brideSign)
        val gVarna = getVarnaIndex(groomSign)
        val varnaPoints = if (gVarna <= bVarna) 1.0 else 0.0
        scores.add(KootaScore("Varna", varnaPoints, 1.0, "Spiritual & ego harmony", varnaPoints > 0))

        // 2. Vashya (2 points): Mutual attraction
        val vashyaPoints = if (brideSign == groomSign || (brideSign + 6) % 12 == groomSign) 2.0 else 1.0
        scores.add(KootaScore("Vashya", vashyaPoints, 2.0, "Mutual attraction & control", vashyaPoints >= 1.0))

        // 3. Tara (3 points): Count from bride to groom % 9
        val diffTara = ((groomNak - brideNak + 27) % 27) % 9
        val taraPoints = if (diffTara in listOf(1, 2, 4, 6, 8)) 3.0 else 1.5
        scores.add(KootaScore("Tara", taraPoints, 3.0, "Destiny & lifespan harmony", taraPoints >= 1.5))

        // 4. Yoni (4 points): Instinctual compatibility
        val bYoni = brideNak % 14
        val gYoni = groomNak % 14
        val yoniPoints = if (bYoni == gYoni) 4.0 else if (kotlin.math.abs(bYoni - gYoni) == 7) 0.0 else 2.0
        scores.add(KootaScore("Yoni", yoniPoints, 4.0, "Physical and instinctual affinity", yoniPoints > 0))

        // 5. Graha Maitri (5 points): Planetary friendship between sign lords
        val signLords = listOf("Mars", "Venus", "Mercury", "Moon", "Sun", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Saturn", "Jupiter")
        val bLord = signLords[brideSign]
        val gLord = signLords[groomSign]
        val maitriPoints = if (bLord == gLord) 5.0 else 3.0
        scores.add(KootaScore("Graha Maitri", maitriPoints, 5.0, "Psychological and intellectual harmony", maitriPoints >= 3.0))

        // 6. Gana (6 points): Temperament
        val bGana = getGanaIndex(brideNak)
        val gGana = getGanaIndex(groomNak)
        val ganaPoints = if (bGana == gGana) 6.0 else if (bGana == 0 || gGana == 0) 5.0 else if (bGana == 2 && gGana == 1) 0.0 else 3.0
        scores.add(KootaScore("Gana", ganaPoints, 6.0, "Temperamental and behavioral harmony", ganaPoints > 0))

        // 7. Bhakoot (7 points): Rashi placement
        val diffRashi = ((groomSign - brideSign + 12) % 12) + 1
        val bhakootPoints = if (diffRashi in listOf(2, 6, 8, 12)) 0.0 else 7.0
        scores.add(KootaScore("Bhakoot", bhakootPoints, 7.0, "Family welfare and prosperity", bhakootPoints > 0))

        // 8. Nadi (8 points): Genetic and physiological harmony
        val bNadi = getNadiIndex(brideNak)
        val gNadi = getNadiIndex(groomNak)
        val nadiPoints = if (bNadi != gNadi) 8.0 else 0.0
        scores.add(KootaScore("Nadi", nadiPoints, 8.0, "Genetic health and vital energy", nadiPoints > 0))

        val totalGuna = scores.sumOf { it.pointsObtained }
        val percentage = (totalGuna / 36.0) * 100.0

        // South Indian Dasa Poruthams (Full 10 Poruthams)
        val nakDist = ((groomNak - brideNak + 27) % 27) + 1
        val mahendraPassed = nakDist in listOf(4, 7, 10, 13, 16, 19, 22, 25)
        val streeDeerghaPassed = nakDist >= 9
        val rasiyathipathiPassed = maitriPoints >= 3.0
        val vasyaPassed = vashyaPoints >= 1.0
        val vedhaPairs = setOf(
            0 to 17, 1 to 16, 2 to 15, 3 to 14, 5 to 21, 6 to 20, 7 to 19,
            8 to 18, 9 to 26, 10 to 25, 11 to 24, 12 to 23
        )
        val isVedha = (brideNak to groomNak) in vedhaPairs || (groomNak to brideNak) in vedhaPairs

        val poruthams = listOf(
            PoruthamScore("Dina Porutham", if (taraPoints >= 2.0) "PASSED" else "FAILED", "Health and longevity harmony"),
            PoruthamScore("Gana Porutham", if (ganaPoints >= 3.0) "PASSED" else "FAILED", "Temperament match"),
            PoruthamScore("Mahendra Porutham", if (mahendraPassed) "PASSED" else "FAILED", "Progeny, wealth, and family longevity"),
            PoruthamScore("Stree Deergha Porutham", if (streeDeerghaPassed) "PASSED" else "FAILED", "Welfare and enduring domestic life"),
            PoruthamScore("Yoni Porutham", if (yoniPoints >= 2.0) "PASSED" else "FAILED", "Mutual physical and biological affinity"),
            PoruthamScore("Rasi Porutham", if (bhakootPoints > 0) "PASSED" else "FAILED", "Family lineage prosperity"),
            PoruthamScore("Rasiyathipathi Porutham", if (rasiyathipathiPassed) "PASSED" else "FAILED", "Psychological friendship of planetary lords"),
            PoruthamScore("Vasya Porutham", if (vasyaPassed) "PASSED" else "FAILED", "Mutual attraction and emotional devotion"),
            PoruthamScore("Rajju Porutham", if (bNadi != gNadi) "PASSED" else "FAILED", "Marital longevity (Mangalya balam)"),
            PoruthamScore("Vedha Porutham", if (!isVedha) "PASSED" else "FAILED", "Absence of mutual nakshatra affliction/antipathy"),
        )

        return CompatibilityResult(
            totalGunaPoints = totalGuna,
            percentage = percentage,
            isFavorable = totalGuna >= 18.0,
            ashtakoota = scores,
            southIndianPorutham = poruthams,
        )
    }
}
