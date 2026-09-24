package com.aynvora.astro

import com.aynvora.astro.ashtakavarga.AshtakavargaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaShodhanaCalculator
import com.aynvora.astro.ashtakavarga.ShodhitaAshtakavargaResult
import com.aynvora.astro.aspects.AspectCalculator
import com.aynvora.astro.aspects.AspectPosition
import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.dignity.PlanetaryDignityCalculator
import com.aynvora.astro.dignity.PlanetaryDignityPosition
import com.aynvora.astro.houses.HouseCalculationInput
import com.aynvora.astro.houses.HousePosition
import com.aynvora.astro.houses.HouseSystemRegistry
import com.aynvora.astro.lagna.LagnaCalculator
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.PlanetaryCalculator
import com.aynvora.astro.planets.PlanetaryCalculator.Planet
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipPosition
import com.aynvora.astro.shadbala.PlanetaryShadbalaPosition
import com.aynvora.astro.shadbala.ShadbalaCalculator
import com.aynvora.astro.states.PlanetStateCalculator
import com.aynvora.astro.states.PlanetStatePosition
import com.aynvora.astro.time.TimeNormalizer
import com.aynvora.astro.varga.DefaultVargaEngine
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import com.aynvora.astro.varga.VargaEngine
import com.aynvora.astro.varga.VargaProfile
import com.aynvora.astro.zodiac.ZodiacCalculator
import kotlinx.serialization.Serializable

@Serializable
enum class BodyId {
    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    RAHU,
    KETU,
}

@Serializable
data class BodyPosition(
    val bodyId: BodyId,
    val tropicalLongitude: Double,
    val siderealLongitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInRashi: Double,
    val nakshatraIndex: Int,
    val nakshatraName: String,
    val degreeInNakshatra: Double,
    val pada: Int,
    val isRetrograde: Boolean,
    val dailyMotionDegrees: Double,
)

@Serializable
data class EngineCalculationConfig(
    val ayanamsa: String = "LAHIRI_CHITRAPAKSHA",
    val houseSystem: String = "EQUAL_HOUSE",
    val profile: String = "STANDARD_VEDIC",
    val vargaRulesetId: String = "PARASHARA_CLASSICAL_V1",
    val requestedDivisionalCharts: Set<DivisionalChart> = emptySet(),
    val ashtakavargaRulesetId: String = "PARASHARA_CLASSICAL_V1",
)

@Serializable
data class BirthData(
    val dateTimeIso: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null,
)

@Serializable
data class CalculationResult(
    val engineVersion: String,
    val status: String,
    val calculationModel: String = "MEEUS_VSOP87",
    val julianDay: Double = 0.0,
    val julianCenturies: Double = 0.0,
    val ayanamsaDegrees: Double = 0.0,
    val ayanamsaName: String = "LAHIRI_CHITRAPAKSHA",
    val lagna: LagnaPosition? = null,
    val houses: List<HousePosition> = emptyList(),
    val planetHouseOccupancy: Map<BodyId, Int> = emptyMap(),
    val aspects: List<AspectPosition> = emptyList(),
    val planetStates: List<PlanetStatePosition> = emptyList(),
    val positions: List<BodyPosition> = emptyList(),
    val divisionalCharts: Map<DivisionalChart, VargaChartResult> = emptyMap(),
    val planetaryDignities: List<PlanetaryDignityPosition> = emptyList(),
    val planetaryRelationships: List<PlanetaryRelationshipPosition> = emptyList(),
    val shadbala: List<PlanetaryShadbalaPosition> = emptyList(),
    val ashtakavarga: AshtakavargaResult? = null,
    val shodhitaAshtakavarga: ShodhitaAshtakavargaResult? = null,
    val ashtakavargaPinda: AshtakavargaPindaResult? = null,
)

interface AstroEngine {
    suspend fun calculate(
        birthData: BirthData,
        config: EngineCalculationConfig = EngineCalculationConfig(),
    ): CalculationResult
}

class AynvoraAstroEngine(
    private val vargaEngine: VargaEngine = DefaultVargaEngine(),
) : AstroEngine {

    override suspend fun calculate(
        birthData: BirthData,
        config: EngineCalculationConfig,
    ): CalculationResult {
        // Resolve date-time components
        val y: Int
        val m: Int
        val d: Int
        val h: Int
        val min: Int
        val s: Int

        if (birthData.year != null && birthData.month != null && birthData.day != null &&
            birthData.hour != null && birthData.minute != null
        ) {
            y = birthData.year
            m = birthData.month
            d = birthData.day
            h = birthData.hour
            min = birthData.minute
            s = birthData.second ?: 0
        } else {
            // Parse ISO format YYYY-MM-DDTHH:MM:SS
            val parts = birthData.dateTimeIso.split("T")
            require(parts.size == 2) { "Invalid dateTimeIso format: ${birthData.dateTimeIso}" }
            val dateParts = parts[0].split("-").map { it.toInt() }
            val cleanTime = parts[1].removeSuffix("Z").split("+")[0].split("-")[0]
            val timeParts = cleanTime.split(":").map { it.toInt() }

            y = dateParts[0]
            m = dateParts[1]
            d = dateParts[2]
            h = timeParts[0]
            min = timeParts[1]
            s = if (timeParts.size > 2) timeParts[2] else 0
        }

        // Time normalization to UTC and Julian Day
        val normalizedTime = TimeNormalizer.normalize(
            year = y,
            month = m,
            day = d,
            hour = h,
            minute = min,
            second = s,
            timezoneId = birthData.timeZoneId,
        )
        val jd = normalizedTime.julianDay

        // Ayanamsa calculation
        val ayanamsaCalculator = AyanamsaCalculator.forConvention(config.ayanamsa)
        val ayanamsaDegrees = ayanamsaCalculator.calculate(jd)

        // Celestial body calculations
        val positions = mutableListOf<BodyPosition>()

        // 1. Sun
        val sun = SunCalculator.calculate(jd)
        positions.add(createBodyPosition(BodyId.SUN, sun.apparentLongitude, ayanamsaDegrees, false, sun.dailyMotionDegrees))

        // 2. Moon
        val moon = MoonCalculator.calculate(jd)
        positions.add(createBodyPosition(BodyId.MOON, moon.apparentLongitude, ayanamsaDegrees, false, moon.dailyMotionDegrees))

        // 3. Mercury
        val mercury = PlanetaryCalculator.calculate(Planet.MERCURY, jd)
        positions.add(createBodyPosition(BodyId.MERCURY, mercury.apparentLongitude, ayanamsaDegrees, mercury.isRetrograde, mercury.dailyMotionDegrees))

        // 4. Venus
        val venus = PlanetaryCalculator.calculate(Planet.VENUS, jd)
        positions.add(createBodyPosition(BodyId.VENUS, venus.apparentLongitude, ayanamsaDegrees, venus.isRetrograde, venus.dailyMotionDegrees))

        // 5. Mars
        val mars = PlanetaryCalculator.calculate(Planet.MARS, jd)
        positions.add(createBodyPosition(BodyId.MARS, mars.apparentLongitude, ayanamsaDegrees, mars.isRetrograde, mars.dailyMotionDegrees))

        // 6. Jupiter
        val jupiter = PlanetaryCalculator.calculate(Planet.JUPITER, jd)
        positions.add(createBodyPosition(BodyId.JUPITER, jupiter.apparentLongitude, ayanamsaDegrees, jupiter.isRetrograde, jupiter.dailyMotionDegrees))

        // 7. Saturn
        val saturn = PlanetaryCalculator.calculate(Planet.SATURN, jd)
        positions.add(createBodyPosition(BodyId.SATURN, saturn.apparentLongitude, ayanamsaDegrees, saturn.isRetrograde, saturn.dailyMotionDegrees))

        // 8. Rahu & 9. Ketu
        val nodes = LunarNodesCalculator.calculate(jd)
        positions.add(createBodyPosition(BodyId.RAHU, nodes.rahu.apparentLongitude, ayanamsaDegrees, true, nodes.rahu.dailyMotionDegrees))
        positions.add(createBodyPosition(BodyId.KETU, nodes.ketu.apparentLongitude, ayanamsaDegrees, true, nodes.ketu.dailyMotionDegrees))

        // Ascendant / Lagna calculation
        val lagna = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = birthData.latitude,
            longitudeDeg = birthData.longitude,
            ayanamsaDegrees = ayanamsaDegrees,
        )

        // House / Bhava calculation
        val planetLongitudes = positions.associate { it.bodyId to it.siderealLongitude }
        val houseCalculator = HouseSystemRegistry.forName(config.houseSystem)
        val houseResult = houseCalculator.calculate(
            HouseCalculationInput(
                lagna = lagna,
                latitudeDeg = birthData.latitude,
                longitudeDeg = birthData.longitude,
                ayanamsaDegrees = ayanamsaDegrees,
                planetPositions = planetLongitudes,
            ),
        )

        // Aspects & Conjunctions calculation
        val aspects = AspectCalculator.calculate(positions)

        // Planet states (combustion & motion states)
        val planetStates = PlanetStateCalculator.calculate(positions)

        // Divisional charts calculation
        val divisionalCharts = if (config.requestedDivisionalCharts.isNotEmpty()) {
            if (config.vargaRulesetId != VargaProfile.DEFAULT_RULESET_ID) {
                throw UnsupportedOperationException("Divisional chart ruleset '${config.vargaRulesetId}' is unsupported.")
            }
            val vargaProfile = VargaProfile(rulesetId = config.vargaRulesetId)
            vargaEngine.calculateMultiple(
                positions = positions,
                lagna = lagna,
                charts = config.requestedDivisionalCharts,
                profile = vargaProfile,
            )
        } else {
            emptyMap()
        }

        // Planetary dignities and relationships
        val planetaryDignities = PlanetaryDignityCalculator.calculateDignities(positions)
        val planetaryRelationships = PlanetaryRelationshipCalculator.calculateRelationships(positions)

        // Shadbala
        val shadbala = ShadbalaCalculator.calculateShadbala(
            positions = positions,
            lagnaLongitude = lagna.siderealLongitude,
            houseCusps = houseResult.houses.associate { it.houseNumber to it.cuspLongitude },
            planetHouseOccupancy = houseResult.planetHouseOccupancy,
            vargas = divisionalCharts,
            vargaEngine = vargaEngine,
            julianDay = jd.value,
            birthHour = birthData.hour ?: 12,
            obliquityDeg = lagna.obliquityDegrees,
        )

        val rawAshtakavarga = AshtakavargaCalculator.calculateAshtakavarga(
            positions = positions,
            lagna = lagna,
            rulesetId = config.ashtakavargaRulesetId,
        )

        val shodhitaAshtakavarga = AshtakavargaShodhanaCalculator.calculateShodhana(
            ashtakavargaResult = rawAshtakavarga,
            positions = positions,
        )

        val ashtakavargaPinda = AshtakavargaPindaCalculator.calculatePindas(
            shodhitaAshtakavarga = shodhitaAshtakavarga,
            positions = positions,
        )

        val ashtakavarga = rawAshtakavarga.copy(
            shodhana = shodhitaAshtakavarga,
            pinda = ashtakavargaPinda,
        )

        return CalculationResult(
            engineVersion = "0.3.0",
            status = "CALCULATED",
            calculationModel = "MEEUS_VSOP87",
            julianDay = jd.value,
            julianCenturies = jd.julianCenturiesJ2000,
            ayanamsaDegrees = ayanamsaDegrees,
            ayanamsaName = config.ayanamsa,
            lagna = lagna,
            houses = houseResult.houses,
            planetHouseOccupancy = houseResult.planetHouseOccupancy,
            aspects = aspects,
            planetStates = planetStates,
            positions = positions,
            divisionalCharts = divisionalCharts,
            planetaryDignities = planetaryDignities,
            planetaryRelationships = planetaryRelationships,
            shadbala = shadbala,
            ashtakavarga = ashtakavarga,
            shodhitaAshtakavarga = shodhitaAshtakavarga,
            ashtakavargaPinda = ashtakavargaPinda,
        )
    }

    companion object {
        internal fun createBodyPosition(
            bodyId: BodyId,
            tropicalLongitude: Double,
            ayanamsaDegrees: Double,
            isRetrograde: Boolean,
            dailyMotion: Double,
        ): BodyPosition {
            val sidereal = ZodiacCalculator.toSidereal(tropicalLongitude, ayanamsaDegrees)
            val rashi = ZodiacCalculator.calculateRashi(sidereal)
            val nakshatra = ZodiacCalculator.calculateNakshatra(sidereal)

            return BodyPosition(
                bodyId = bodyId,
                tropicalLongitude = tropicalLongitude,
                siderealLongitude = sidereal,
                rashiIndex = rashi.index,
                rashiName = rashi.name,
                degreeInRashi = rashi.degreeInRashi,
                nakshatraIndex = nakshatra.index,
                nakshatraName = nakshatra.name,
                degreeInNakshatra = nakshatra.degreeInNakshatra,
                pada = nakshatra.pada,
                isRetrograde = isRetrograde,
                dailyMotionDegrees = dailyMotion,
            )
        }
    }
}
