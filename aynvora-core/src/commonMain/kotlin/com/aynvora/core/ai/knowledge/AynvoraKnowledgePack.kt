package com.aynvora.core.ai.knowledge

import com.aynvora.core.ai.AynvoraInterpretationRecord
import com.aynvora.core.ai.AynvoraKnowledgeRule
import com.aynvora.core.ai.AynvoraSafetyConstraint
import com.aynvora.core.ai.AynvoraSourceReference
import com.aynvora.core.ai.AynvoraUserContext
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance

/**
 * Standard contract for feature-provided verified knowledge packs.
 *
 * Invariant: The on-device SLM runtime interacts with application intelligence
 * exclusively through [AynvoraKnowledgePack] and [EvidenceGraph].
 * The AI runtime must NEVER directly read arbitrary application databases or DAOs.
 */
interface AynvoraKnowledgePack {
    val featureId: CoreFeatureId
    val knowledgePackId: String
    val version: String
    val locale: String
    val sourceReferences: List<AynvoraSourceReference>
    val evidenceNodes: List<EvidenceItem>
    val interpretationRecords: List<AynvoraInterpretationRecord>
    val rules: List<AynvoraKnowledgeRule>
    val exclusions: List<String>
    val safetyConstraints: List<AynvoraSafetyConstraint>

    /**
     * Lightweight deterministic retrieval layer selecting only evidence relevant to the question.
     */
    fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext? = null
    ): List<EvidenceItem>

    /**
     * Retrieves domain interpretation records matching symbols or query context.
     */
    fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord>
}

/**
 * Verified Knowledge Pack for Vedic Astrology.
 */
class VedicAstrologyKnowledgePack(
    override val locale: String = "en",
    chartEvidence: List<EvidenceItem> = emptyList(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.ASTROLOGY
    override val knowledgePackId: String = "kp_astrology_vedic_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Brihat Parashara Hora Shastra",
            rulesetOrEdition = "Parashara Classical Samhita",
            authorOrTranslator = "Maharishi Parashara (Canonical Sanskrit)",
            provenancePolicy = "DETERMINISTIC_PARASHARA_CLASSICAL_V1",
        ),
        AynvoraSourceReference(
            sourceName = "Jaimini Sutras",
            rulesetOrEdition = "Upadesha Sutras",
            authorOrTranslator = "Maharishi Jaimini",
            provenancePolicy = "JAIMINI_CHARA_DASHA_V1",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = chartEvidence

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "astro_rec_dharma",
            domain = CoreFeatureId.ASTROLOGY,
            primarySymbolOrKey = "Dharma Trikona (Houses 1, 5, 9)",
            contextCondition = "Planets situated in trines",
            verifiedMeaning = "Signifies purpose, higher wisdom, righteous action, and innate spiritual trajectory.",
            sourceReference = sourceReferences.first(),
        ),
        AynvoraInterpretationRecord(
            recordId = "astro_rec_karma",
            domain = CoreFeatureId.ASTROLOGY,
            primarySymbolOrKey = "Karma Bhava (10th House)",
            contextCondition = "Midheaven planetary placements",
            verifiedMeaning = "Indicates professional exertion, public standing, societal responsibility, and conduct.",
            sourceReference = sourceReferences.first(),
        ),
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "rule_astro_lagna_primary",
            domain = CoreFeatureId.ASTROLOGY,
            description = "Ascendant (Lagna) forms the primary lens of physical vitality and personal orientation.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not predict exact lifespan, death date, or incurable terminal conditions.",
        "Do not manufacture planetary transits not computed by the ephemeris.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS,
        AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> {
        val qLower = question.lowercase()
        return if (evidenceNodes.isEmpty()) emptyList() else {
            evidenceNodes.filter { item ->
                when {
                    qLower.contains("career") || qLower.contains("job") || qLower.contains("work") ->
                        item.summary.contains("10th") || item.summary.contains("Karma") || item.summary.contains("Sun") || item.summary.contains("Saturn")
                    qLower.contains("mind") || qLower.contains("peace") || qLower.contains("emotion") ->
                        item.summary.contains("Moon") || item.summary.contains("4th")
                    else -> true
                }
            }.take(5)
        }
    }

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> {
        val qLower = question.lowercase()
        return interpretationRecords.filter {
            qLower.contains("career") || qLower.contains("purpose") || qLower.contains("dharma") || it.primarySymbolOrKey.lowercase().contains(qLower)
        }.ifEmpty { interpretationRecords.take(1) }
    }
}

/**
 * Sourced verse item bundled in [GitaKnowledgePack].
 */
data class GitaVerseEntry(
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val transliteration: String,
    val wordMeanings: String,
    val translation: String,
    val themes: List<String>,
    val traditionalCommentary: String,
    val author: String = "Swami Sivananda / Canonical Public Domain",
)

/**
 * Verified Knowledge Pack for Bhagavad Gita scripture.
 *
 * Implements Step 7: Sourced scripture, approved translations, Nishkama Karma evidence,
 * and lightweight retrieval so the entire scripture is never dumped into a single prompt.
 */
class GitaKnowledgePack(
    override val locale: String = "en",
    private val preloadedVerses: List<GitaVerseEntry> = defaultGitaVerses(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.GITA
    override val knowledgePackId: String = "kp_gita_canonical_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Srimad Bhagavad Gita",
            rulesetOrEdition = "Public Domain Gita Corpus (github.com/gita/gita)",
            canonicalReference = "Mahabharata Bhishma Parva Ch. 23-40",
            authorOrTranslator = "Canonical Sanskrit & Public Domain Translators",
            provenancePolicy = "SCRIPTURE_GROUNDED_VERIFIED_TEXT",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = preloadedVerses.map { verse ->
        EvidenceItem(
            evidenceId = "gita_${verse.chapter}_${verse.verse}",
            domain = CoreFeatureId.GITA,
            category = EvidenceCategory.TRADITIONAL_RULE,
            ruleId = "BG_${verse.chapter}_${verse.verse}",
            summary = "Gita ${verse.chapter}.${verse.verse}: ${verse.translation}",
            provenance = EvidenceProvenance(
                domain = CoreFeatureId.GITA,
                sourceName = "Srimad Bhagavad Gita",
                rulesetOrEdition = "Public Domain Gita Corpus",
                engineVersion = "1.0.0",
                timestampEpochMs = 1714000000000L,
                locale = locale,
                referenceId = "BG_${verse.chapter}_${verse.verse}",
            ),
            priority = if (verse.chapter == 2 && verse.verse == 47) 10 else 7,
        )
    }

    override val interpretationRecords: List<AynvoraInterpretationRecord> = preloadedVerses.map { verse ->
        AynvoraInterpretationRecord(
            recordId = "gita_interp_${verse.chapter}_${verse.verse}",
            domain = CoreFeatureId.GITA,
            primarySymbolOrKey = "Gita ${verse.chapter}.${verse.verse}",
            contextCondition = verse.themes.joinToString(", "),
            verifiedMeaning = "${verse.translation}\nTraditional Commentary: ${verse.traditionalCommentary}",
            sourceReference = sourceReferences.first(),
        )
    }

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "gita_rule_nishkama_karma",
            domain = CoreFeatureId.GITA,
            description = "Nishkama Karma: The individual has right over action alone, never over its fruits.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        ),
        AynvoraKnowledgeRule(
            ruleId = "gita_rule_svadharma",
            domain = CoreFeatureId.GITA,
            description = "Svadharma: Better is one's own duty, though destitute of merit, than the duty of another well discharged.",
            sourceReference = sourceReferences.first(),
            priority = 9,
        ),
    )

    override val exclusions: List<String> = listOf(
        "Do not claim that the Gita determines scientific or medical fact.",
        "Do not invent verses, Sanskrit words, or imaginary chapters.",
        "Do not assert fatalistic career certainty based on scripture.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.NO_INVENTED_SCRIPTURE,
        AynvoraSafetyConstraint.NO_INVENTED_CITATIONS,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
        AynvoraSafetyConstraint.NON_AUTHORITATIVE_REFLECTIVE_FRAMING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> {
        val qLower = (question + " " + (userContext?.statedSituation ?: "") + " " + (userContext?.selectedAreaOfReflection ?: "")).lowercase()
        val matchingVerses = preloadedVerses.filter { v ->
            v.themes.any { qLower.contains(it.lowercase()) } ||
                qLower.contains("duty") && v.themes.contains("SVADHARMA") ||
                qLower.contains("action") && v.themes.contains("NISHKAMA_KARMA") ||
                qLower.contains("stuck") && v.themes.contains("DECISION_MAKING") ||
                qLower.contains("career") && (v.themes.contains("SVADHARMA") || v.themes.contains("NISHKAMA_KARMA")) ||
                qLower.contains("peace") && v.themes.contains("INNER_PEACE")
        }

        val selected = (if (matchingVerses.isNotEmpty()) matchingVerses else preloadedVerses).take(3)
        return selected.mapNotNull { v -> evidenceNodes.find { it.ruleId == "BG_${v.chapter}_${v.verse}" } }
    }

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> {
        val evidence = retrieveRelevantEvidence(question)
        val ids = evidence.map { it.ruleId }
        return interpretationRecords.filter { rec ->
            ids.any { rec.primarySymbolOrKey.contains(it?.replace("BG_", "")?.replace("_", ".") ?: "") }
        }
    }

    fun getVerse(chapter: Int, verse: Int): GitaVerseEntry? {
        return preloadedVerses.find { it.chapter == chapter && it.verse == verse }
    }

    companion object {
        fun defaultGitaVerses(): List<GitaVerseEntry> = listOf(
            GitaVerseEntry(
                chapter = 2,
                verse = 47,
                sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
                transliteration = "karmaṇy-evādhikāras te mā phaleṣu kadācana, mā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi",
                wordMeanings = "karmaṇi - in prescribed action; eva - certainly; adhikāraḥ - right; te - of you; mā - never; phaleṣu - in fruits; kadācana - at any time",
                translation = "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty.",
                themes = listOf("NISHKAMA_KARMA", "ACTION", "DUTY", "ATTACHMENT", "CAREER", "DECISION_MAKING"),
                traditionalCommentary = "Focus on the integrity of the action before you without cognitive entanglement in future results.",
            ),
            GitaVerseEntry(
                chapter = 3,
                verse = 35,
                sanskrit = "श्रेयान्स्वधर्मो विगुणः परधर्मात्स्वनुष्ठितात्। स्वधर्मे निधनं श्रेयः परधर्मो भयावहः॥",
                transliteration = "śreyān sva-dharmo viguṇaḥ para-dharmāt sv-anuṣṭhitāt, sva-dharme nidhanaṁ śreyaḥ para-dharmo bhayāvahaḥ",
                wordMeanings = "śreyān - far better; sva-dharmaḥ - one's own prescribed duty; viguṇaḥ - imperfect; para-dharmāt - than another's duty; sva-anuṣṭhitāt - well performed",
                translation = "It is far better to perform one's own natural duty, though tinged with faults, than to perform another's duty perfectly. Destruction in the course of performing one's own duty is better than engaging in another's duty, for to follow another's path is perilous.",
                themes = listOf("SVADHARMA", "DUTY", "VOCATION", "CAREER", "AUTHENTICITY"),
                traditionalCommentary = "Honoring your innate nature (svabhava) in your vocation brings harmony and clarity.",
            ),
            GitaVerseEntry(
                chapter = 2,
                verse = 48,
                sanskrit = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय। सिद्ध्यसिद्ध्योः समो भूत्वा समत्वं योग उच्यते॥",
                transliteration = "yoga-sthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya, siddhy-asiddhyoḥ samo bhūtvā samatvaṁ yoga ucyate",
                wordMeanings = "yoga-sthaḥ - steadfast in yoga; kuru - perform; karmāṇi - duties; saṅgam - attachment; tyaktvā - having abandoned; samatvam - equanimity; yogaḥ - yoga; ucyate - is called",
                translation = "Perform your duty equipoised, O Arjuna, abandoning all attachment to success or failure. Such equanimity is called yoga.",
                themes = listOf("EQUANIMITY", "INNER_PEACE", "ACTION", "MINDFULNESS"),
                traditionalCommentary = "True mastery is calm steadiness regardless of transient favorable or unfavorable outcomes.",
            ),
            GitaVerseEntry(
                chapter = 18,
                verse = 66,
                sanskrit = "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज। अहं त्वां सर्वपापेभ्यो मोक्षयिष्यामि मा शुचः॥",
                transliteration = "sarva-dharmān parityajya mām ekaṁ śaraṇaṁ vraja, ahaṁ tvāṁ sarva-pāpebhyo mokṣayiṣyāmi mā śucaḥ",
                wordMeanings = "sarva-dharmān - all varieties of dharmas; parityajya - relinquishing; mām - unto Me; ekam - alone; śaraṇam - refuge; vraja - go",
                translation = "Abandon all varieties of secondary dharmas and simply surrender unto Me alone. I shall liberate you from all distress; do not grieve.",
                themes = listOf("SURRENDER", "FAITH", "LIBERATION", "PEACE"),
                traditionalCommentary = "Letting go of obsessive personal control and trusting the transcendent cosmic order.",
            ),
        )
    }
}

/**
 * Verified Knowledge Pack for Tarot Contemplation.
 */
class TarotKnowledgePack(
    override val locale: String = "en",
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.TAROT
    override val knowledgePackId: String = "kp_tarot_rws_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Rider-Waite-Smith Tarot Archetypes",
            rulesetOrEdition = "1909 Archetype Standard",
            authorOrTranslator = "A.E. Waite & Pamela Colman Smith",
            provenancePolicy = "PUBLIC_DOMAIN_ARCHETYPAL_CONTEMPLATION",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = emptyList()

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "tarot_fool_upright",
            domain = CoreFeatureId.TAROT,
            primarySymbolOrKey = "The Fool (0) - Upright",
            contextCondition = "New beginnings, spontaneous steps",
            verifiedMeaning = "Embracing fresh horizons with openness, unburdened curiosity, and beginner's mind.",
            sourceReference = sourceReferences.first(),
        ),
        AynvoraInterpretationRecord(
            recordId = "tarot_magician_upright",
            domain = CoreFeatureId.TAROT,
            primarySymbolOrKey = "The Magician (I) - Upright",
            contextCondition = "Resource alignment, focused intention",
            verifiedMeaning = "Skillful alignment of internal clarity and external resources to manifest constructive action.",
            sourceReference = sourceReferences.first(),
        ),
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "tarot_rule_symbolic_mirror",
            domain = CoreFeatureId.TAROT,
            description = "Cards serve as symbolic contemplative mirrors for reflection, not fortune-telling or deterministic prophecy.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not predict lottery wins, stock prices, or dates of death.",
        "Do not diagnose clinical mental illness.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> = evidenceNodes

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> {
        return interpretationRecords
    }
}

/**
 * Verified Knowledge Pack for Numerology Traditions.
 */
class NumerologyKnowledgePack(
    override val locale: String = "en",
    calculatedEvidence: List<EvidenceItem> = emptyList(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.NUMEROLOGY
    override val knowledgePackId: String = "kp_numerology_multi_tradition_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Western Pythagorean & Chaldean Classical Numerology",
            rulesetOrEdition = "Harmonic Number Symbolism",
            authorOrTranslator = "Classical Mathematical Numerology Traditions",
            provenancePolicy = "DETERMINISTIC_NUMERICAL_REDUCTION",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = calculatedEvidence

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "num_rec_1",
            domain = CoreFeatureId.NUMEROLOGY,
            primarySymbolOrKey = "Number 1",
            contextCondition = "Pioneering, individuality",
            verifiedMeaning = "Leadership, initiation, courage, and original thought.",
            sourceReference = sourceReferences.first(),
        ),
        AynvoraInterpretationRecord(
            recordId = "num_rec_7",
            domain = CoreFeatureId.NUMEROLOGY,
            primarySymbolOrKey = "Number 7",
            contextCondition = "Analytical contemplation, introspection",
            verifiedMeaning = "Deep analytical inquiry, truth-seeking, wisdom, and spiritual discernment.",
            sourceReference = sourceReferences.first(),
        ),
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "num_rule_strict_reduction",
            domain = CoreFeatureId.NUMEROLOGY,
            description = "Calculated numbers are deterministic arithmetic sums that cannot be modified by AI.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not invent new numbers outside the deterministic calculation.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.PRESERVE_NUMERICAL_FACTS,
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> = evidenceNodes

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> {
        return interpretationRecords
    }
}

/**
 * Verified Knowledge Pack for Palmistry Analysis.
 */
class PalmistryKnowledgePack(
    override val locale: String = "en",
    extractedEvidence: List<EvidenceItem> = emptyList(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.PALMISTRY
    override val knowledgePackId: String = "kp_palmistry_samudrika_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Samudrika Shastra & Classical Palmistry",
            rulesetOrEdition = "Hastarekha Vijnana / Cheiro Classical System",
            authorOrTranslator = "Classical Hastarekha Scholars",
            provenancePolicy = "GEOMETRIC_LINE_OBSERVATION_GROUNDED",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = extractedEvidence

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "palm_rec_heart_line",
            domain = CoreFeatureId.PALMISTRY,
            primarySymbolOrKey = "Heart Line (Hridaya Rekha)",
            contextCondition = "Curvature towards Jupiter mount",
            verifiedMeaning = "Reflects emotional warmth, interpersonal responsiveness, and affective vitality.",
            sourceReference = sourceReferences.first(),
        ),
        AynvoraInterpretationRecord(
            recordId = "palm_rec_head_line",
            domain = CoreFeatureId.PALMISTRY,
            primarySymbolOrKey = "Head Line (Mastaka Rekha)",
            contextCondition = "Clear linear trajectory across palm",
            verifiedMeaning = "Denotes intellectual concentration, mental endurance, and cognitive orientation.",
            sourceReference = sourceReferences.first(),
        ),
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "palm_rule_visual_observation",
            domain = CoreFeatureId.PALMISTRY,
            description = "AI reflection must cite only geometric line features actually segmented or observed.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not predict medical illness, physical lifespan, or genetic conditions from palm lines.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS,
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> = evidenceNodes

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> = interpretationRecords
}

/**
 * Verified Knowledge Pack for Gemstones and Navaratna.
 */
class GemstoneKnowledgePack(
    override val locale: String = "en",
    gemstoneEvidence: List<EvidenceItem> = emptyList(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.GEMSTONE
    override val knowledgePackId: String = "kp_gemstone_navaratna_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Garuda Purana Ratna Pariksha & Classical Navaratna",
            rulesetOrEdition = "Navaratna Gemological Tradition",
            authorOrTranslator = "Classical Ratna Shastra Treatises",
            provenancePolicy = "DETERMINISTIC_PLANETARY_RESONANCE",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = gemstoneEvidence

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "gem_rec_ruby",
            domain = CoreFeatureId.GEMSTONE,
            primarySymbolOrKey = "Manikya (Ruby) - Sun",
            contextCondition = "Benefic Sun placement, Lagnesha alignment",
            verifiedMeaning = "Supports vitality, self-confidence, leadership, and public authority.",
            sourceReference = sourceReferences.first(),
        ),
        AynvoraInterpretationRecord(
            recordId = "gem_rec_emerald",
            domain = CoreFeatureId.GEMSTONE,
            primarySymbolOrKey = "Marakata (Emerald) - Mercury",
            contextCondition = "Benefic Mercury placement, Budhaditya Yoga",
            verifiedMeaning = "Supports eloquence, analytical acumen, trade, and learning.",
            sourceReference = sourceReferences.first(),
        ),
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "gem_rule_planetary_compatibility",
            domain = CoreFeatureId.GEMSTONE,
            description = "Gemstones must strictly align with planetary lordship rules without conflicting enemy gems (e.g. Ruby + Blue Sapphire).",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not claim gemstones physically cure cancer or medical diseases.",
        "Do not guarantee financial windfalls upon wearing.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS,
        AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> = evidenceNodes

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> = interpretationRecords
}

/**
 * Verified Knowledge Pack for Garuda Puran.
 */
class GarudaPuranKnowledgePack(
    override val locale: String = "en",
    garudaEvidence: List<EvidenceItem> = emptyList(),
) : AynvoraKnowledgePack {
    override val featureId: CoreFeatureId = CoreFeatureId.GARUDA_PURAN
    override val knowledgePackId: String = "kp_garuda_puran_v1"
    override val version: String = "1.0.0"

    override val sourceReferences: List<AynvoraSourceReference> = listOf(
        AynvoraSourceReference(
            sourceName = "Garuda Purana (Preta Khanda & Dharma Khanda)",
            rulesetOrEdition = "Sanskrit Classical Edition",
            authorOrTranslator = "Canonical Purana Corpus",
            provenancePolicy = "STRICT_CANONICAL_PROVENANCE",
        )
    )

    override val evidenceNodes: List<EvidenceItem> = garudaEvidence

    override val interpretationRecords: List<AynvoraInterpretationRecord> = listOf(
        AynvoraInterpretationRecord(
            recordId = "garuda_rec_karma_transcendence",
            domain = CoreFeatureId.GARUDA_PURAN,
            primarySymbolOrKey = "Karmic Accountability and Dharma",
            contextCondition = "Dharma passages",
            verifiedMeaning = "Righteous conduct and charity alleviate distress and purify consciousness.",
            sourceReference = sourceReferences.first(),
        )
    )

    override val rules: List<AynvoraKnowledgeRule> = listOf(
        AynvoraKnowledgeRule(
            ruleId = "garuda_rule_provenance",
            domain = CoreFeatureId.GARUDA_PURAN,
            description = "Passages must cite approved edition metadata and never invent non-existent verses.",
            sourceReference = sourceReferences.first(),
            priority = 10,
        )
    )

    override val exclusions: List<String> = listOf(
        "Do not invent sensationalized hellish punishments not in canonical text.",
    )

    override val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_INVENTED_SCRIPTURE,
        AynvoraSafetyConstraint.NO_INVENTED_CITATIONS,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
        AynvoraSafetyConstraint.NON_AUTHORITATIVE_REFLECTIVE_FRAMING,
    )

    override fun retrieveRelevantEvidence(
        question: String,
        userContext: AynvoraUserContext?
    ): List<EvidenceItem> = evidenceNodes

    override fun retrieveRelevantInterpretations(question: String): List<AynvoraInterpretationRecord> = interpretationRecords
}
