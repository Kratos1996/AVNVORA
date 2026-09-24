package com.aynvora.core.astrology.prediction

import com.aynvora.core.intelligence.TraditionProfile

/**
 * Standard starter pack of classical astrological prediction rules.
 * Reference: Brihat Parashara Hora Shastra (BPHS), Phaladeepika, and Saravali.
 */
object CanonicalAstrologyRules {

    val RULE_YOGAKARAKA_ACTIVATION = AstrologicalRule(
        ruleId = "BPHS_YOGAKARAKA_01",
        tradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
        topic = PredictionTopic.CAREER,
        sourceTitle = "Brihat Parashara Hora Shastra",
        sourceChapterOrVerse = "Chapter 34, Sloka 16",
        description = "A planet simultaneously owning a Kendra (angle: 1, 4, 7, 10) and a Trikona (trine: 5, 9) acts as Yogakaraka, granting professional prominence during its Dasha/Antardasha.",
    )

    val RULE_DHANA_YOGA = AstrologicalRule(
        ruleId = "BPHS_DHANA_YOGA_01",
        tradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
        topic = PredictionTopic.FINANCE,
        sourceTitle = "Brihat Parashara Hora Shastra",
        sourceChapterOrVerse = "Chapter 41, Sloka 2",
        description = "Relationship or mutual aspect between the lord of the 2nd house (Dhana) and the 11th house (Labha) forms Dhana Yoga, indicating wealth accumulation windows.",
    )

    val RULE_GAJAKESARI_YOGA = AstrologicalRule(
        ruleId = "PHALADEEPIKA_GAJAKESARI_01",
        tradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
        topic = PredictionTopic.EDUCATION,
        sourceTitle = "Phaladeepika",
        sourceChapterOrVerse = "Chapter 6, Sloka 17",
        description = "Jupiter in a Kendra (1st, 4th, 7th, 10th) from the Moon confers lasting wisdom, intellectual reputation, and virtuous conduct.",
    )

    val RULE_7TH_LORD_TRANSIT_ACTIVATION = AstrologicalRule(
        ruleId = "SARAVALI_KALATRA_01",
        tradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
        topic = PredictionTopic.RELATIONSHIPS,
        sourceTitle = "Saravali",
        sourceChapterOrVerse = "Chapter 35, Sloka 11",
        description = "When transiting Jupiter aspects or transits the natal 7th house or 7th lord, relationship commitments and alliances are activated.",
    )

    val RULE_10TH_HOUSE_TRANSIT_ACTIVATION = AstrologicalRule(
        ruleId = "BPHS_KARMA_TRANSIT_01",
        tradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
        topic = PredictionTopic.CAREER,
        sourceTitle = "Brihat Parashara Hora Shastra",
        sourceChapterOrVerse = "Chapter 43, Sloka 9",
        description = "Transiting Jupiter or Saturn aspecting the 10th house from Lagna or Moon prompts significant shifts and new milestones in one's vocation.",
    )

    val ALL_RULES = listOf(
        RULE_YOGAKARAKA_ACTIVATION,
        RULE_DHANA_YOGA,
        RULE_GAJAKESARI_YOGA,
        RULE_7TH_LORD_TRANSIT_ACTIVATION,
        RULE_10TH_HOUSE_TRANSIT_ACTIVATION,
    )

    fun getRulesForTopic(topic: PredictionTopic): List<AstrologicalRule> =
        ALL_RULES.filter { it.topic == topic }
}
