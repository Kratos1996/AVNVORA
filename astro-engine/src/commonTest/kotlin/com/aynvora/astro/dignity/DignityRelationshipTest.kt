package com.aynvora.astro.dignity

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.relationship.CompoundRelationshipType
import com.aynvora.astro.relationship.NaturalRelationshipType
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.relationship.TemporaryRelationshipType
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DignityRelationshipTest {

    private fun dummyBody(bodyId: BodyId, rashiIndex: Int, degreeInRashi: Double): BodyPosition {
        val sidereal = rashiIndex * 30.0 + degreeInRashi
        return BodyPosition(
            bodyId = bodyId,
            tropicalLongitude = sidereal + 24.0,
            siderealLongitude = sidereal,
            rashiIndex = rashiIndex,
            rashiName = "Sign_$rashiIndex",
            degreeInRashi = degreeInRashi,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = false,
            dailyMotionDegrees = 1.0,
        )
    }

    // ==========================================
    // 1. Sign Ownership (ASTRO-R25)
    // ==========================================

    @Test
    fun testSignLordsMapping() {
        assertEquals(BodyId.MARS, PlanetaryDignityCalculator.getSignLord(0)) // Aries
        assertEquals(BodyId.VENUS, PlanetaryDignityCalculator.getSignLord(1)) // Taurus
        assertEquals(BodyId.MERCURY, PlanetaryDignityCalculator.getSignLord(2)) // Gemini
        assertEquals(BodyId.MOON, PlanetaryDignityCalculator.getSignLord(3)) // Cancer
        assertEquals(BodyId.SUN, PlanetaryDignityCalculator.getSignLord(4)) // Leo
        assertEquals(BodyId.MERCURY, PlanetaryDignityCalculator.getSignLord(5)) // Virgo
        assertEquals(BodyId.VENUS, PlanetaryDignityCalculator.getSignLord(6)) // Libra
        assertEquals(BodyId.MARS, PlanetaryDignityCalculator.getSignLord(7)) // Scorpio
        assertEquals(BodyId.JUPITER, PlanetaryDignityCalculator.getSignLord(8)) // Sagittarius
        assertEquals(BodyId.SATURN, PlanetaryDignityCalculator.getSignLord(9)) // Capricorn
        assertEquals(BodyId.SATURN, PlanetaryDignityCalculator.getSignLord(10)) // Aquarius
        assertEquals(BodyId.JUPITER, PlanetaryDignityCalculator.getSignLord(11)) // Pisces
    }

    // ==========================================
    // 2. Exaltation & Debilitation (ASTRO-R26)
    // ==========================================

    @Test
    fun testSunExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.SUN to 0, BodyId.MARS to 4)
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 0, 10.0, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(10.0, exalted.deepExaltationDegree)
        assertEquals(10.0, exalted.deepDebilitationDegree)

        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 6, 10.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
    }

    @Test
    fun testMoonExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.MOON to 1, BodyId.VENUS to 4)
        // Taurus [0, 3) is Exaltation
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 1, 2.5, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(3.0, exalted.deepExaltationDegree)

        // Scorpio [0, 30) is Debilitation
        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 7, 3.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(3.0, debilitated.deepDebilitationDegree)
    }

    @Test
    fun testMarsExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.MARS to 9, BodyId.SATURN to 4)
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.MARS, 9, 28.0, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(28.0, exalted.deepExaltationDegree)

        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.MARS, 3, 28.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(28.0, debilitated.deepDebilitationDegree)
    }

    @Test
    fun testMercuryExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.MERCURY to 5)
        // Virgo [0, 15) is Exaltation
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 5, 14.5, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(15.0, exalted.deepExaltationDegree)

        // Pisces is Debilitation
        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 11, 15.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(15.0, debilitated.deepDebilitationDegree)
    }

    @Test
    fun testJupiterExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.JUPITER to 3, BodyId.MOON to 4)
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.JUPITER, 3, 5.0, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(5.0, exalted.deepExaltationDegree)

        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.JUPITER, 9, 5.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(5.0, debilitated.deepDebilitationDegree)
    }

    @Test
    fun testVenusExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.VENUS to 11, BodyId.JUPITER to 4)
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.VENUS, 11, 27.0, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(27.0, exalted.deepExaltationDegree)

        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.VENUS, 5, 27.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(27.0, debilitated.deepDebilitationDegree)
    }

    @Test
    fun testSaturnExaltationAndDebilitation() {
        val signMap = mapOf(BodyId.SATURN to 6, BodyId.VENUS to 4)
        val exalted = PlanetaryDignityCalculator.evaluateDignity(BodyId.SATURN, 6, 20.0, signMap)
        assertEquals(DignityType.EXALTATION, exalted.dignityType)
        assertTrue(exalted.isExalted)
        assertEquals(20.0, exalted.deepExaltationDegree)

        val debilitated = PlanetaryDignityCalculator.evaluateDignity(BodyId.SATURN, 0, 20.0, signMap)
        assertEquals(DignityType.DEBILITATION, debilitated.dignityType)
        assertTrue(debilitated.isDebilitated)
        assertEquals(20.0, debilitated.deepDebilitationDegree)
    }

    // ==========================================
    // 3. Moolatrikona Degree Boundaries (ASTRO-R27)
    // ==========================================

    @Test
    fun testSunMoolatrikonaBoundary() {
        val signMap = mapOf(BodyId.SUN to 4)
        // Leo [0, 20) is Moolatrikona
        val moolaStart = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 4, 0.0, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moolaStart.dignityType)
        assertTrue(moolaStart.isMoolatrikona)

        val moolaInside = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 4, 19.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moolaInside.dignityType)

        // Leo [20, 30) is Own Sign
        val ownSign = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 4, 20.0, signMap)
        assertEquals(DignityType.OWN_SIGN, ownSign.dignityType)
        assertTrue(ownSign.isOwnSign)
    }

    @Test
    fun testMoonTaurusBoundaries() {
        val signMap = mapOf(BodyId.MOON to 1)
        // Taurus [0, 3) is Exaltation
        val exalt = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 1, 2.999, signMap)
        assertEquals(DignityType.EXALTATION, exalt.dignityType)

        // Taurus [3, 30) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 1, 3.0, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        val moolaEnd = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 1, 29.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moolaEnd.dignityType)
    }

    @Test
    fun testMarsAriesBoundaries() {
        val signMap = mapOf(BodyId.MARS to 0)
        // Aries [0, 12) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.MARS, 0, 11.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        // Aries [12, 30) is Own Sign
        val own = PlanetaryDignityCalculator.evaluateDignity(BodyId.MARS, 0, 12.0, signMap)
        assertEquals(DignityType.OWN_SIGN, own.dignityType)
    }

    @Test
    fun testMercuryVirgoBoundaries() {
        val signMap = mapOf(BodyId.MERCURY to 5)
        // Virgo [0, 15) is Exaltation
        val exalt = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 5, 14.999, signMap)
        assertEquals(DignityType.EXALTATION, exalt.dignityType)

        // Virgo [15, 20) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 5, 15.0, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        val moolaEnd = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 5, 19.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moolaEnd.dignityType)

        // Virgo [20, 30) is Own Sign
        val own = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 5, 20.0, signMap)
        assertEquals(DignityType.OWN_SIGN, own.dignityType)
    }

    @Test
    fun testJupiterSagittariusBoundaries() {
        val signMap = mapOf(BodyId.JUPITER to 8)
        // Sagittarius [0, 10) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.JUPITER, 8, 9.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        // Sagittarius [10, 30) is Own Sign
        val own = PlanetaryDignityCalculator.evaluateDignity(BodyId.JUPITER, 8, 10.0, signMap)
        assertEquals(DignityType.OWN_SIGN, own.dignityType)
    }

    @Test
    fun testVenusLibraBoundaries() {
        val signMap = mapOf(BodyId.VENUS to 6)
        // Libra [0, 15) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.VENUS, 6, 14.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        // Libra [15, 30) is Own Sign
        val own = PlanetaryDignityCalculator.evaluateDignity(BodyId.VENUS, 6, 15.0, signMap)
        assertEquals(DignityType.OWN_SIGN, own.dignityType)
    }

    @Test
    fun testSaturnAquariusBoundaries() {
        val signMap = mapOf(BodyId.SATURN to 10)
        // Aquarius [0, 20) is Moolatrikona
        val moola = PlanetaryDignityCalculator.evaluateDignity(BodyId.SATURN, 10, 19.999, signMap)
        assertEquals(DignityType.MOOLATRIKONA, moola.dignityType)

        // Aquarius [20, 30) is Own Sign
        val own = PlanetaryDignityCalculator.evaluateDignity(BodyId.SATURN, 10, 20.0, signMap)
        assertEquals(DignityType.OWN_SIGN, own.dignityType)
    }

    // ==========================================
    // 4. Own Signs (ASTRO-R25)
    // ==========================================

    @Test
    fun testOwnSignsPure() {
        // Mars in Scorpio
        val marsScorpio = PlanetaryDignityCalculator.evaluateDignity(BodyId.MARS, 7, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, marsScorpio.dignityType)

        // Jupiter in Pisces
        val jupPisces = PlanetaryDignityCalculator.evaluateDignity(BodyId.JUPITER, 11, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, jupPisces.dignityType)

        // Saturn in Capricorn
        val satCap = PlanetaryDignityCalculator.evaluateDignity(BodyId.SATURN, 9, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, satCap.dignityType)

        // Venus in Taurus
        val venTaurus = PlanetaryDignityCalculator.evaluateDignity(BodyId.VENUS, 1, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, venTaurus.dignityType)

        // Mercury in Gemini
        val mercGemini = PlanetaryDignityCalculator.evaluateDignity(BodyId.MERCURY, 2, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, mercGemini.dignityType)

        // Moon in Cancer
        val moonCancer = PlanetaryDignityCalculator.evaluateDignity(BodyId.MOON, 3, 15.0, emptyMap())
        assertEquals(DignityType.OWN_SIGN, moonCancer.dignityType)
    }

    // ==========================================
    // 5. Natural Relationships (ASTRO-R28)
    // ==========================================

    @Test
    fun testNaisargikaMaitriMatrix() {
        // Sun
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.MOON))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.MARS))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.JUPITER))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.VENUS))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.SATURN))

        // Moon (has no enemies)
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.SUN))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.MARS))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.JUPITER))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.VENUS))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MOON, BodyId.SATURN))

        // Directional asymmetry: Moon sees Mercury as Friend, Mercury sees Moon as Enemy!
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MERCURY, BodyId.MOON))

        // Directional asymmetry: Sun sees Mercury as Neutral, Mercury sees Sun as Friend!
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MERCURY, BodyId.SUN))

        // Mars
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.SUN))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.MOON))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.JUPITER))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.VENUS))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.MARS, BodyId.SATURN))

        // Jupiter
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.SUN))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.MOON))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.MARS))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.VENUS))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.JUPITER, BodyId.SATURN))

        // Venus
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.SUN))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.MOON))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.MARS))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.JUPITER))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.VENUS, BodyId.SATURN))

        // Saturn
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.SUN))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.MOON))
        assertEquals(NaturalRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.MARS))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.MERCURY))
        assertEquals(NaturalRelationshipType.NEUTRAL, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.JUPITER))
        assertEquals(NaturalRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SATURN, BodyId.VENUS))
    }

    // ==========================================
    // 6. Temporary Relationships (ASTRO-R29)
    // ==========================================

    @Test
    fun testTatkalikaMaitriDistanceAndWraparound() {
        // Distance 1 (same sign) -> Enemy
        assertEquals(1, PlanetaryRelationshipCalculator.calculateRelativeHouseDistance(0, 0))
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 0))

        // Distance 2 (Aries to Taurus) -> Friend
        assertEquals(2, PlanetaryRelationshipCalculator.calculateRelativeHouseDistance(0, 1))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 1))

        // Distance 3, 4 -> Friend
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 2))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 3))

        // Distance 5, 6, 7, 8, 9 -> Enemy
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 4))
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 5))
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 6))
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 7))
        assertEquals(TemporaryRelationshipType.ENEMY, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 8))

        // Distance 10, 11, 12 -> Friend
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 9))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 10))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 11))

        // Wraparound: Pisces (11) to Aries (0) is distance 2 (Friend)
        assertEquals(2, PlanetaryRelationshipCalculator.calculateRelativeHouseDistance(11, 0))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 11, 0))

        // Wraparound: Aries (0) to Pisces (11) is distance 12 (Friend)
        assertEquals(12, PlanetaryRelationshipCalculator.calculateRelativeHouseDistance(0, 11))
        assertEquals(TemporaryRelationshipType.FRIEND, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.SUN, BodyId.MOON, 0, 11))
    }

    // ==========================================
    // 7. Compound Relationships (ASTRO-R30)
    // ==========================================

    @Test
    fun testPanchadhaMaitriCombinations() {
        // Friend + Friend = Great Friend (+2)
        assertEquals(
            CompoundRelationshipType.GREAT_FRIEND,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.FRIEND, TemporaryRelationshipType.FRIEND),
        )

        // Friend + Enemy = Neutral (0)
        assertEquals(
            CompoundRelationshipType.NEUTRAL,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.FRIEND, TemporaryRelationshipType.ENEMY),
        )

        // Neutral + Friend = Friend (+1)
        assertEquals(
            CompoundRelationshipType.FRIEND,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.NEUTRAL, TemporaryRelationshipType.FRIEND),
        )

        // Neutral + Enemy = Enemy (-1)
        assertEquals(
            CompoundRelationshipType.ENEMY,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.NEUTRAL, TemporaryRelationshipType.ENEMY),
        )

        // Enemy + Friend = Neutral (0)
        assertEquals(
            CompoundRelationshipType.NEUTRAL,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.ENEMY, TemporaryRelationshipType.FRIEND),
        )

        // Enemy + Enemy = Great Enemy (-2)
        assertEquals(
            CompoundRelationshipType.GREAT_ENEMY,
            PlanetaryRelationshipCalculator.getCompoundRelationship(NaturalRelationshipType.ENEMY, TemporaryRelationshipType.ENEMY),
        )
    }

    // ==========================================
    // 8. Residential Dignity to Sign Lord (ASTRO-R31)
    // ==========================================

    @Test
    fun testResidentialCompoundSignDignity() {
        // Sun in Gemini (Sign 2, Lord Mercury).
        // Sun to Mercury: Natural Neutral.
        // If Mercury in Taurus (Sign 1): Distance 12 (Temporary Friend).
        // Neutral + Friend = Compound Friend -> FRIEND_SIGN.
        val signMapFriend = mapOf(BodyId.SUN to 2, BodyId.MERCURY to 1)
        val dignityFriend = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 2, 10.0, signMapFriend)
        assertEquals(DignityType.FRIEND_SIGN, dignityFriend.dignityType)

        // If Mercury in Gemini (Sign 2): Same sign, Distance 1 (Temporary Enemy).
        // Neutral + Enemy = Compound Enemy -> ENEMY_SIGN.
        val signMapEnemy = mapOf(BodyId.SUN to 2, BodyId.MERCURY to 2)
        val dignityEnemy = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 2, 10.0, signMapEnemy)
        assertEquals(DignityType.ENEMY_SIGN, dignityEnemy.dignityType)

        // Sun in Aquarius (Sign 10, Lord Saturn).
        // Sun to Saturn: Natural Enemy.
        // If Saturn in Aquarius (Sign 10): Distance 1 (Temporary Enemy).
        // Enemy + Enemy = Great Enemy -> GREAT_ENEMY_SIGN.
        val signMapGreatEnemy = mapOf(BodyId.SUN to 10, BodyId.SATURN to 10)
        val dignityGreatEnemy = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 10, 10.0, signMapGreatEnemy)
        assertEquals(DignityType.GREAT_ENEMY_SIGN, dignityGreatEnemy.dignityType)

        // Sun in Sagittarius (Sign 8, Lord Jupiter).
        // Sun to Jupiter: Natural Friend.
        // If Jupiter in Capricorn (Sign 9): Distance 2 (Temporary Friend).
        // Friend + Friend = Great Friend -> GREAT_FRIEND_SIGN.
        val signMapGreatFriend = mapOf(BodyId.SUN to 8, BodyId.JUPITER to 9)
        val dignityGreatFriend = PlanetaryDignityCalculator.evaluateDignity(BodyId.SUN, 8, 10.0, signMapGreatFriend)
        assertEquals(DignityType.GREAT_FRIEND_SIGN, dignityGreatFriend.dignityType)
    }

    // ==========================================
    // 9. Rahu and Ketu Handling
    // ==========================================

    @Test
    fun testRahuKetuNotApplicable() {
        val signMap = mapOf(BodyId.RAHU to 1, BodyId.KETU to 7, BodyId.SUN to 0)

        val rahuDignity = PlanetaryDignityCalculator.evaluateDignity(BodyId.RAHU, 1, 10.0, signMap)
        assertEquals(DignityType.NOT_APPLICABLE, rahuDignity.dignityType)
        assertFalse(rahuDignity.isExalted)
        assertFalse(rahuDignity.isDebilitated)

        val ketuDignity = PlanetaryDignityCalculator.evaluateDignity(BodyId.KETU, 7, 10.0, signMap)
        assertEquals(DignityType.NOT_APPLICABLE, ketuDignity.dignityType)

        assertEquals(NaturalRelationshipType.NOT_APPLICABLE, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.RAHU, BodyId.SUN))
        assertEquals(NaturalRelationshipType.NOT_APPLICABLE, PlanetaryRelationshipCalculator.getNaturalRelationship(BodyId.SUN, BodyId.KETU))
        assertEquals(TemporaryRelationshipType.NOT_APPLICABLE, PlanetaryRelationshipCalculator.getTemporaryRelationship(BodyId.RAHU, BodyId.SUN, 1, 0))
    }

    // ==========================================
    // 10. Varga Evaluation Integration
    // ==========================================

    @Test
    fun testVargaDignityEvaluation() {
        // Navamsa (D9) positions: Sun in Aries (resultingRashiIndex = 0), Mars in Leo (resultingRashiIndex = 4)
        val vargaPositions = listOf(
            VargaPosition(
                bodyId = BodyId.SUN,
                isLagna = false,
                sourceLongitude = 10.0,
                sourceRashiIndex = 0,
                divisionIndex = 1,
                resultingRashiIndex = 0, // Aries
                degreeInResultingRashi = 10.0,
                resultingLongitude = 10.0,
            ),
            VargaPosition(
                bodyId = BodyId.MARS,
                isLagna = false,
                sourceLongitude = 120.0,
                sourceRashiIndex = 4,
                divisionIndex = 1,
                resultingRashiIndex = 4, // Leo
                degreeInResultingRashi = 10.0,
                resultingLongitude = 130.0,
            ),
        )

        val dignities = PlanetaryDignityCalculator.calculateDignitiesFromVargaPositions(vargaPositions, DivisionalChart.D9)
        assertEquals(2, dignities.size)

        val sunDignity = dignities.first { it.bodyId == BodyId.SUN }
        assertEquals(DivisionalChart.D9, sunDignity.chart)
        assertEquals(0, sunDignity.sourceRashiIndex)
        assertEquals(DignityType.EXALTATION, sunDignity.dignityType)
        assertTrue(sunDignity.isExalted)
    }

    // ==========================================
    // 11. Determinism and Repeatability
    // ==========================================

    @Test
    fun testDeterminismAndRepeatability() {
        val bodies = listOf(
            dummyBody(BodyId.SUN, 0, 10.0),
            dummyBody(BodyId.MOON, 1, 2.0),
            dummyBody(BodyId.MARS, 9, 28.0),
            dummyBody(BodyId.MERCURY, 5, 14.0),
            dummyBody(BodyId.JUPITER, 3, 5.0),
            dummyBody(BodyId.VENUS, 11, 27.0),
            dummyBody(BodyId.SATURN, 6, 20.0),
        )

        val run1 = PlanetaryDignityCalculator.calculateDignities(bodies)
        val run2 = PlanetaryDignityCalculator.calculateDignities(bodies)

        assertEquals(run1.size, run2.size)
        for (i in run1.indices) {
            assertEquals(run1[i].bodyId, run2[i].bodyId)
            assertEquals(run1[i].dignityType, run2[i].dignityType)
            assertEquals(run1[i].deepExaltationDegree, run2[i].deepExaltationDegree)
            assertEquals(run1[i].deepDebilitationDegree, run2[i].deepDebilitationDegree)
        }
    }
}
