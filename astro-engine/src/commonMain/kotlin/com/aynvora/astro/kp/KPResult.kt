package com.aynvora.astro.kp

import kotlinx.serialization.Serializable

@Serializable
data class KPCuspPosition(
    val houseNumber: Int, // 1 to 12
    val longitude: Double,
    val formattedLongitude: String,
    val signName: String,
    val signLord: String,
    val starName: String,
    val starLord: String,
    val subLord: String,
    val subSubLord: String,
    val kpNumber: Int, // 1 to 249
)

@Serializable
data class KPPlanetPosition(
    val planetName: String,
    val longitude: Double,
    val formattedLongitude: String,
    val houseNumber: Int,
    val signName: String,
    val signLord: String,
    val starName: String,
    val starLord: String,
    val subLord: String,
    val subSubLord: String,
    val kpNumber: Int,
    val isRetrograde: Boolean = false,
)

@Serializable
data class KPRulingPlanets(
    val ascendantSignLord: String,
    val ascendantStarLord: String,
    val ascendantSubLord: String,
    val moonSignLord: String,
    val moonStarLord: String,
    val moonSubLord: String,
    val dayLord: String, // Vara Lord
    val orderedSignificators: List<String>,
)

@Serializable
data class KPHouseSignificators(
    val houseNumber: Int,
    val levelA: List<String>, // Planets in the star of occupants
    val levelB: List<String>, // Occupants of the house
    val levelC: List<String>, // Planets in the star of the house lord
    val levelD: List<String>, // House lord
)

@Serializable
data class KPResult(
    val ayanamshaName: String = "KP_ORIGINAL",
    val ayanamshaDegrees: Double,
    val cusps: List<KPCuspPosition>,
    val planets: List<KPPlanetPosition>,
    val rulingPlanets: KPRulingPlanets,
    val houseSignificators: List<KPHouseSignificators>,
    val calculationProfile: String = "KP_CLASSICAL_PLACIDUS_249",
    val status: String = "PRODUCTION_VERIFIED",
)
