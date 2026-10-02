package com.aynvora.astro.compatibility

import kotlinx.serialization.Serializable

@Serializable
enum class CompatibilityTradition {
    NORTH_INDIAN_ASHTAKOOTA,
    SOUTH_INDIAN_DASA_PORUTHAM,
}

@Serializable
enum class CompatibilityRuleStatus {
    VERIFIED,
    PARTIAL,
    RESEARCH_ONLY,
}

@Serializable
data class CompatibilityRuleDefinition(
    val id: String,
    val name: String,
    val tradition: CompatibilityTradition,
    val maxWeight: Double,
    val formulaDescription: String,
    val sourceReference: String,
    val status: CompatibilityRuleStatus,
)

/**
 * Authoritative registry of all classical marital compatibility rules.
 * Keeps North Indian Ashtakoota (36 Guna) and South Indian Dasa Porutham (10 Poruthams) strictly separated.
 */
object CompatibilityRuleRegistry {

    val ASHTAKOOTA_RULES: List<CompatibilityRuleDefinition> = listOf(
        CompatibilityRuleDefinition(
            id = "AK_VARNA",
            name = "Varna",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 1.0,
            formulaDescription = "Groom Varna class rank >= Bride Varna class rank (Brahmin=0, Kshatriya=1, Vaishya=2, Shudra=3)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_VASHYA",
            name = "Vashya",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 2.0,
            formulaDescription = "Mutual harmony between Moon sign archetypes (Chatushpada, Manava, Jalachara, Vanchara, Keeta)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_TARA",
            name = "Tara (Dina)",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 3.0,
            formulaDescription = "Count from Bride star to Groom star mod 9 in auspicious tara categories (Sampat, Kshema, Sadhana, Mitra, Paramamitra)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_YONI",
            name = "Yoni",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 4.0,
            formulaDescription = "Instinctual animal archetype compatibility; non-enemy yoni pairs",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_GRAHA_MAITRI",
            name = "Graha Maitri",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 5.0,
            formulaDescription = "Friendship between natural lords of Bride and Groom Moon signs (Naisargika and Tatkalika)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_GANA",
            name = "Gana",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 6.0,
            formulaDescription = "Temperament alignment: Deva (divine), Manushya (human), Rakshasa (demonic). Deva-Deva=6, Rakshasa-Manushya=0",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_BHAKOOT",
            name = "Bhakoot (Rasi)",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 7.0,
            formulaDescription = "Relative sign distance. Inauspicious: 6/8 (Shadashtaka), 9/5 (Navapanchama for certain signs), 2/12 (Dvidvadasha)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "AK_NADI",
            name = "Nadi",
            tradition = CompatibilityTradition.NORTH_INDIAN_ASHTAKOOTA,
            maxWeight = 8.0,
            formulaDescription = "Physiological and genetic pulse: Adi (Vata), Madhya (Pitta), Antya (Kapha). Same nadi = 0 points (affliction)",
            sourceReference = "Brihat Parashara Hora Shastra Ch. 22",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
    )

    val DASA_PORUTHAM_RULES: List<CompatibilityRuleDefinition> = listOf(
        CompatibilityRuleDefinition(
            id = "PORUTHAM_DINA",
            name = "Dina Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Count from bride nakshatra to groom nakshatra; 2, 4, 6, 8, 9, 11, 13, 15, 18, 20, 24, 26 are auspicious",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_GANA",
            name = "Gana Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Temperament alignment (Deva, Manushya, Rakshasa); avoids Rakshasa bride with Manushya groom",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_MAHENDRA",
            name = "Mahendra Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Count from bride nakshatra to groom nakshatra is 4, 7, 10, 13, 16, 19, 22, or 25; promotes progeny and wealth",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_STREE_DEERGHA",
            name = "Stree Deergha Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Groom nakshatra is at least 9 (preferably > 13) nakshatras away from bride nakshatra",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_YONI",
            name = "Yoni Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Instinctual/biological affinity between animal archetypes; hostile animal pairs are rejected",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_RASI",
            name = "Rasi Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Harmonious distance between Moon rashis; 6/8 and 2/12 are rejected unless lords are friendly",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_RASIYATHIPATHI",
            name = "Rasiyathipathi Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Friendship between the governing lords of bride and groom Moon rashis",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_VASYA",
            name = "Vasya Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Mutual attraction between designated compatible sign pairs (e.g. Aries-Leo, Taurus-Cancer/Libra)",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_RAJJU",
            name = "Rajju Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Vital cord compatibility: Shiro (head), Kantha (neck), Udara (stomach), Kati (waist), Pada (foot). Same rajju is strongly inauspicious",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
        CompatibilityRuleDefinition(
            id = "PORUTHAM_VEDHA",
            name = "Vedha Porutham",
            tradition = CompatibilityTradition.SOUTH_INDIAN_DASA_PORUTHAM,
            maxWeight = 1.0,
            formulaDescription = "Incompatibility/antipathy pairs of nakshatras (Ashwini-Jyeshtha, Bharani-Anuradha, Krittika-Visakha, etc.)",
            sourceReference = "Kalaprakasika / Jataka Chandrika",
            status = CompatibilityRuleStatus.VERIFIED,
        ),
    )

    fun getAllRules(): List<CompatibilityRuleDefinition> = ASHTAKOOTA_RULES + DASA_PORUTHAM_RULES
}
