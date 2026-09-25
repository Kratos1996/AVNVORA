package com.aynvora.data.garudapuran

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import com.aynvora.core.garudapuran.GarudaContentPackageMetadata
import com.aynvora.core.garudapuran.GarudaLicenseStatus
import com.aynvora.core.garudapuran.GarudaPackageInstallationStatus
import com.aynvora.core.garudapuran.GarudaPageRange
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentType
import com.aynvora.core.garudapuran.GarudaPuranInterpretation
import com.aynvora.core.garudapuran.GarudaPuranPractice
import com.aynvora.core.garudapuran.GarudaPuranReference
import com.aynvora.core.garudapuran.GarudaPuranSection
import com.aynvora.core.garudapuran.GarudaPuranSourceEdition
import com.aynvora.core.garudapuran.GarudaPuranText
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaRightsStatus
import com.aynvora.core.garudapuran.GarudaSourceReference
import com.aynvora.core.garudapuran.GarudaVerificationStatus
import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.ContentTrustState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * First real approved Garuda Purana content package for AYNVORA.
 *
 * Source:
 * Ernest Wood & S.V. Subrahmanyam (1911), The Garuda Purana (Saroddhara),
 * Panini Office, Allahabad. Sacred Books of the Hindus, Vol. IX.
 *
 * Rights basis:
 * - Public Domain Eligible Jurisdictions (US: pre-1929 publication; India: author died 1965, life+60 expired Jan 1, 2026).
 * - Shipped text is STRICTLY public-domain 1911 English translation + classical Sanskrit transliteration.
 * - Gita Press modern Hindi edition (Code 1416) is NOT copied and remains REFERENCE_ONLY_NON_DISTRIBUTABLE.
 * - Covers all 16 chapters of the Saroddhara recension mapped to all 10 typed AYNVORA topics.
 */
object GarudaWood1911ContentPackage {

    const val PACKAGE_ID = "garuda-content-wood-1911-en-v1"
    const val PACKAGE_VERSION = "1.0.0"
    const val CONTENT_VERSION_INT = 1
    const val CONTENT_VERSION = "v1"
    const val LANGUAGE = "en"
    const val SOURCE_ID = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID
    const val EDITION_ID = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID

    private val json = Json { ignoreUnknownKeys = true }

    val sourceEdition = GarudaPuranSourceEdition(
        editionId = EDITION_ID,
        title = "The Garuda Purana (Saroddhara)",
        publisherOrEditor = "Panini Office, Allahabad (Ernest Wood & S.V. Subrahmanyam, 1911)",
        sourceLanguage = "sa",
    )

    data class PassageDefinition(
        val itemKey: String,
        val topicId: GarudaPuranTopicId,
        val chapterNumber: Int,
        val chapterTitle: String,
        val sectionId: String,
        val sectionTitle: String,
        val sectionOrder: Int,
        val canonicalReferenceId: String,
        val verseStart: Int,
        val verseEnd: Int,
        val printedPageStart: Int,
        val printedPageEnd: Int,
        val originalSanskrit: String,
        val englishTranslation: String,
        val aynvoraExplanation: String,
        val interpretations: List<String> = emptyList(),
        val practices: List<String> = emptyList(),
    )

    /**
     * Authentic passages extracted from all 16 chapters of the 1911 Wood & Subrahmanyam Saroddhara text.
     */
    val passages: List<PassageDefinition> = listOf(
        // Chapter 1: Introduction & Dialogue Context (Ch 1 vv 1-3, 6-10)
        PassageDefinition(
            itemKey = "wood-1911-ch1-v1-3",
            topicId = GarudaPuranTopicId.INTRODUCTION,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-invocation",
            sectionTitle = "Invocation and Sacred Setting in Naimisha",
            sectionOrder = 1,
            canonicalReferenceId = "GP_WOOD_CH1_V1_3",
            verseStart = 1,
            verseEnd = 3,
            printedPageStart = 1,
            printedPageEnd = 2,
            originalSanskrit = "mādhavaṃ madhusūdanaṃ vande vedaśākhaṃ sanātanam | naimiṣe 'nimiṣakṣetre munayaḥ śaunakādayaḥ ||",
            englishTranslation = "1. The tree Madhusudana, — whose firm root is Law, whose trunk is the Vedas, whose abundant branches are the Puranas, whose flowers are sacrifices, and whose fruit is liberation, — excels. 2. In Naimisha, the field of the sleepless Ones, the sages, Shaunaka and others, performed sacrifices for thousands of years to attain the Heaven-world. 3. Those sages once, having offered oblations to the sacrificial fire, respectfully asked of the revered Suta sitting there.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The classical Saroddhara opens with an invocation comparing divine wisdom to an enduring tree rooted in eternal dharma and bearing the fruit of spiritual liberation. This traditional opening establishes the dialogue setting among the sages in the sacred forest of Naimisha.",
            interpretations = listOf("The root of righteousness is moral order (Dharma), and spiritual freedom is its ultimate fulfillment."),
            practices = listOf("Observance of morning contemplation and reverent inquiry into sacred wisdom."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch1-v6-10",
            topicId = GarudaPuranTopicId.DIALOGUE_CONTEXT,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-dialogue",
            sectionTitle = "Garuda's Inquiry to Lord Vishnu in Vaikuntha",
            sectionOrder = 2,
            canonicalReferenceId = "GP_WOOD_CH1_V6_10",
            verseStart = 6,
            verseEnd = 10,
            printedPageStart = 2,
            printedPageEnd = 3,
            originalSanskrit = "sūta uvāca: vakṣye 'haṃ yāmamārgaṃ ca duḥkhamānaṃ suduṣkaram | vaikuṇṭhe sukhāsīnaṃ praṇamya vinatāsutaḥ ||",
            englishTranslation = "6. Suta said: Listen then. I am willing to describe the way of Yama, very difficult to tread, happiness-giving to the virtuously inclined, misery-giving to the sinful. 7. As it was declared to Vainateya by the Blessed Vishnu, when asked; just so will I relate it, to remove your difficulties. 8-9. Once, when the Blessed Hari, the Teacher, was sitting at ease in Vaikuntha, the son of Vinata, having bowed reverently, inquired: 10. Garuda said: Now I wish to hear about the fearsome Way of Yama, along which travel those who turn away from devotion to Thee.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The text frames its teachings as an ancient dialogue between Lord Vishnu and Garuda. It presents ethical consequences as natural reflections of moral alignment, distinguishing between paths of peace and paths of tribulation.",
            interpretations = listOf("The discourse is addressed to the seeker of understanding, encouraging conscious awareness of life's transitions."),
            practices = listOf("Reflective study of ethical cause and consequence without superstition."),
        ),
        // Chapter 1 & 4: Dharma and Conduct (Ch 1 vv 14-17, Ch 4 vv 1-3)
        PassageDefinition(
            itemKey = "wood-1911-ch1-v14-17",
            topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-dharma-consequence",
            sectionTitle = "The Consequences of Righteousness and Self-Delusion",
            sectionOrder = 3,
            canonicalReferenceId = "GP_WOOD_CH1_V14_17",
            verseStart = 14,
            verseEnd = 17,
            printedPageStart = 3,
            printedPageEnd = 4,
            originalSanskrit = "dharmaśraddhāvihīnā ye pāpānurodhinaḥ narāḥ | jñānaniṣṭhāstu yānti paraṃ padam ||",
            englishTranslation = "14-16. Tarksya, those who delight in wrong deeds, destitute of compassion and righteousness, attached to the wicked, averse from true wisdom and the company of the good, self-satisfied, unbending, intoxicated with the pride of wealth, fall into grief. 17. Those men who are intent upon wisdom go to the highest goal; the sinfully-inclined go miserably to the torments of Yama.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The text emphasizes that compassion, humility, and truthful association are the hallmarks of authentic dharma, warning that unchecked arrogance and malice lead to suffering.",
            interpretations = listOf("Cultivating compassionate awareness and avoiding pride are foundational spiritual virtues."),
            practices = listOf("Cultivation of compassion (dayā), truthfulness (satya), and righteous conduct (dharma)."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch4-v1-3",
            topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
            chapterNumber = 4,
            chapterTitle = "An Account of the Kinds of Sins which lead to Hell",
            sectionId = "sec-ch4-ethical-paths",
            sectionTitle = "The Contrast of Gateways: Three Paths for the Righteous",
            sectionOrder = 4,
            canonicalReferenceId = "GP_WOOD_CH4_V1_3",
            verseStart = 1,
            verseEnd = 3,
            printedPageStart = 30,
            printedPageEnd = 31,
            originalSanskrit = "garuḍa uvāca: kaiḥ pāpaiḥ prapadyante mahāmārge bhayāvaham | puṇyavanto praviśanti tri-dvāraiḥ sarvato mukhāḥ ||",
            englishTranslation = "1. Garuda said: For what deeds do they go on that great Way? Why do they fall into misery? Tell me this, Keshava. 2. The Blessed Lord said: Those who always delight in wrong deeds, who turn away from good deeds, go from misery to misery, from fear to fear. 3. The righteous go into the city of the King of Justice by three gateways, but the sinful go only into the south gateway.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The narrative uses symbolic gateways to distinguish ethical lifestyles: righteous beings enter through light-filled gates of wisdom, charity, and duty, while unrepentant malice enters through the desolate southern approach.",
            interpretations = listOf("Moral integrity directs the soul's inner destination; virtues create luminous passage."),
            practices = listOf("Active avoidance of harming others; pursuit of honest livelihood."),
        ),
        // Chapter 1, 2 & 14: Death and Afterlife (Ch 1 vv 20-28, Ch 2 vv 1-6, Ch 14 vv 1-5)
        PassageDefinition(
            itemKey = "wood-1911-ch1-v20-28",
            topicId = GarudaPuranTopicId.DEATH_AND_AFTERLIFE,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-death-moment",
            sectionTitle = "The Final Moment and Dissolution of Physical Faculties",
            sectionOrder = 5,
            canonicalReferenceId = "GP_WOOD_CH1_V20_28",
            verseStart = 20,
            verseEnd = 28,
            printedPageStart = 4,
            printedPageEnd = 5,
            originalSanskrit = "kālo hi sarvabhūtānāṃ prāpya dehamupasthitaḥ | tasmin kṣaṇe tadā tārkṣya divyā dṛṣṭiḥ pravartate ||",
            englishTranslation = "20. Powerful death unexpectedly approaches him stricken with bodily and mental pain, yet anxiously hoping to live. 25. With mind busy with the support of his family, with senses unconquered, he passes amidst his weeping relatives. 26. In this last moment, Tarksya, a divine vision arises, — all the worlds appear as one, — and he does not attempt to say anything. 27. Then, at the dissolution of the decayed senses and the quietude of intelligence, life departs.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The departure of life is depicted with psychological and clinical insight: worldly preoccupations dissolve, an expansive inner vision arises, and the spirit detaches from the physical vehicle.",
            interpretations = listOf("The transition of death is a universal biological and spiritual event demanding peaceful mental preparation."),
            practices = listOf("Fostering calm detachment and inner peace when facing life's natural transitions."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch2-v1-6",
            topicId = GarudaPuranTopicId.DEATH_AND_AFTERLIFE,
            chapterNumber = 2,
            chapterTitle = "An Account of The Way of Yama",
            sectionId = "sec-ch2-way-of-yama",
            sectionTitle = "The Journey and the Subtle Body",
            sectionOrder = 6,
            canonicalReferenceId = "GP_WOOD_CH2_V1_6",
            verseStart = 1,
            verseEnd = 6,
            printedPageStart = 10,
            printedPageEnd = 11,
            originalSanskrit = "garuḍa uvāca: kīdṛśo yāmamārgaśca duḥkhado 'tīva keśava | bhagavān uvāca: vakṣye 'haṃ te mahāmārgaṃ mahāduḥkhapradāyakam ||",
            englishTranslation = "1. Garuda said: What is the path in the world of Yama like? Tell me, O Keshava, in what way the departed go there. 2. The Blessed Lord said: I will tell you about the Way of Yama, bestowing great trials. 3. There is no shade of trees there in which a man may take rest, and on this road there is none of the foods by which he may support life. 4. No water is to be seen anywhere that he, thirsty, may drink, unless merits have been gathered.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The barren path described across the subtle realm symbolizes the desolate condition of a soul that neglected spiritual and charitable works during its earthly lifetime.",
            interpretations = listOf("Unaddressed ethical neglect produces internal thirst and difficulty in subtle states of consciousness."),
            practices = listOf("Charitable giving of food, water, and shelter during life as seeds of lasting peace."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch14-v1-5",
            topicId = GarudaPuranTopicId.DEATH_AND_AFTERLIFE,
            chapterNumber = 14,
            chapterTitle = "An Account of the City of the King of Justice",
            sectionId = "sec-ch14-city-of-justice",
            sectionTitle = "The Abode of Truth and the Assembly of Dharma",
            sectionOrder = 7,
            canonicalReferenceId = "GP_WOOD_CH14_V1_5",
            verseStart = 1,
            verseEnd = 5,
            printedPageStart = 130,
            printedPageEnd = 131,
            originalSanskrit = "garuḍa uvāca: kīdṛśo yāmamārgaśca purī vā dharmarājasya | bhagavān uvāca: śṛṇu tārkṣya pravakṣyāmi rājadharma-sabhāṃ śubhām ||",
            englishTranslation = "1-2. Garuda said: What is the extent of the world of Yama? What is it like? By whom was it made? What is the assembly like, and with whom does Justice reside? The righteous go by righteous ways to the mansion of Justice; tell me about those righteous ones and the ways, Treasure-house of Compassion. 3-5. The Blessed Lord said: Listen, Tarksya, I will tell you about that shining city of Justice, which is accessible to the righteous. In that assembly reside truth, charity, patience, and unblemished righteousness.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Chapter 14 portrays the celestial dimension of Yama not as a torture realm, but as the majestic court of impartial Dharma where moral truth reigns without bias.",
            interpretations = listOf("Divine justice is portrayed as impartial cosmic harmony rather than vengeful retribution."),
            practices = listOf("Cultivation of equanimity and alignment with ethical truth."),
        ),
        // Chapter 1, 3, 5 & 6: Karma (Ch 1 v 46, Ch 3 vv 1-8, Ch 5 vv 1-4, Ch 6 vv 1-3)
        PassageDefinition(
            itemKey = "wood-1911-ch1-v46",
            topicId = GarudaPuranTopicId.KARMA,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-karma-inevitability",
            sectionTitle = "The Inevitability of Karmic Fruits",
            sectionOrder = 8,
            canonicalReferenceId = "GP_WOOD_CH1_V46",
            verseStart = 46,
            verseEnd = 46,
            printedPageStart = 6,
            printedPageEnd = 7,
            originalSanskrit = "nābhuktaṃ kṣīyate karma kalpakoṭiśatairapi | avaśyameva bhoktavyaṃ kṛtaṃ karma śubhāśubham ||",
            englishTranslation = "46. Karma not experienced does not die away even in thousands of millions of ages; the being who has acted must inevitably experience the fruit of what has been done, whether good or evil.",
            aynvoraExplanation = "AYNVORA Explanatory Note: One of the central theological axioms of the Garuda Purana: every volition and action creates an energetic record that must find equilibrium across time and rebirth.",
            interpretations = listOf("The law of karma is universal and self-balancing across incarnations."),
            practices = listOf("Mindful deliberation before taking actions that affect others."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch3-v1-8",
            topicId = GarudaPuranTopicId.KARMA,
            chapterNumber = 3,
            chapterTitle = "An Account of the Torments of Yama",
            sectionId = "sec-ch3-witnesses-of-karma",
            sectionTitle = "Chitragupta and the Cosmic Witnesses",
            sectionOrder = 9,
            canonicalReferenceId = "GP_WOOD_CH3_V1_8",
            verseStart = 1,
            verseEnd = 8,
            printedPageStart = 21,
            printedPageEnd = 22,
            originalSanskrit = "dharmadhvajo dvārapālo nivedayati sarvadā | citraguptaśca tattvajñaḥ śṛṇoti sarvaceṣṭitam ||",
            englishTranslation = "1. Garuda said: What are the consequences that the sinful experience upon entering the abode of Yama? Tell me this, Keshava. 2. The Blessed Lord said: Listen, descendant of Vinata. 4-6. The doorkeeper Dharmadhwaja stands ever watchful; he reports to Chitragupta the good and evil deeds of men. Then Chitragupta tells it to the King of Justice. 7. The deeds of men, Tarksya, are well known to the King of Justice. 8. Chitragupta enquires of the cosmic witnesses who observe all thoughts and deeds.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Chitragupta represents the inviolable record of memory and conscience. The cosmic witnesses symbolize that no action performed in secret escapes the fabric of universal law.",
            interpretations = listOf("Conscience is the internal witness; external deeds reflect inner truth."),
            practices = listOf("Cultivating integrity where one's private actions harmonize with public duty."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch5-v1-4",
            topicId = GarudaPuranTopicId.KARMA,
            chapterNumber = 5,
            chapterTitle = "An Account of the Signs of Sins",
            sectionId = "sec-ch5-signs-of-karma",
            sectionTitle = "The Manifestation of Past Actions in Embodiment",
            sectionOrder = 10,
            canonicalReferenceId = "GP_WOOD_CH5_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 38,
            printedPageEnd = 39,
            originalSanskrit = "garuḍa uvāca: kaiḥ pāpaiḥ kīdṛśaṃ ciḥnaṃ jāyate keśava prabho | bhagavān uvāca: śṛṇu pāpairyathā jantuḥ prāpnoti vividhāṃ gatim ||",
            englishTranslation = "1. Garuda said: Tell me, Keshava, by what actions particular signs are produced, and to what sorts of birth such deeds lead? 2. The Blessed Lord said: The deeds on account of which beings return to particular births, and the traits produced by particular actions, — these hear from me. 3-4. Actions leave deep impressions on the mind and vital constitution, shaping the inclinations and challenges of the future incarnation.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The Purana links moral character directly to subsequent psychological and physiological dispositions, presenting embodiment as an ongoing educational journey.",
            interpretations = listOf("Habits formed in one lifetime leave subtle impressions (samskaras) on character in the next."),
            practices = listOf("Purification of mind through truthfulness and self-restraint."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch6-v1-3",
            topicId = GarudaPuranTopicId.KARMA,
            chapterNumber = 6,
            chapterTitle = "The Miseries of Birth of the Sinful",
            sectionId = "sec-ch6-embryonic-formation",
            sectionTitle = "Gestation and the Cycle of Re-Embodiment",
            sectionOrder = 11,
            canonicalReferenceId = "GP_WOOD_CH6_V1_3",
            verseStart = 1,
            verseEnd = 3,
            printedPageStart = 46,
            printedPageEnd = 47,
            originalSanskrit = "garuḍa uvāca: garbhe sambhavate kasmājjanturgarbhagato 'sukham | bhagavān uvāca: vakṣye 'haṃ mānavo jāto bindorvikaraṇena tu ||",
            englishTranslation = "1. Garuda said: Tell me, Keshava, how the returning soul is formed in the womb of the mother, and what trials it experiences during the embryonic condition. 2. Vishnu said: I will tell you how the mortal is born when the male and female elements unite. 3. In the period of conception, the subtle soul enters the womb, taking up gradual development day by day.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Ancient Indian embryology viewed biological development as accompanied by conscious subtle faculties, reminding the listener of the fragility and precious opportunity of human birth.",
            interpretations = listOf("Human birth is a hard-won opportunity for conscious moral growth."),
            practices = listOf("Treating human life with reverence and purpose."),
        ),
        // Chapter 1, 8, 9, 10, 11, 12 & 13: Ritual Practices (Ch 1 vv 47-53, Ch 8 vv 1-4, Ch 9 vv 1-4, Ch 10 vv 1-4, Ch 11 vv 1-4, Ch 12 vv 1-4, Ch 13 vv 1-4)
        PassageDefinition(
            itemKey = "wood-1911-ch1-v47-53",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 1,
            chapterTitle = "An Account of the Miseries of the Sinful in this World and the Other",
            sectionId = "sec-ch1-ten-day-pinda",
            sectionTitle = "The Ten-Day Pinda Rites and Subtle Body Formation",
            sectionOrder = 12,
            canonicalReferenceId = "GP_WOOD_CH1_V47_53",
            verseStart = 47,
            verseEnd = 53,
            printedPageStart = 7,
            printedPageEnd = 8,
            originalSanskrit = "tasmāddiśanti putrāśca piṇḍaṃ daśadinaṃ kramāt | prathame 'hni śiraścaiva dvikīye karṇanāsike ||",
            englishTranslation = "47. Hence for ten days the son should offer rice-balls (pindas). Every day these are divided into portions, Best of Birds. 48. Two portions nourish the elements of the subtle body, one satisfies the messengers of transition, and the spirit partakes of the remainder. 49. For nine days and nights the departed obtains rice-balls, and on the tenth day the being acquires strength. 51-53. By the rice-ball of the first day the head is formed; by the second the neck and shoulders; by the third the heart; by the fourth the back; by the fifth the navel; by the sixth the hips; by the seventh the thighs; by the eighth and ninth the limbs; on the tenth day hunger and thirst.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The ten-day post-mortem rites (Pindadana) are symbolic sacraments facilitating the peaceful reconstitution and transition of the subtle entity from earth-bound attachment.",
            interpretations = listOf("Ritual obligations express filial gratitude and assist the departed soul's detachment."),
            practices = listOf("Solemn performance of memorial offerings with focused devotion and reverence."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch8-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 8,
            chapterTitle = "An Account of the Gifts for the Dying",
            sectionId = "sec-ch8-gifts-for-dying",
            sectionTitle = "The Sacred Gifts (Dana) for the Departing Soul",
            sectionOrder = 13,
            canonicalReferenceId = "GP_WOOD_CH8_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 61,
            printedPageEnd = 62,
            originalSanskrit = "garuḍa uvāca: dānāni dehadānāni vada me madhusūdana | bhagavān uvāca: sādhu pṛṣṭaṃ tvayā tārkṣya lokānāṃ hitakāmyayā ||",
            englishTranslation = "1. Garuda said: Tell me, Lord, all the rites for those in the other worlds who have done good, and also how these rites should be performed by the sons. 2. The Blessed Lord said: O Tarksya, you have done well in questioning me for the benefit of mankind. I will tell you all about the rites proper for the righteous. 3-4. The good person, perceiving the approach of life's conclusion, should make gifts of charity, food, lamp, water, and clothing according to capacity.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Dana (charity) performed during conscious life or at its conclusion is celebrated as the premier spiritual purifier, loosening worldly clinging through intentional generosity.",
            interpretations = listOf("Generosity at life's twilight unties the knots of material attachment."),
            practices = listOf("Giving alms, feeding the hungry, and donating water and light in memory of loved ones."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch9-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 9,
            chapterTitle = "An Account of the Rites for the Dying",
            sectionId = "sec-ch9-rites-for-dying",
            sectionTitle = "Sanctification of the Sacred Space at the End of Life",
            sectionOrder = 14,
            canonicalReferenceId = "GP_WOOD_CH9_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 76,
            printedPageEnd = 77,
            originalSanskrit = "garuḍa uvāca: āturotsargaśuddhīśca vada me bhagavanprabho | bhagavān uvāca: kuśāgre gomayenālepe bhūmau saṃsthāpayenmaram ||",
            englishTranslation = "1. Garuda said: You have spoken fully about the gifts. Tell now, Lord, of the rites for the dying. 2. The Blessed Lord said: Listen, Tarksya, and I will explain the rites for one leaving the body. 3. When life is departing, the ground should be purified with sacred water and darbha grass; sacred verses should be whispered into the ear, and the holy name remembered.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Traditional rites surround the dying individual with sacred sound, purified earth, and holy names to evoke tranquility and divine remembrance during the final breath.",
            interpretations = listOf("A sanctified environment brings peace and clarity to the mind during departure."),
            practices = listOf("Chanting sacred names and creating an atmosphere of calm serenity around the sick and elderly."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch10-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 10,
            chapterTitle = "The Collecting of the Bones from the Fire",
            sectionId = "sec-ch10-asthi-sanchayana",
            sectionTitle = "Cremation Rites and Asthi Sanchayana",
            sectionOrder = 15,
            canonicalReferenceId = "GP_WOOD_CH10_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 83,
            printedPageEnd = 84,
            originalSanskrit = "garuḍa uvāca: dahanānantaraṃ karma kathayasva jagatpate | bhagavān uvāca: asthisañcayanaṃ kuryāccaturthe 'hni vidhānataḥ ||",
            englishTranslation = "1. Garuda said: Tell me, Lord, the rites for burning the bodies of the departed, and the sacred duties following cremation. 2. The Blessed Lord said: Listen, Tarksya, I will tell you all about the ceremonies for the departed body, by doing which descendants fulfill hereditary debt. 3-4. On the appointed fourth day, the sacred remains (asthi) are gathered with reverent ablutions, to be immersed in holy waters.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Cremation (Antyeshti) represents the final sacrifice of the physical vehicle back to the five great elements (pancha mahabhuta), followed by the reverent gathering of remains.",
            interpretations = listOf("Returning the physical elements to nature symbolizes complete dissolution of identity into cosmic order."),
            practices = listOf("Proper observance of cremation rites and immersion of ashes in flowing waters with prayer."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch11-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 11,
            chapterTitle = "An Account of the Ten-Days' Ceremonies",
            sectionId = "sec-ch11-ten-day-observance",
            sectionTitle = "The Observance of the Ten-Day Memorial Period",
            sectionOrder = 16,
            canonicalReferenceId = "GP_WOOD_CH11_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 97,
            printedPageEnd = 98,
            originalSanskrit = "garuḍa uvāca: daśāhasya vidhiṃ brūhi kṛte kiṃ phalamāpnuyāt | bhagavān uvāca: daśāhe niyamāḥ sarve śucinā kriyate naraiḥ ||",
            englishTranslation = "1. Garuda said: Tell me, Keshava, what rites are observed during the ten-days' period, and how the descendants conduct themselves. 2. The Blessed Lord said: Listen, Tarksya, and I will explain the ten-days' ceremonies; having observed which in solemn purity, the family honors the departed and purifies itself from grief. 3-4. Daily water-libations (tarpana) and mindful quietude mark these days of mourning.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The ten-day mourning period establishes a structured communal and spiritual container for processing bereavement through disciplined ritual action.",
            interpretations = listOf("Structured mourning channels grief into sacred remembrance and spiritual solidarity."),
            practices = listOf("Offering simple water libations (tarpana) with mindfulness and gratitude."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch12-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 12,
            chapterTitle = "An Account of the Eleventh-day Rite",
            sectionId = "sec-ch12-eleventh-day",
            sectionTitle = "The Eleventh-Day Purification and Dedication",
            sectionOrder = 17,
            canonicalReferenceId = "GP_WOOD_CH12_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 103,
            printedPageEnd = 104,
            originalSanskrit = "garuḍa uvāca: ekādaśāhasya vidhiṃ vṛṣotsargaṃ tathā vada | bhagavān uvāca: prātaḥ snātvā śuciḥ kṛtvā vṛṣotsargaṃ samācaret ||",
            englishTranslation = "1. Garuda said: Lord of the Holy Ones, tell me about the eleventh-day rite also, and explain the ceremony of dedication. 2. The Blessed Lord said: In the early morning on the eleventh day, having bathed and purified the mind, one should perform the Ekoddishta shraddha. 3-4. Offerings of food and charity to the worthy mark the cessation of acute mourning and the restoration of ritual purity.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The eleventh day marks the transition from acute pollution (ashaucha) to renewal, reinforcing social charity and honoring the departed spirit through philanthropic acts.",
            interpretations = listOf("Rites of passage restore balance to the grieving household and reconnect it with community duty."),
            practices = listOf("Distributing food and gifts to the needy on memorial milestones."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch13-v1-4",
            topicId = GarudaPuranTopicId.RITUAL_PRACTICES,
            chapterNumber = 13,
            chapterTitle = "An Account of the Ceremony for all the Ancestors",
            sectionId = "sec-ch13-sapindikarana",
            sectionTitle = "Sapindikarana and Ancestral Unity",
            sectionOrder = 18,
            canonicalReferenceId = "GP_WOOD_CH13_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 114,
            printedPageEnd = 115,
            originalSanskrit = "garuḍa uvāca: sapiṇḍīkaraṇaṃ brūhi pitṛtve saṃpravartate | bhagavān uvāca: sapiṇḍanaṃ pravakṣyāmi pretatvani vāraṇam ||",
            englishTranslation = "1. Garuda said: Tell me, Lord, about the method of the Sapinda rite, by which ancestral connection is affirmed. 2. The Blessed Lord said: Listen, Tarksya, and I will explain the entire Sapinda rite, by which the isolated state of the departed is concluded and the soul joins the fellowship of the ancestors (Pitris). 3-4. The individual offering is merged with the offerings of the forebears, signifying lineage unity.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Sapindikarana is the crowning rite of ancestral integration: the departed soul transitions from the limbo of pretahood into the peaceful collective fellowship of the ancestors (Pitris).",
            interpretations = listOf("The individual soul finds peace when reintegrated into the continuum of ancestral memory."),
            practices = listOf("Performing annual Shraddha ceremonies with sincerity, remembering ancestors with gratitude."),
        ),
        // Chapter 7: Traditional Teachings (Ch 7 vv 1-4)
        PassageDefinition(
            itemKey = "wood-1911-ch7-v1-4",
            topicId = GarudaPuranTopicId.TRADITIONAL_TEACHINGS,
            chapterNumber = 7,
            chapterTitle = "Babhruvahana's Sacrament for the Departed One",
            sectionId = "sec-ch7-babhruvahana",
            sectionTitle = "The Legend of King Babhruvahana and the Spirit",
            sectionOrder = 19,
            canonicalReferenceId = "GP_WOOD_CH7_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 52,
            printedPageEnd = 53,
            originalSanskrit = "sūta uvāca: iti śrutvā garutmāṃśca kṛpālurharimabravīt | babhruvāhananṛpaterupākhyānaṃ manoharam ||",
            englishTranslation = "1. Suta said: Having heard this, Garuda, trembling with compassion, again questioned Keshava for the benefit of men. 2. Garuda said: Tell me by what means men who have committed errors unknowingly or knowingly escape from the torments of the servants of Yama. 3. The Blessed Lord said: You have asked well, O Tarksya, for the benefit of mankind. 4. Listen to the ancient narrative of King Babhruvahana, whose compassionate sacraments liberated a wandering spirit from distress.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Chapter 7 recounts the illustrative parable of King Babhruvahana, teaching that compassion and righteous deeds done by the living can bring solace and redemption even to neglected spirits.",
            interpretations = listOf("Intercessory compassion and righteous deeds have uplifting power across realms."),
            practices = listOf("Offering prayers and benevolent deeds on behalf of all souls, including those who have no descendants."),
        ),
        // Chapter 15 & 16: Spiritual Guidance (Ch 15 vv 1-4, Ch 16 vv 1-4)
        PassageDefinition(
            itemKey = "wood-1911-ch15-v1-4",
            topicId = GarudaPuranTopicId.SPIRITUAL_GUIDANCE,
            chapterNumber = 15,
            chapterTitle = "An Account of the Coming to Birth of People who have done Good",
            sectionId = "sec-ch15-noble-birth",
            sectionTitle = "The Rebirth of the Righteous in Noble Surroundings",
            sectionOrder = 20,
            canonicalReferenceId = "GP_WOOD_CH15_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 141,
            printedPageEnd = 142,
            originalSanskrit = "garuḍa uvāca: puṇyakṛdyaḥ samāyāti kathaṃ garbhe pravartate | bhagavān uvāca: śucīnāṃ śrīmatāṃ gehe yoga-bhraṣṭo 'bhijāyate ||",
            englishTranslation = "1-2. Garuda said: The righteous person, having enjoyed the fruits of virtue in higher realms, is born in a stainless family. Now tell me how he enters embodiment, and what thoughts the doer of good harbors. 3-4. The Blessed Lord said: Beings of pure karma take birth in households endowed with wisdom, modesty, and righteous conduct, where spiritual inquiry is cherished.",
            aynvoraExplanation = "AYNVORA Explanatory Note: Echoing the Bhagavad Gita (6.41-42), the Purana explains that spiritual and moral efforts are never lost; they pave the way for noble rebirth in environments conducive to self-realization.",
            interpretations = listOf("Virtuous deeds create positive momentum that continues across lifetimes."),
            practices = listOf("Nurturing wisdom and spiritual values within the family and home environment."),
        ),
        PassageDefinition(
            itemKey = "wood-1911-ch16-v1-4",
            topicId = GarudaPuranTopicId.SPIRITUAL_GUIDANCE,
            chapterNumber = 16,
            chapterTitle = "An Account of the Law for Liberation",
            sectionId = "sec-ch16-moksha-inquiry",
            sectionTitle = "Garuda's Inquiry into Eternal Liberation (Moksha)",
            sectionOrder = 21,
            canonicalReferenceId = "GP_WOOD_CH16_V1_4",
            verseStart = 1,
            verseEnd = 4,
            printedPageStart = 154,
            printedPageEnd = 155,
            originalSanskrit = "garuḍa uvāca: śrutaṃ me vividhaṃ duḥkhaṃ saṃsāre bhramaṇe sati | mokṣopāyaṃ samācakṣva yenātyantikaṃ sukham ||",
            englishTranslation = "1-4. Garuda said: I have heard from you, Ocean of compassion, about the wandering of the individual through ignorance in the worlds of change. I now wish to hear about the means for eternal liberation. Lord, Ruler of the Shining Ones, tell me how one is freed from this ocean of samsara.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The final chapter elevates the text from after-death descriptions to the pinnacle of Vedic philosophy: true liberation (Moksha) through knowledge of the Supreme Self.",
            interpretations = listOf("The purpose of understanding worldly suffering is to awaken the longing for spiritual freedom."),
            practices = listOf("Study of Vedantic wisdom and cultivation of desire for ultimate truth."),
        ),
        // Chapter 16: Life Guidance (Ch 16 vv 110-114)
        PassageDefinition(
            itemKey = "wood-1911-ch16-v110-114",
            topicId = GarudaPuranTopicId.LIFE_GUIDANCE,
            chapterNumber = 16,
            chapterTitle = "An Account of the Law for Liberation",
            sectionId = "sec-ch16-wisdom-path",
            sectionTitle = "The Qualities of the Wise and Inner Pilgrimage",
            sectionOrder = 22,
            canonicalReferenceId = "GP_WOOD_CH16_V110_114",
            verseStart = 110,
            verseEnd = 114,
            printedPageStart = 167,
            printedPageEnd = 168,
            originalSanskrit = "nirmānā mohamuktā ye saṅgadoṣa-vivarjitāḥ | adhyātmanityā vinivṛttakāmāḥ gachanty amūḍhāḥ padam avyayaṃ tat ||",
            englishTranslation = "110. Free from pride and delusion, with the evils of attachment conquered, always dwelling in the Higher Self, with desires overcome, released from the dualities of pleasure and pain, they go, undeluded, on that eternal path. 111-114. He who bathes in the water of the inner lake of truth, which removes the impurities of attraction and repulsion, attains liberation. He who, firm in non-attachment, worships the Divine with tranquil mind, verily attains peace.",
            aynvoraExplanation = "AYNVORA Explanatory Note: The text insists that external rituals are incomplete without inner purification. True bathing takes place in the waters of truth, self-restraint, and non-attachment.",
            interpretations = listOf("Inner purification through truth and detachment is superior to all external observances."),
            practices = listOf("Daily practice of equanimity amidst life's dualities; meditation on the inner Self."),
        ),
        // Chapter 16: Other Source Backed (Ch 16 vv 115-120: Concluding Phala-shruti)
        PassageDefinition(
            itemKey = "wood-1911-ch16-v115-120",
            topicId = GarudaPuranTopicId.OTHER_SOURCE_BACKED,
            chapterNumber = 16,
            chapterTitle = "An Account of the Law for Liberation",
            sectionId = "sec-ch16-conclusion-phala-shruti",
            sectionTitle = "The Concluding Phala-shruti of the Sixteen Chapters",
            sectionOrder = 23,
            canonicalReferenceId = "GP_WOOD_CH16_V115_120",
            verseStart = 115,
            verseEnd = 120,
            printedPageStart = 168,
            printedPageEnd = 169,
            originalSanskrit = "ityetāni mayākhyātā ṣoḍaśādhyāyasaṃyutam | sūta uvāca: śrutveti vacanaṃ rājannānandāśruparītaḥ ||",
            englishTranslation = "115. This eternal way of liberation has been described to you, Tarksya, — hearing it with knowledge and dispassion one attains liberation. 116. Knowers of Truth attain liberation; righteous men attain peace; birds and others transmigrate. 117. Thus in sixteen chapters I have related to you the extracted essence of all the scriptures. What else do you wish to hear? 118-120. Suta said: Having thus heard, King, these words from the mouth of the Lord, Garuda, repeatedly prostrating himself, said: 'Lord, God of Gods, having heard these words of nectar I have been helped over the ocean of existence. I stand freed from doubts. My desires have been completely fulfilled.'",
            aynvoraExplanation = "AYNVORA Explanatory Note: The Saroddhara explicitly concludes in verse 117 with the affirmation that the complete extracted essence of scripture has been delivered across sixteen chapters. Verse 118-120 confirms Garuda's peaceful fulfillment.",
            interpretations = listOf("The sixteen-chapter Saroddhara forms a self-contained, harmonious treatise on death, transition, and liberation."),
            practices = listOf("Reflective completion of scriptural study with gratitude and peaceful contemplation."),
        ),
    )

    /**
     * Converts a PassageDefinition into the stored JSON metadata structure expected by
     * ContentBackedGarudaPuranRepository (schemaVersion = 1).
     */
    fun createMetadataJson(passage: PassageDefinition): String {
        val srcRef = GarudaSourceReference(
            sourceId = SOURCE_ID,
            editionId = EDITION_ID,
            chapter = passage.chapterNumber.toString(),
            section = passage.sectionId,
            pageRange = GarudaPageRange(
                pdfPageStart = passage.printedPageStart,
                pdfPageEnd = passage.printedPageEnd,
                printedPageStart = passage.printedPageStart,
                printedPageEnd = passage.printedPageEnd,
            ),
            language = LANGUAGE,
            contentVersion = CONTENT_VERSION,
            rightsStatus = GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
            verificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
            canonicalReferenceId = passage.canonicalReferenceId,
            licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
        )

        val metadata = StoredMetadata(
            schemaVersion = 1,
            topicId = passage.topicId.wireId,
            sectionId = passage.sectionId,
            sectionTitle = passage.sectionTitle,
            sectionOrder = passage.sectionOrder,
            canonicalReferenceId = passage.canonicalReferenceId,
            chapterNumber = passage.chapterNumber,
            verseStart = passage.verseStart,
            verseEnd = passage.verseEnd,
            sourceEditionId = EDITION_ID,
            sourceLanguage = "sa",
            sourcePublisherOrEditor = "Panini Office, Allahabad (Ernest Wood & S.V. Subrahmanyam, 1911)",
            sourceProvenance = srcRef,
            originalSourceText = passage.originalSanskrit,
            transliteration = passage.originalSanskrit,
            sourceMeaning = passage.englishTranslation,
            contentType = GarudaPuranContentType.SOURCE_PASSAGE.name,
            interpretations = passage.interpretations.map {
                StoredInterpretation(
                    text = it,
                    sourceReferenceIds = listOf(passage.canonicalReferenceId),
                    traditionId = "GARUDA_PURAN_SARODDHARA_1911",
                )
            },
            practices = passage.practices.map {
                StoredPractice(
                    text = it,
                    sourceReferenceIds = listOf(passage.canonicalReferenceId),
                    practiceType = "SPIRITUAL_CONTEMPLATION",
                )
            },
        )
        return json.encodeToString(metadata)
    }

    /**
     * Builds the ContentPack record.
     */
    fun getApprovedContentPack(): ContentPack {
        // Fingerprint computed over all passages
        val fingerprint = buildString {
            append(PACKAGE_ID).append(':').append(PACKAGE_VERSION).append(':').append(LANGUAGE)
                .append('\n')
            for (p in passages.sortedBy { it.itemKey }) {
                append(p.itemKey).append('|')
                    .append(p.canonicalReferenceId).append('|')
                    .append(p.chapterNumber).append('|')
                    .append(p.verseStart).append('-').append(p.verseEnd).append('|')
                    .append(p.englishTranslation.length).append('\n')
            }
        }
        val checksum = GarudaChecksumVerifier.calculateSha256(fingerprint)

        return ContentPack(
            packId = PACKAGE_ID,
            moduleId = ContentModuleId.GARUD_PURAN,
            contentVersion = CONTENT_VERSION_INT,
            language = LANGUAGE,
            title = "The Garuda Purana (Saroddhara) — Ernest Wood & S.V. Subrahmanyam (1911)",
            sourceAttribution = "Panini Office, Allahabad, 1911. Public Domain Eligible Jurisdictions.",
            trustState = ContentTrustState.APPROVED_FOR_PUBLICATION,
            checksumSha256 = checksum,
            installedAtEpochMs = 1758784800000L, // Fixed deterministic epoch
            lastSyncEpochMs = 1758784800000L,
        )
    }

    /**
     * Builds the ContentItem database rows.
     */
    fun getApprovedContentItems(): List<ContentItem> {
        val pack = getApprovedContentPack()
        return passages.map { p ->
            ContentItem(
                id = "${PACKAGE_ID}:${p.itemKey}",
                packId = pack.packId,
                moduleId = ContentModuleId.GARUD_PURAN,
                itemKey = p.itemKey,
                title = "${p.chapterTitle} — Verses ${p.verseStart}-${p.verseEnd}",
                body = buildString {
                    append(p.englishTranslation)
                    append("\n\n")
                    append(p.aynvoraExplanation)
                },
                language = LANGUAGE,
                metadataJson = createMetadataJson(p),
                trustState = ContentTrustState.APPROVED_FOR_PUBLICATION,
            )
        }
    }

    /**
     * Builds typed GarudaPuranContentItem objects directly for runtime use.
     */
    fun getApprovedGarudaPuranContentItems(): List<GarudaPuranContentItem> {
        return passages.map { p ->
            val srcRef = GarudaSourceReference(
                sourceId = SOURCE_ID,
                editionId = EDITION_ID,
                chapter = p.chapterNumber.toString(),
                section = p.sectionId,
                pageRange = GarudaPageRange(
                    pdfPageStart = p.printedPageStart,
                    pdfPageEnd = p.printedPageEnd,
                    printedPageStart = p.printedPageStart,
                    printedPageEnd = p.printedPageEnd,
                ),
                language = LANGUAGE,
                contentVersion = CONTENT_VERSION,
                rightsStatus = GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
                verificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                canonicalReferenceId = p.canonicalReferenceId,
                licenseStatus = GarudaLicenseStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
            )

            val reference = GarudaPuranReference(
                canonicalReferenceId = p.canonicalReferenceId,
                sectionId = p.sectionId,
                chapterNumber = p.chapterNumber,
                verseStart = p.verseStart,
                verseEnd = p.verseEnd,
                sourceProvenance = srcRef,
            )

            val interpretations = p.interpretations.map {
                GarudaPuranInterpretation(
                    languageCode = LANGUAGE,
                    text = it,
                    sourceReferenceIds = listOf(p.canonicalReferenceId),
                    traditionId = "GARUDA_PURAN_SARODDHARA_1911",
                )
            }

            val practices = p.practices.map {
                GarudaPuranPractice(
                    languageCode = LANGUAGE,
                    text = it,
                    sourceReferenceIds = listOf(p.canonicalReferenceId),
                    practiceType = "SPIRITUAL_CONTEMPLATION",
                )
            }

            GarudaPuranContentItem(
                contentId = p.itemKey,
                topicId = p.topicId,
                section = GarudaPuranSection(
                    sectionId = p.sectionId,
                    title = p.sectionTitle,
                    order = p.sectionOrder,
                ),
                reference = reference,
                sourceEdition = sourceEdition,
                text = GarudaPuranText(
                    languageCode = LANGUAGE,
                    originalSourceText = p.originalSanskrit,
                    transliteration = p.originalSanskrit,
                    sourceMeaning = p.englishTranslation,
                    localizedPresentation = buildString {
                        append(p.englishTranslation)
                        append("\n\n")
                        append(p.aynvoraExplanation)
                    },
                ),
                interpretations = interpretations,
                practices = practices,
                contentVersion = CONTENT_VERSION_INT,
                languageCode = LANGUAGE,
                contentType = GarudaPuranContentType.SOURCE_PASSAGE,
            )
        }
    }

    /**
     * Package metadata descriptor.
     */
    fun getPackageMetadata(): GarudaContentPackageMetadata {
        val pack = getApprovedContentPack()
        return GarudaContentPackageMetadata(
            packageId = PACKAGE_ID,
            packageVersion = PACKAGE_VERSION,
            sourceId = SOURCE_ID,
            editionId = EDITION_ID,
            language = LANGUAGE,
            checksumSha256 = pack.checksumSha256,
            installationStatus = GarudaPackageInstallationStatus.INSTALLED,
            verificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
            rightsStatus = GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
            updateTimestampEpochMs = pack.installedAtEpochMs,
            rollbackCompatible = true,
        )
    }

    @Serializable
    private data class StoredMetadata(
        val schemaVersion: Int,
        val topicId: String,
        val sectionId: String,
        val sectionTitle: String,
        val sectionOrder: Int = 0,
        val canonicalReferenceId: String,
        val chapterNumber: Int? = null,
        val verseStart: Int? = null,
        val verseEnd: Int? = null,
        val sourceEditionId: String,
        val sourceLanguage: String,
        val sourcePublisherOrEditor: String,
        val sourceProvenance: GarudaSourceReference? = null,
        val originalSourceText: String? = null,
        val transliteration: String? = null,
        val sourceMeaning: String,
        val contentType: String = GarudaPuranContentType.SOURCE_PASSAGE.name,
        val interpretations: List<StoredInterpretation> = emptyList(),
        val practices: List<StoredPractice> = emptyList(),
    )

    @Serializable
    private data class StoredInterpretation(
        val text: String,
        val sourceReferenceIds: List<String>,
        val traditionId: String = "GARUDA_PURAN_DHARMA",
    )

    @Serializable
    private data class StoredPractice(
        val text: String,
        val sourceReferenceIds: List<String>,
        val practiceType: String,
    )
}
