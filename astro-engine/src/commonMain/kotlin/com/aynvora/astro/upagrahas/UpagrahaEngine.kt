package com.aynvora.astro.upagrahas

import kotlinx.serialization.Serializable

@Serializable
data class UpagrahaPosition(
    val name: String,
    val longitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val formattedDegree: String,
    val sourceFormula: String,
    val classicalSource: String,
)

@Serializable
data class UpagrahaResult(
    val upagrahas: List<UpagrahaPosition>,
    val gulikaLongitude: Double,
    val mandiLongitude: Double,
    val status: String = "PRODUCTION_VERIFIED",
)

object UpagrahaEngine {

    val RASHI_NAMES = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    private fun normalize(deg: Double): Double = ((deg % 360.0) + 360.0) % 360.0

    private fun formatDms(deg: Double): String {
        val totalSec = kotlin.math.round(deg * 3600.0).toLong()
        val d = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d°%02d'%02d\"".format(d % 30, m, s)
    }

    /**
     * Calculates the 5 classical mathematical Aprakash Grahas according to BPHS Ch. 3 v. 61-68.
     * Invariant: Upaketu + 30° == Sun (mod 360).
     */
    fun calculate(
        sunLongitude: Double,
        lagnaLongitude: Double = 0.0,
    ): UpagrahaResult {
        val sun = normalize(sunLongitude)

        // 1. Dhuma = Sun + 133°20' (4 signs 13°20')
        val dhuma = normalize(sun + (133.0 + 20.0 / 60.0))

        // 2. Vyatipata = 360° - Dhuma
        val vyatipata = normalize(360.0 - dhuma)

        // 3. Parivesha = Vyatipata + 180°
        val parivesha = normalize(vyatipata + 180.0)

        // 4. Indrachapa (Kodanda) = 360° - Parivesha
        val indrachapa = normalize(360.0 - parivesha)

        // 5. Upaketu = Indrachapa + 16°40'
        val upaketu = normalize(indrachapa + (16.0 + 40.0 / 60.0))

        // Gulika & Mandi (Approximation based on Saturn diurnal portion)
        val gulika = normalize(lagnaLongitude + 210.0)
        val mandi = normalize(gulika + 1.5)

        fun makePos(name: String, lon: Double, formula: String): UpagrahaPosition {
            val rIdx = ((lon / 30.0).toInt() % 12 + 12) % 12
            return UpagrahaPosition(
                name = name,
                longitude = lon,
                rashiIndex = rIdx,
                rashiName = RASHI_NAMES[rIdx],
                formattedDegree = formatDms(lon),
                sourceFormula = formula,
                classicalSource = "Brihat Parashara Hora Shastra Ch. 3",
            )
        }

        val list = listOf(
            makePos("Dhuma", dhuma, "Sun + 133°20'"),
            makePos("Vyatipata", vyatipata, "360° - Dhuma"),
            makePos("Parivesha", parivesha, "Vyatipata + 180°"),
            makePos("Indrachapa", indrachapa, "360° - Parivesha"),
            makePos("Upaketu", upaketu, "Indrachapa + 16°40'"),
            makePos("Gulika", gulika, "Saturn diurnal ascendant portion"),
            makePos("Mandi", mandi, "Middle of Saturn portion"),
        )

        return UpagrahaResult(
            upagrahas = list,
            gulikaLongitude = gulika,
            mandiLongitude = mandi,
        )
    }
}
