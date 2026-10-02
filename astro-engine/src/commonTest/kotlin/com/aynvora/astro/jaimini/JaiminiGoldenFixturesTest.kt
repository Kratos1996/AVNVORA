package com.aynvora.astro.jaimini

import kotlin.test.Test
import kotlin.test.assertEquals

class JaiminiGoldenFixturesTest {

    @Test
    fun testCharaKarakasOrdering() {
        val longitudes = mapOf(
            "SUN" to 28.5,     // 28.5° in sign -> Highest -> AK
            "MOON" to 25.2,    // 2nd -> AmK
            "MARS" to 22.0,    // 3rd -> BK
            "MERCURY" to 19.8, // 4th -> MK
            "JUPITER" to 15.1, // 5th -> PK
            "VENUS" to 8.4,    // 6th -> GK
            "SATURN" to 3.2    // 7th -> Lowest -> DK
        )

        val result = JaiminiEngine.calculate(
            lagnaLongitude = 15.0, // Aries Lagna
            planetLongitudes = longitudes
        )

        val karakas = result.karakas.associate { it.role to it.planetName }
        assertEquals("SUN", karakas[JaiminiKarakaRole.ATMAKARAKA])
        assertEquals("MOON", karakas[JaiminiKarakaRole.AMATYAKARAKA])
        assertEquals("MARS", karakas[JaiminiKarakaRole.BHRATRIKARAKA])
        assertEquals("MERCURY", karakas[JaiminiKarakaRole.MATRIKARAKA])
        assertEquals("JUPITER", karakas[JaiminiKarakaRole.PUTRAKARAKA])
        assertEquals("VENUS", karakas[JaiminiKarakaRole.GNATIKARAKA])
        assertEquals("SATURN", karakas[JaiminiKarakaRole.DARAKARAKA])
    }

    @Test
    fun testArudhaLagna10HouseJumpException() {
        // Aries Lagna (Sign 0). Mars is lord of Aries.
        // Mars in Aries (Sign 0). Distance = 1 sign.
        // Direct count lands in 1st house (Aries).
        // Jaimini exception rule: If Arudha lands in 1st or 7th from house, jump 10 houses forward!
        // 1st house + 10 houses - 1 = 10th house (Capricorn / Sign 9).
        val result = JaiminiEngine.calculate(
            lagnaLongitude = 15.0, // Aries
            planetLongitudes = mapOf(
                "MARS" to 5.0, // Aries
                "SUN" to 10.0,
                "MOON" to 12.0,
                "MERCURY" to 15.0,
                "JUPITER" to 20.0,
                "VENUS" to 22.0,
                "SATURN" to 25.0
            )
        )

        val al = result.arudhas.single { it.label == "AL" }
        assertEquals("Capricorn", al.rashiName)
        assertEquals(true, al.appliedException)
    }

    @Test
    fun testJaiminiRashiAspects() {
        val aspects = JaiminiEngine.calculateRashiAspects()

        // Aries (Movable) aspects all Fixed signs (Leo, Scorpio, Aquarius) EXCEPT adjacent (Taurus)
        val ariesAspects = aspects.filter { it.sourceRashi == "Aries" }.map { it.targetRashi }
        assertEquals(listOf("Leo", "Scorpio", "Aquarius"), ariesAspects)

        // Taurus (Fixed) aspects all Movable signs (Cancer, Libra, Capricorn) EXCEPT adjacent (Aries)
        val taurusAspects = aspects.filter { it.sourceRashi == "Taurus" }.map { it.targetRashi }
        assertEquals(listOf("Cancer", "Libra", "Capricorn"), taurusAspects)

        // Gemini (Dual) aspects all other Dual signs (Virgo, Sagittarius, Pisces)
        val geminiAspects = aspects.filter { it.sourceRashi == "Gemini" }.map { it.targetRashi }
        assertEquals(listOf("Virgo", "Sagittarius", "Pisces"), geminiAspects)
    }
}
