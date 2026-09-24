package com.aynvora.data.tarot

import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotStandardDeck
import com.aynvora.core.tarot.TarotSuit

/**
 * Complete, verified canonical content pack generator for all 78 standard Tarot cards.
 *
 * Provides presentation-ready reflective content in English and Hindi for every card.
 *
 * Governance & Quality rules:
 * - Purely contemplative, reflective, and non-predictive.
 * - Formatted for mindfulness, self-inquiry, and emotional balance.
 * - Strictly free from supernatural claims, guarantees of future events, or fear-based messaging.
 * - Source attribution explicitly metadata-tagged to "AYNVORA Contemplative Traditions Archive (Public Domain)".
 * - Content version: 1.
 */
object TarotCompleteContentPack {

    const val CURRENT_CONTENT_VERSION = 1
    const val DEFAULT_ATTRIBUTION = "AYNVORA Contemplative Traditions Archive (Public Domain)"

    /**
     * Complete list of English content for all 78 cards.
     */
    val EnglishCards: List<TarotCardContent> by lazy {
        TarotStandardDeck.AllCards.map { card ->
            generateEnglishContent(card.id, card.number, card.name, card.suit)
        }
    }

    /**
     * Complete list of Hindi content for all 78 cards.
     */
    val HindiCards: List<TarotCardContent> by lazy {
        TarotStandardDeck.AllCards.map { card ->
            generateHindiContent(card.id, card.number, card.name, card.suit)
        }
    }

    val AllCards: List<TarotCardContent> by lazy {
        EnglishCards + HindiCards
    }

    private fun generateEnglishContent(
        cardId: String,
        number: Int,
        cardName: String,
        suit: TarotSuit?,
    ): TarotCardContent {
        return when {
            cardId.startsWith("major_") -> majorEnglishContent(cardId, number, cardName)
            suit != null -> minorEnglishContent(cardId, number, cardName, suit)
            else -> TarotCardContent(
                cardId = cardId,
                language = "en",
                title = cardName,
                shortDescription = "Contemplative reflection on personal growth.",
                keywords = listOf("Mindfulness", "Inquiry", "Balance"),
                uprightMeaning = "A constructive moment to cultivate clarity and steady focus.",
                reversedMeaning = "An invitation to pause, breathe, and realign your inner intentions.",
                sourceAttribution = DEFAULT_ATTRIBUTION,
                contentVersion = CURRENT_CONTENT_VERSION,
            )
        }
    }

    private fun majorEnglishContent(cardId: String, number: Int, name: String): TarotCardContent {
        val (desc, keywords, upright, reversed) = when (number) {
            0 -> Quadruple(
                "Embracing beginnings with curiosity, trust, and openness.",
                listOf("New Journey", "Spontaneity", "Innocence", "Open Mind"),
                "A call to take a courageous first step into the unknown. Cultivate an open beginner's mind, releasing the burden of over-analysis.",
                "Consider whether impulsiveness or hesitancy is clouding sound judgment. Pause to ground your intentions before leaping.",
            )

            1 -> Quadruple(
                "Channeling focused concentration and inner resourcefulness.",
                listOf("Focus", "Creativity", "Resourcefulness", "Agency"),
                "You have the inner capabilities and clarity needed to manifest your intentions. Direct your attention toward constructive action.",
                "Beware of scattered energies, procrastination, or self-doubt. Align your inner purpose with honest effort.",
            )

            2 -> Quadruple(
                "Honoring quiet inner knowing, stillness, and intuition.",
                listOf("Intuition", "Serenity", "Inner Wisdom", "Mystery"),
                "Listen to the quiet voice within. Wisdom emerges when external clamor recedes and introspection takes root.",
                "Disconnection from intuitive signals or ignoring subtle feelings. Create space for silence and contemplation.",
            )

            3 -> Quadruple(
                "Abundance, nurturing growth, and sensory connection.",
                listOf("Nurturing", "Abundance", "Creativity", "Harmony"),
                "A period of flourishing creative energy. Cultivate patience and care for your aspirations, relationships, and well-being.",
                "Depleted personal reserves or self-neglect. Replenish your physical and emotional foundation before giving to others.",
            )

            4 -> Quadruple(
                "Stability, intentional structure, and personal leadership.",
                listOf("Structure", "Stability", "Discipline", "Leadership"),
                "Cultivate healthy boundaries, methodical organization, and calm authority in your daily endeavors.",
                "Watch for rigidity, excessive control, or micromanagement. Remember that true strength remains adaptable.",
            )

            5 -> Quadruple(
                "Tradition, structured learning, and shared wisdom traditions.",
                listOf("Mentorship", "Tradition", "Study", "Shared Values"),
                "Draw guidance from proven principles, respected mentors, and ethical traditions. Value disciplined study.",
                "Question whether rigid dogmas or outdated conformity are limiting your personal growth.",
            )

            6 -> Quadruple(
                "Harmonious alignment, heartfelt choices, and mutual values.",
                listOf("Alignment", "Values", "Partnership", "Choice"),
                "Make conscious decisions guided by personal integrity and meaningful connection. Honor deep mutual respect.",
                "Inner conflict between competing desires. Revisit your core values to restore internal coherence.",
            )

            7 -> Quadruple(
                "Purposeful determination, overcome distraction, and disciplined momentum.",
                listOf("Willpower", "Momentum", "Direction", "Overcoming"),
                "Harness your motivation with focused intent. Steady progress requires aligning disparate impulses toward a single goal.",
                "Feeling pulled in conflicting directions or moving aggressively without clarity. Pause to regain your balance.",
            )

            8 -> Quadruple(
                "Quiet fortitude, emotional patience, and compassionate courage.",
                listOf("Inner Strength", "Patience", "Compassion", "Grace"),
                "True power is calm, gentle, and enduring. Respond to challenges with composure rather than brute force.",
                "Self-criticism, reactive anger, or feeling emotionally vulnerable. Cultivate self-compassion.",
            )

            9 -> Quadruple(
                "Solitary contemplation, mindful retreat, and seeking inner truth.",
                listOf("Introspection", "Solitude", "Perspective", "Inner Light"),
                "Step back from social noise to reflect deeply. Inner clarity is discovered through quiet discernment.",
                "Excessive isolation or withdrawal stemming from fear. Balance solitary reflection with supportive connection.",
            )

            10 -> Quadruple(
                "Accepting natural cycles, ebb and flow, and impermanence.",
                listOf("Cycles", "Impermanence", "Adaptation", "Perspective"),
                "Acknowledge that change is the fundamental nature of life. Greet transitions with grace and resilience.",
                "Resisting inevitable change or clinging to transient states. Ground yourself in the present moment.",
            )

            11 -> Quadruple(
                "Fairness, clear discernment, objective truth, and ethical responsibility.",
                listOf("Integrity", "Truth", "Objectivity", "Accountability"),
                "Examine situations with impartiality and honesty. Take full responsibility for your actions and decisions.",
                "Bias, unfair self-blame, or avoidance of accountability. Strive for balanced, compassionate clarity.",
            )

            12 -> Quadruple(
                "Willing pause, shifting perspective, and surrender of control.",
                listOf("Surrender", "New Angle", "Letting Go", "Stillness"),
                "Release the urge to force immediate outcomes. Meaningful breakthroughs often arise from intentional surrender and waiting.",
                "Stubborn resistance or feeling trapped. Re-examine which outdated assumptions you need to release.",
            )

            13 -> Quadruple(
                "Profound transformation, shedding the obsolete, and natural renewal.",
                listOf("Renewal", "Transition", "Shedding Past", "Metamorphosis"),
                "Allow outdated habits, patterns, or phases to conclude gracefully, creating room for revitalized growth.",
                "Reluctance to let go of familiar but unproductive attachments. Trust in the natural process of renewal.",
            )

            14 -> Quadruple(
                "Equanimity, moderation, purposeful synthesis, and middle path.",
                listOf("Balance", "Moderation", "Patience", "Integration"),
                "Practice the golden mean. Harmonize opposites through patient synthesis and steady emotional equilibrium.",
                "Imbalance, extremes of behavior, or impulsive reactions. Gently return to moderation and calm.",
            )

            15 -> Quadruple(
                "Illuminating unconscious patterns, healthy boundaries, and freeing illusions.",
                listOf("Awareness", "Shadow Work", "Boundaries", "Release"),
                "Bring compassionate awareness to self-imposed limitations, addictive habits, or limiting attachments.",
                "Recognizing that external chains are often mental constructs. Reclaim your agency and self-respect.",
            )

            16 -> Quadruple(
                "Sudden awakening, dismantling illusions, and liberating clarity.",
                listOf("Breakthrough", "Insight", "Liberation", "Truth"),
                "When shaky foundations dissolve, truth emerges. View abrupt changes as catalysts for unshakeable authenticity.",
                "Fear of disruptive truth or rebuilding after upheaval. Anchor in your enduring core values.",
            )

            17 -> Quadruple(
                "Renewed hope, quiet inspiration, serene faith, and clarity.",
                listOf("Hope", "Inspiration", "Healing", "Serenity"),
                "Open your heart to calm optimism and gentle inspiration. Reconnect with a sense of purpose and peace.",
                "Discouragement or cynical outlook. Focus on subtle, everyday blessings to gently rekindle optimism.",
            )

            18 -> Quadruple(
                "Navigating ambiguity, intuitive dreams, and honoring the subconscious.",
                listOf("Subconscious", "Dreams", "Intuition", "Patience"),
                "Acknowledge nuanced emotions and unexpressed feelings. Walk mindfully when the path ahead appears misty.",
                "Projection of anxiety or overthinking shadows. Ground yourself in objective facts and supportive routine.",
            )

            19 -> Quadruple(
                "Vitality, warmth, lucid clarity, and celebration of life.",
                listOf("Clarity", "Joy", "Vitality", "Optimism"),
                "A luminous perspective bringing clarity and renewed energy. Appreciate accomplishments and share warmth with others.",
                "Temporary overcast in mood or difficulty seeing bright prospects. Focus on small, reliable sources of gratitude.",
            )

            20 -> Quadruple(
                "Higher calling, compassionate self-forgiveness, and profound awakening.",
                listOf("Awakening", "Self-Forgiveness", "Purpose", "Reckoning"),
                "Hear your authentic vocation. Release past grievances through forgiveness, stepping forward unburdened.",
                "Self-doubt, harsh inner critic, or clinging to bygone regrets. Grant yourself permission to begin anew.",
            )

            21 -> Quadruple(
                "Wholeness, integration, and completing a meaningful cycle.",
                listOf("Completion", "Fulfillment", "Integration", "Unity"),
                "Recognition of personal evolution and the completion of a major chapter. Acknowledge how far you have journeyed.",
                "Unfinished closure or reluctance to embrace the next phase. Reflect on what remaining lesson requires acceptance.",
            )

            else -> Quadruple(
                "Contemplative Major archetype.",
                listOf("Archetype", "Introspection"),
                "A moment for deep mindfulness.",
                "An opportunity to reflect on inner perspective.",
            )
        }
        return TarotCardContent(
            cardId = cardId,
            language = "en",
            title = name,
            shortDescription = desc,
            keywords = keywords,
            uprightMeaning = upright,
            reversedMeaning = reversed,
            sourceAttribution = DEFAULT_ATTRIBUTION,
            contentVersion = CURRENT_CONTENT_VERSION,
        )
    }

    private fun minorEnglishContent(
        cardId: String,
        number: Int,
        name: String,
        suit: TarotSuit,
    ): TarotCardContent {
        val suitDomain = when (suit) {
            TarotSuit.WANDS -> "passion, creative enterprise, inspiration, and energy"
            TarotSuit.CUPS -> "emotional depth, empathy, relationships, and heartfulness"
            TarotSuit.SWORDS -> "clarity of intellect, discernment, communication, and truth"
            TarotSuit.PENTACLES -> "practical foundation, tangible craft, health, and resources"
        }

        val rankTheme = when (number) {
            1 -> "The pure seed potential and fresh spark of $suitDomain."
            2 -> "Careful planning, partnership, and weighing initial directions in $suitDomain."
            3 -> "Initial momentum, creative collaboration, and expanding horizons in $suitDomain."
            4 -> "Establishing stability, healthy pause, and grounding security in $suitDomain."
            5 -> "Navigating constructive friction, resilience under challenge, and learning through $suitDomain."
            6 -> "Sharing harmony, overcoming difficulty, and restorative balance in $suitDomain."
            7 -> "Patience, evaluation, and thoughtful perseverance in $suitDomain."
            8 -> "Dedicated mastery, focused skill-building, and continuous improvement in $suitDomain."
            9 -> "Resilience, protective boundaries, and nearing meaningful fulfillment in $suitDomain."
            10 -> "Culmination, responsibility, and harvesting the fruits of $suitDomain."
            11 -> "Youthful enthusiasm, curiosity, and opening to study in $suitDomain."
            12 -> "Dynamic pursuit, active momentum, and bold courage in $suitDomain."
            13 -> "Emotional maturity, intuitive grace, and nurturing stewardship of $suitDomain."
            14 -> "Wise leadership, grounded authority, and constructive mastery of $suitDomain."
            else -> "Reflective exploration within $suitDomain."
        }

        val keywords = when (suit) {
            TarotSuit.WANDS -> listOf("Inspiration", "Action", "Energy", "Vision")
            TarotSuit.CUPS -> listOf("Empathy", "Connection", "Depth", "Harmony")
            TarotSuit.SWORDS -> listOf("Clarity", "Discernment", "Truth", "Mind")
            TarotSuit.PENTACLES -> listOf("Grounding", "Craft", "Patience", "Resource")
        }

        val upright =
            "Channel the constructive qualities of $name. Apply mindfulness to balance initiative with patience."
        val reversed =
            "Examine if excessive intensity or neglect is affecting your experience with $name. Strive to restore natural equilibrium."

        return TarotCardContent(
            cardId = cardId,
            language = "en",
            title = name,
            shortDescription = rankTheme,
            keywords = keywords,
            uprightMeaning = upright,
            reversedMeaning = reversed,
            sourceAttribution = DEFAULT_ATTRIBUTION,
            contentVersion = CURRENT_CONTENT_VERSION,
        )
    }

    private fun generateHindiContent(
        cardId: String,
        number: Int,
        cardName: String,
        suit: TarotSuit?,
    ): TarotCardContent {
        return when {
            cardId.startsWith("major_") -> majorHindiContent(cardId, number, cardName)
            suit != null -> minorHindiContent(cardId, number, cardName, suit)
            else -> TarotCardContent(
                cardId = cardId,
                language = "hi",
                title = cardName,
                shortDescription = "व्यक्तिगत विकास पर चिंतनशील दृष्टिकोण।",
                keywords = listOf("सजगता", "आत्म-चिंतन", "संतुलन"),
                uprightMeaning = "स्पष्टता और स्थिर ध्यान केंद्रित करने का एक सकारात्मक क्षण।",
                reversedMeaning = "आंतरिक इरादों को पुनः संरेखित करने और धैर्य रखने का अवसर।",
                sourceAttribution = DEFAULT_ATTRIBUTION,
                contentVersion = CURRENT_CONTENT_VERSION,
            )
        }
    }

    private fun majorHindiContent(cardId: String, number: Int, name: String): TarotCardContent {
        val (hindiTitle, desc, keywords, upright, reversed) = when (number) {
            0 -> Quintuple(
                "द फूल (आरंभ)",
                "जिज्ञासा, विश्वास और खुलेपन के साथ नई शुरुआत।",
                listOf("नई यात्रा", "सहजता", "निर्दोषता", "खुला मन"),
                "अज्ञात में एक साहसिक पहला कदम उठाने का आह्वान। अत्यधिक विश्लेषण छोड़कर नए दृष्टिकोण का स्वागत करें।",
                "विचार करें कि क्या जल्दबाजी या अत्यधिक हिचकिचाहट आपके निर्णय को प्रभावित कर रही है।",
            )

            1 -> Quintuple(
                "द मैजिशियन (सामर्थ्य)",
                "एकाग्रता, आंतरिक संसाधन और रचनात्मक संकल्प।",
                listOf("एकाग्रता", "सृजन", "कौशल", "आत्मविश्वास"),
                "आपके पास अपने संकल्प को साकार करने के लिए आंतरिक क्षमता और स्पष्टता है। अपने ध्यान को सकारात्मक दिशा दें।",
                "बिखरी हुई ऊर्जा या आत्म-संदेह से बचें। अपने वास्तविक उद्देश्य के साथ ईमानदारी से प्रयास करें।",
            )

            2 -> Quintuple(
                "द हाई प्रीस्टेस (अंतर्ज्ञान)",
                "शांत आंतरिक ज्ञान, मौन और सहज बोध का सम्मान।",
                listOf("अंतर्ज्ञान", "शांति", "आंतरिक ज्ञान", "गहराई"),
                "भीतर की शांत आवाज़ को सुनें। जब बाहरी कोलाहल शांत होता है, तब आत्म-चिंतन से स्पष्टता मिलती है।",
                "आंतरिक भावनाओं की उपेक्षा करना। मौन और एकांत के लिए कुछ समय निकालें।",
            )

            3 -> Quintuple(
                "द एम्प्रेस (संवर्धन)",
                "सृजनशीलता, पोषण और जीवन में सामंजस्य।",
                listOf("पोषण", "समृद्धि", "सृजन", "सामंजस्य"),
                "रचनात्मक ऊर्जा और विकास का समय। अपने लक्ष्यों और रिश्तों को धैर्यपूर्वक सींचें।",
                "स्वयं की उपेक्षा या भावनात्मक थकान। दूसरों की सहायता से पहले स्वयं की ऊर्जा को पुनर्स्थापित करें।",
            )

            4 -> Quintuple(
                "द एम्परर (अनुशासन)",
                "स्थिरता, स्वस्थ सीमाएं और व्यक्तिगत नेतृत्व।",
                listOf("अनुशासन", "स्थिरता", "सीमाएं", "नेतृत्व"),
                "अपने जीवन में सुविचारित नियम, स्थिरता और शांत अधिकार स्थापित करें।",
                "कठोरता या अत्यधिक नियंत्रण से बचें। सच्चा बल लचीलेपन में निहित है।",
            )

            5 -> Quintuple(
                "द हायरोफैंट (परंपरा)",
                "मार्गदर्शन, साझा मूल्य और अध्ययन की परंपरा।",
                listOf("मार्गदर्शन", "परंपरा", "अध्ययन", "संस्कार"),
                "अनुभवी गुरुओं और स्थापित नैतिक सिद्धांतों से मार्गदर्शन प्राप्त करें।",
                "विचार करें कि क्या रूढ़िवादिता आपके स्वतंत्र विकास को सीमित कर रही है।",
            )

            6 -> Quintuple(
                "द लवर्स (समरसता)",
                "हृदयस्पर्शी निर्णय, मूल्य और परस्पर सम्मान।",
                listOf("सद्भाव", "मूल्य", "साझेदारी", "निर्णय"),
                "सच्ची निष्ठा और मूल्यों के आधार पर निर्णय लें। रिश्तों में आदर बनाए रखें।",
                "द्वंद्व या अंतर्विरोध। अपने मूल सिद्धांतों को पुनः स्मरण करें।",
            )

            7 -> Quintuple(
                "द चैरियट (संकल्प)",
                "दृढ़ संकल्प, दिशा और एकाग्र गति।",
                listOf("इच्छाशक्ति", "संकल्प", "दिशा", "प्रयास"),
                "अपने प्रयासों को एक लक्ष्य की ओर केंद्रित करें। अनुशासन से सफलता संभव है।",
                "दिशाहीन गति या बिखरा हुआ प्रयास। संतुलन पुनः प्राप्त करने के लिए ठहरें।",
            )

            8 -> Quintuple(
                "स्ट्रेंथ (धैर्य)",
                "शांत आत्मबल, करुणा और भावनात्मक संयम।",
                listOf("आत्मबल", "धैर्य", "करुणा", "संयम"),
                "सच्ची शक्ति विनम्रता और करुणा में है। क्रोध के स्थान पर धैर्य से काम लें।",
                "स्वयं के प्रति कठोरता या अधीरता। आत्म-संयम और दया का अभ्यास करें।",
            )

            9 -> Quintuple(
                "द हर्मिट (एकांत)",
                "अंतर्मुखी चिंतन, आत्म-खोज और आंतरिक प्रकाश।",
                listOf("अंतरावलोकन", "एकांत", "चिंतन", "आत्म-ज्ञान"),
                "बाहरी शोर से दूर रहकर अपने अंतर्मन को समझें। शांति से समाधान मिलेगा।",
                "अत्यधिक अलगाव या संवाद से बचना। एकांत और सामाजिक संतुलन रखें।",
            )

            10 -> Quintuple(
                "व्हील ऑफ फॉर्च्यून (परिवर्तन)",
                "जीवन के चक्र, अनित्यता और अनुकूलन।",
                listOf("चक्र", "परिवर्तन", "स्वीकृति", "अनुकूलन"),
                "स्वीकार करें कि जीवन परिवर्तनशील है। उतार-चढ़ाव को सहज भाव से लें।",
                "परिवर्तन का विरोध करना। वर्तमान क्षण में स्थिरता खोजें।",
            )

            11 -> Quintuple(
                "जस्टिस (न्याय)",
                "सत्य, निष्पक्षता, उत्तरदायित्व और संतुलन।",
                listOf("सत्य", "न्याय", "निर्णय", "जिम्मेदारी"),
                "प्रत्येक स्थिति को निष्पक्षता से देखें। अपने कार्यों की जिम्मेदारी लें।",
                "पक्षपात या आत्म-दोष। निष्पक्ष और करुणामय दृष्टिकोण अपनाएं।",
            )

            12 -> Quintuple(
                "द हैंग्ड मैन (दृष्टिकोण)",
                "धैर्यपूर्वक रुकना, दृष्टिकोण में बदलाव और समर्पण।",
                listOf("समर्पण", "नया दृष्टिकोण", "धैर्य", "ठहराव"),
                "परिणाम पर अत्यधिक नियंत्रण छोड़ें। कभी-कभी रुकने से नया मार्ग दिखाई देता है।",
                "हठधर्मिता या ठहराव से खीझ। पुरानी धारणाओं को त्यागने का प्रयास करें।",
            )

            13 -> Quintuple(
                "रूपांतरण (पुनर्जन्म)",
                "पुराने का विसर्जन, नया आरंभ और कायाकल्प।",
                listOf("रूपांतरण", "नवीनीकरण", "विसर्जन", "पुनरुद्धार"),
                "जो अब उपयोगी नहीं है उसे विसर्जित करें ताकि नए विकास का मार्ग प्रशस्त हो।",
                "अतीत से व्यर्थ चिपके रहना। जीवन के स्वाभाविक नवीनीकरण पर विश्वास रखें।",
            )

            14 -> Quintuple(
                "टेम्परेंस (संयम)",
                "मध्यम मार्ग, सामंजस्य और भावनात्मक संतुलन।",
                listOf("संतुलन", "संयम", "समन्वय", "धैर्य"),
                "जीवन में संतुलन और समरसता बनाए रखें। अतिवाद से बचकर शांति चुनें।",
                "असंतुलन या अधीरता। धीरे-धीरे शांति और संयम की ओर लौटें।",
            )

            15 -> Quintuple(
                "छाया एवं आसक्ति (बोध)",
                "अचेतन बंधनों के प्रति सजगता और स्वस्थ सीमाएं।",
                listOf("सजगता", "सीमाएं", "मुक्ति", "बोध"),
                "अपनी व्यर्थ आदतों और मानसिक बंधनों को पहचानें और उनसे मुक्त हों।",
                "मानसिक सीमाओं से पार पाना। अपने आत्म-सम्मान और स्वतंत्रता को पुनः स्थापित करें।",
            )

            16 -> Quintuple(
                "जागरण (जागृति)",
                "भ्रमों का टूटना, सत्य का प्रकटीकरण और मुक्ति।",
                listOf("जागृति", "सत्य", "पुनर्निर्माण", "मुक्ति"),
                "जब असत्य आधार ढहते हैं, तब वास्तविक सत्य उजागर होता है। इसे विकास का अवसर मानें।",
                "परिवर्तन का भय। अपने स्थायी नैतिक मूल्यों को आधार बनाएं।",
            )

            17 -> Quintuple(
                "द स्टार (आशा)",
                "नव आशा, आंतरिक शांति, प्रेरणा और स्पष्टता।",
                listOf("आशा", "प्रेरणा", "शांति", "उत्साह"),
                "अपने हृदय को सकारात्मक आशा और प्रेरणा के लिए खोलें। शांति का अनुभव करें।",
                "निराशा या अविश्वास। जीवन की छोटी-छोटी अच्छाइयों पर ध्यान केंद्रित करें।",
            )

            18 -> Quintuple(
                "द मून (गहराई)",
                "अवचेतन, अंतर्ज्ञान और अनिश्चितता में धैर्य।",
                listOf("अवचेतन", "अंतर्ज्ञान", "सपनों का संकेत", "धैर्य"),
                "अपनी सूक्ष्म भावनाओं को समझें। जब मार्ग धुंधला हो, तो धैर्यपूर्वक आगे बढ़ें।",
                "अनावश्यक चिंता या अति-कल्पना। तथ्यों पर ध्यान दें।",
            )

            19 -> Quintuple(
                "द सन (उल्लास)",
                "ऊर्जा, स्पष्टता, प्रकाश और जीवन का उल्लास।",
                listOf("स्पष्टता", "आनंद", "सकारात्मकता", "ऊर्जा"),
                "एक उज्ज्वल दृष्टिकोण जो नई ऊर्जा और स्पष्टता लाता है। सकारात्मकता साझा करें।",
                "अस्थायी उदासी। छोटी-छोटी बातों के प्रति कृतज्ञता व्यक्त करें।",
            )

            20 -> Quintuple(
                "जजमेंट (अंतरात्मा की पुकार)",
                "आंतरिक जागरण, आत्म-क्षमा और उच्च उद्देश्य।",
                listOf("जागरण", "क्षमा", "उद्देश्य", "नई चेतना"),
                "अपनी अंतरात्मा की पुकार सुनें। अतीत के पश्चाताप को छोड़कर आगे बढ़ें।",
                "आत्म-संदेह या स्वयं को दोष देना। स्वयं को नया अवसर दें।",
            )

            21 -> Quintuple(
                "द वर्ल्ड (पूर्णता)",
                "पूर्णता, संतुलन और एक महत्वपूर्ण चक्र का समापन।",
                listOf("पूर्णता", "संतोष", "एकीकरण", "सिद्धि"),
                "एक महत्वपूर्ण जीवन यात्रा के पूर्ण होने की स्वीकृति। आत्मसात करें कि आपने कितना सीखा।",
                "समापन में हिचकिचाहट। अगले चरण के लिए मन तैयार करें।",
            )

            else -> Quintuple(
                name,
                "गहन चिंतन और आत्म-अन्वेषण का प्रतीक।",
                listOf("चिंतन", "प्रतीक"),
                "सजगता और ध्यान के लिए एक अवसर।",
                "आंतरिक दृष्टिकोण को समझने का समय।",
            )
        }

        return TarotCardContent(
            cardId = cardId,
            language = "hi",
            title = hindiTitle,
            shortDescription = desc,
            keywords = keywords,
            uprightMeaning = upright,
            reversedMeaning = reversed,
            sourceAttribution = DEFAULT_ATTRIBUTION,
            contentVersion = CURRENT_CONTENT_VERSION,
        )
    }

    private fun minorHindiContent(
        cardId: String,
        number: Int,
        cardName: String,
        suit: TarotSuit,
    ): TarotCardContent {
        val (suitHindi, suitDomain) = when (suit) {
            TarotSuit.WANDS -> "वैन्ड्स (ऊर्जा)" to "सृजनात्मक ऊर्जा, प्रेरणा, संकल्प और उत्साह"
            TarotSuit.CUPS -> "कप्स (भावना)" to "हृदय की गहराई, संवेदनशीलता, प्रेम और भावनात्मक संतुलन"
            TarotSuit.SWORDS -> "स्वॉर्ड्स (विवेक)" to "बौद्धिक स्पष्टता, विवेक, सत्य और चिंतन"
            TarotSuit.PENTACLES -> "पेंटाकल्स (समृद्धि)" to "व्यावहारिक आधार, कर्म, स्वास्थ्य और भौतिक संतुलन"
        }

        val rankHindi = when (number) {
            1 -> "ऐस (प्रारंभ)"
            2 -> "2 (दो)"
            3 -> "3 (तीन)"
            4 -> "4 (चार)"
            5 -> "5 (पाँच)"
            6 -> "6 (छह)"
            7 -> "7 (सात)"
            8 -> "8 (आठ)"
            9 -> "9 (नौ)"
            10 -> "10 (दस)"
            11 -> "पेज (शिष्य)"
            12 -> "नाइट (अभियान)"
            13 -> "क्वीन (गरिमा)"
            14 -> "किंग (अधिपति)"
            else -> number.toString()
        }

        val title = "$suitHindi का $rankHindi"
        val desc = "$suitDomain का चिंतनशील प्रतीक।"
        val keywords = when (suit) {
            TarotSuit.WANDS -> listOf("प्रेरणा", "ऊर्जा", "उत्साह", "सृजन")
            TarotSuit.CUPS -> listOf("संवेदना", "सद्भाव", "प्रेम", "शांति")
            TarotSuit.SWORDS -> listOf("विवेक", "स्पष्टता", "सत्य", "विचार")
            TarotSuit.PENTACLES -> listOf("कर्म", "धैर्य", "स्थिरता", "समृद्धि")
        }

        val upright =
            "$title के सकारात्मक गुणों को अपनाएं। $suitDomain में संतुलन और स्पष्टता बनाए रखें।"
        val reversed =
            "जाँचें कि क्या $suitDomain में अति या उपेक्षा हो रही है। स्वाभाविक संतुलन पुनः स्थापित करें।"

        return TarotCardContent(
            cardId = cardId,
            language = "hi",
            title = title,
            shortDescription = desc,
            keywords = keywords,
            uprightMeaning = upright,
            reversedMeaning = reversed,
            sourceAttribution = DEFAULT_ATTRIBUTION,
            contentVersion = CURRENT_CONTENT_VERSION,
        )
    }

    private data class Quadruple(
        val desc: String,
        val keywords: List<String>,
        val upright: String,
        val reversed: String,
    )

    private data class Quintuple(
        val title: String,
        val desc: String,
        val keywords: List<String>,
        val upright: String,
        val reversed: String,
    )
}
