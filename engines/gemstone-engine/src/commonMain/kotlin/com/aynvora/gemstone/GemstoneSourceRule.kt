package com.aynvora.gemstone

import com.aynvora.contracts.CelestialBody
import kotlinx.serialization.Serializable

/**
 * Result classification of a gemstone compatibility evaluation.
 */
@Serializable
enum class GemstoneCompatibilityStatus {
    COMPATIBLE,
    CAUTION,
    CONFLICT,
    INSUFFICIENT_DATA,
}

/**
 * Type of rule governing a gemstone evaluation.
 */
@Serializable
enum class GemstoneRuleType {
    PLANETARY_ENMITY_CONFLICT,
    FUNCTIONAL_MALEFIC_CONTRAINDICATION,
    DUSTHANA_LORD_WARNING,
    TRIKONA_BENEFIC_SUPPORT,
    MUTUAL_ASPECT_AFFLICTION,
    DEBILITATION_CAUTION,
}

/**
 * Concrete rule object representing classical Jyotisha gemstone principles.
 */
@Serializable
data class GemstoneRule(
    val ruleId: String,
    val ruleType: GemstoneRuleType,
    val primaryGemstone: GemstoneType,
    val secondaryGemstone: GemstoneType? = null,
    val conditionDescription: String,
    val effectStatus: GemstoneCompatibilityStatus,
    val rationale: String,
    val sourceCitation: GemstoneSourceCitation,
)

/**
 * Authoritative registry of gemstone conflict and compatibility rules.
 */
object GemstoneRuleRegistry {

    private val BPHS_GRAHA_SAMBANDHA = GemstoneSourceCitation(
        sourceId = "bphs_graha_sambandha",
        title = "Brihat Parashara Hora Shastra",
        chapterOrSection = "Adhyaya 3, Graha Sambandha",
        quoteOrSummary = "Sun's enemies are Saturn and Venus. Moon's enemies are Rahu and Ketu. Mars' enemies are Mercury and Saturn. Mercury's enemy is Moon. Jupiter's enemies are Mercury and Venus. Venus' enemies are Sun and Moon. Saturn's enemies are Sun, Moon, and Mars.",
    )

    private val BPHS_BHAVA_LORDS = GemstoneSourceCitation(
        sourceId = "bphs_bhava_phala",
        title = "Brihat Parashara Hora Shastra",
        chapterOrSection = "Adhyaya 34, Yogakaraka and Bhava Lords",
        quoteOrSummary = "The lords of the 5th and 9th are always auspicious (Trikona). The lords of the 6th, 8th, and 12th produce impediments and distress (Trika/Dusthana).",
    )

    private val PHALADEEPIKA_RATNA = GemstoneSourceCitation(
        sourceId = "phaladeepika_adhyaya_2",
        title = "Phaladeepika",
        chapterOrSection = "Adhyaya 2, Sloka 21-22",
        quoteOrSummary = "Gems belonging to mutually inimical grahas should never be combined on the same body, as their conflicting energies create disharmony.",
    )

    /**
     * Major classical gemstone conflict rules (incompatible pairs).
     */
    val INCOMPATIBLE_PAIRS: List<GemstoneRule> = listOf(
        // 1. Ruby + Blue Sapphire (Sun vs Saturn)
        GemstoneRule(
            ruleId = "conflict_sun_saturn",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.RUBY,
            secondaryGemstone = GemstoneType.BLUE_SAPPHIRE,
            conditionDescription = "Ruby (Sun) and Blue Sapphire (Saturn) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Classical Surya-Shani deep mutual enmity. Wearing Manikya with Neelam produces intense energetic conflict between royal self-expression and ascetic limitation.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
        // 2. Ruby + Diamond (Sun vs Venus)
        GemstoneRule(
            ruleId = "conflict_sun_venus",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.RUBY,
            secondaryGemstone = GemstoneType.DIAMOND,
            conditionDescription = "Ruby (Sun) and Diamond (Venus) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Surya and Shukra are natural enemies. Solar austerity clashes with Venusian material indulgence and artistic luxury.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
        // 3. Ruby + Hessonite (Sun vs Rahu)
        GemstoneRule(
            ruleId = "conflict_sun_rahu",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.RUBY,
            secondaryGemstone = GemstoneType.HESSONITE,
            conditionDescription = "Ruby (Sun) and Hessonite (Rahu) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Rahu eclipses the Sun (Surya Grahana). Combining Manikya and Gomed creates internal turbulence, identity confusion, and restlessness.",
            sourceCitation = PHALADEEPIKA_RATNA,
        ),
        // 4. Pearl + Blue Sapphire (Moon vs Saturn - Vish Yoga)
        GemstoneRule(
            ruleId = "conflict_moon_saturn",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.NATURAL_PEARL,
            secondaryGemstone = GemstoneType.BLUE_SAPPHIRE,
            conditionDescription = "Natural Pearl (Moon) and Blue Sapphire (Saturn) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Chandra-Shani combination produces classical Vish Yoga (poison combination), leading to melancholy, emotional heaviness, and depressive tendencies.",
            sourceCitation = PHALADEEPIKA_RATNA,
        ),
        // 5. Pearl + Hessonite (Moon vs Rahu - Grahana Yoga)
        GemstoneRule(
            ruleId = "conflict_moon_rahu",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.NATURAL_PEARL,
            secondaryGemstone = GemstoneType.HESSONITE,
            conditionDescription = "Natural Pearl (Moon) and Hessonite (Rahu) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Rahu causes Lunar Eclipse (Chandra Grahana). Wearing Mukta with Gomed leads to severe anxiety, emotional instability, and phobias.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
        // 6. Pearl + Cat's Eye (Moon vs Ketu)
        GemstoneRule(
            ruleId = "conflict_moon_ketu",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.NATURAL_PEARL,
            secondaryGemstone = GemstoneType.CATS_EYE,
            conditionDescription = "Natural Pearl (Moon) and Cat's Eye (Ketu) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Ketu dissolves lunar emotional grounding. Mukta paired with Vaidurya creates emotional isolation and detachment.",
            sourceCitation = PHALADEEPIKA_RATNA,
        ),
        // 7. Red Coral + Emerald (Mars vs Mercury)
        GemstoneRule(
            ruleId = "conflict_mars_mercury",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.RED_CORAL,
            secondaryGemstone = GemstoneType.EMERALD,
            conditionDescription = "Red Coral (Mars) and Emerald (Mercury) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Mangala and Budha have sharp planetary enmity. Mars acts with impulse and fire; Mercury acts with intellect and coolness. Combining them impairs calm decision-making.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
        // 8. Red Coral + Blue Sapphire (Mars vs Saturn)
        GemstoneRule(
            ruleId = "conflict_mars_saturn",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.RED_CORAL,
            secondaryGemstone = GemstoneType.BLUE_SAPPHIRE,
            conditionDescription = "Red Coral (Mars) and Blue Sapphire (Saturn) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Classical Fire (Mars) and Ice/Air (Saturn) confrontation. Wearing Moonga with Neelam can provoke accidents, frustration, and blocked energy.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
        // 9. Yellow Sapphire + Diamond (Jupiter vs Venus)
        GemstoneRule(
            ruleId = "conflict_jupiter_venus",
            ruleType = GemstoneRuleType.PLANETARY_ENMITY_CONFLICT,
            primaryGemstone = GemstoneType.YELLOW_SAPPHIRE,
            secondaryGemstone = GemstoneType.DIAMOND,
            conditionDescription = "Yellow Sapphire (Jupiter) and Diamond (Venus) worn simultaneously",
            effectStatus = GemstoneCompatibilityStatus.CONFLICT,
            rationale = "Brihaspati (Deva Guru) and Shukracharya (Asura Guru) are rival preceptors. Wearing Pukhraj and Heera together creates ethical and ideological dilemmas in life direction.",
            sourceCitation = BPHS_GRAHA_SAMBANDHA,
        ),
    )

    /**
     * Checks if two gemstones have an explicit classical conflict rule.
     */
    fun findConflict(gem1: GemstoneType, gem2: GemstoneType): GemstoneRule? {
        if (gem1 == gem2) return null
        return INCOMPATIBLE_PAIRS.firstOrNull { rule ->
            (rule.primaryGemstone == gem1 && rule.secondaryGemstone == gem2) ||
                    (rule.primaryGemstone == gem2 && rule.secondaryGemstone == gem1)
        }
    }
}
