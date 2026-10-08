package com.aynvora.numerology

import com.aynvora.contracts.AynvoraResult

/**
 * Pure, deterministic, platform-independent Numerology Calculation Engine.
 *
 * Implements strict source-gated calculation algorithms without external dependencies,
 * UI text, or platform-specific APIs.
 *
 * Core Supported Traditions:
 * 1. Chaldean / Cheiro (CHALDEAN_CHEIRO_V1):
 *    - Cheiro's Book of Numbers (Count Louis Hamon, 1926)
 *    - Ank Jyotish tradition (Moolank, Bhagyank, Namank 1–8)
 * 2. Western Pythagorean (PYTHAGOREAN_WESTERN_V1):
 *    - Matthew Oliver Goodwin, "Numerology: The Complete Guide" (1981)
 *    - Florence Campbell, "Your Days Are Numbered" (1931)
 */
object NumerologyCalculationEngine {

    /**
     * Executes calculation for a given [NumerologyRequest] and returns a [NumerologyResult].
     */
    fun calculate(request: NumerologyRequest): AynvoraResult<NumerologyResult> {
        // 1. Resolve ruleset
        val ruleset = NumerologyRuleset.fromId(request.rulesetId)
            ?: return AynvoraResult.Failure.UnsupportedConfiguration(
                "Unsupported numerology ruleset ID: ${request.rulesetId}. Supported: ${NumerologyRuleset.ALL_RULESETS.map { it.id }}"
            )

        // 2. Validate name/text input if provided (ruleset-aware script validation)
        if (!request.fullName.isNullOrBlank()) {
            val nameValidation = NumerologyAlphabet.validateNameInput(request.fullName, ruleset.id)
            if (nameValidation != null) {
                return AynvoraResult.Failure.InvalidInput("fullName", nameValidation)
            }
        }

        val traces = mutableMapOf<NumerologyCalculationType, NumerologyCalculationTrace>()
        val dateDisplay = formatBirthDate(request.birthDay, request.birthMonth, request.birthYear)

        // 3. Handle Classical Hebrew Gematria discipline (Ragil and Gadol variants)
        if (ruleset.id == NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id || ruleset.id == NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id) {
            if (request.fullName.isNullOrBlank()) {
                return AynvoraResult.Failure.InvalidInput(
                    "fullName",
                    "Classical Hebrew Gematria requires text input in the fullName field."
                )
            }
            val (gematriaRes, gematriaTrace) = GematriaEngine.calculate(
                request.fullName,
                ruleset.id
            )
            traces[NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE] = gematriaTrace

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = null,
                gematria = gematriaRes,
                abjad = null,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    gematriaResult = gematriaRes,
                )
            )
        }

        // 4. Handle Arabic Hisab al-Jummal discipline (Mashriqi and Maghribi variants)
        if (ruleset.id == NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id || ruleset.id == NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id) {
            if (request.fullName.isNullOrBlank()) {
                return AynvoraResult.Failure.InvalidInput(
                    "fullName",
                    "Arabic Hisab al-Jummal requires text input in the fullName field."
                )
            }
            val (abjadRes, abjadTrace) = AbjadEngine.calculate(request.fullName, ruleset.id)
            traces[NumerologyCalculationType.ABJAD_KABIR_VALUE] = abjadTrace

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = null,
                gematria = null,
                abjad = abjadRes,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    abjadResult = abjadRes,
                )
            )
        }

        // 5. Handle independent Lo Shu Magic Square tradition
        if (ruleset.id == NumerologyRuleset.LO_SHU_CLASSICAL_V1.id) {
            val dateValidation =
                validateDate(request.birthDay, request.birthMonth, request.birthYear)
            if (dateValidation != null) {
                return AynvoraResult.Failure.InvalidInput("birthDate", dateValidation)
            }

            val (grid, trace) = LoShuEngine.calculateGrid(
                request.birthDay,
                request.birthMonth,
                request.birthYear
            )
            traces[NumerologyCalculationType.LO_SHU_GRID] = trace

            val loShuResult = LoShuResult(
                birthDateDisplay = dateDisplay,
                grid = grid,
                rulesetId = ruleset.id,
                trace = trace,
            )

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = grid,
                gematria = null,
                abjad = null,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    loShuResult = loShuResult,
                )
            )
        }

        // 5b. Handle Indian Katapayadi system
        if (ruleset.id == NumerologyRuleset.INDIAN_KATAPAYADI_V1.id) {
            if (request.fullName.isNullOrBlank()) {
                return AynvoraResult.Failure.InvalidInput(
                    "fullName",
                    "Indian Katapayadi system requires text input in the fullName field."
                )
            }
            val (katapayadiRes, katapayadiTrace) = KatapayadiEngine.calculate(
                request.fullName,
                ruleset.id
            )
            traces[NumerologyCalculationType.KATAPAYADI_CODING] = katapayadiTrace

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = null,
                gematria = null,
                abjad = null,
                katapayadi = katapayadiRes,
                nineStarKi = null,
                tarotBirthCard = null,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    katapayadiResult = katapayadiRes,
                )
            )
        }

        // 5c. Handle Chinese Nine Star Ki
        if (ruleset.id == NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id) {
            val dateValidation =
                validateDate(request.birthDay, request.birthMonth, request.birthYear)
            if (dateValidation != null) {
                return AynvoraResult.Failure.InvalidInput("birthDate", dateValidation)
            }

            val (nineStarRes, nineStarTrace) = NineStarKiEngine.calculate(
                year = request.birthYear,
                month = request.birthMonth,
                day = request.birthDay,
                rulesetId = ruleset.id,
            )
            traces[NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL] = nineStarTrace

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = null,
                gematria = null,
                abjad = null,
                katapayadi = null,
                nineStarKi = nineStarRes,
                tarotBirthCard = null,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    nineStarKiResult = nineStarRes,
                )
            )
        }

        // 5d. Handle Tarot Birth Cards
        if (ruleset.id == NumerologyRuleset.TAROT_BIRTH_CARD_V1.id) {
            val dateValidation =
                validateDate(request.birthDay, request.birthMonth, request.birthYear)
            if (dateValidation != null) {
                return AynvoraResult.Failure.InvalidInput("birthDate", dateValidation)
            }

            val (tarotRes, tarotTrace) = TarotBirthCardEngine.calculate(
                birthDay = request.birthDay,
                birthMonth = request.birthMonth,
                birthYear = request.birthYear,
                rulesetId = ruleset.id,
            )
            traces[NumerologyCalculationType.TAROT_BIRTH_CARD] = tarotTrace

            val profile = NumerologyProfile(
                birthDateDisplay = dateDisplay,
                radical = null,
                destiny = null,
                nameNumber = null,
                soulUrge = null,
                personality = null,
                personalYears = emptyList(),
                personalMonths = emptyList(),
                pinnacles = emptyList(),
                combinations = emptyList(),
                loShu = null,
                gematria = null,
                abjad = null,
                katapayadi = null,
                nineStarKi = null,
                tarotBirthCard = tarotRes,
                planetaryAssociation = null,
                rulesetId = ruleset.id,
                calculationTraces = traces,
                engineStatus = NumerologyEngineStatus.IMPLEMENTED,
            )

            return AynvoraResult.Success(
                NumerologyResult(
                    profile = profile,
                    tarotBirthCardResult = tarotRes,
                )
            )
        }

        // 6. Validate birth date for calendar-based rulesets
        val dateValidation = validateDate(request.birthDay, request.birthMonth, request.birthYear)
        if (dateValidation != null) {
            return AynvoraResult.Failure.InvalidInput("birthDate", dateValidation)
        }

        // 7. Radical Number (Moolank / Birth Day)
        var radical: RadicalNumber? = null
        if (ruleset.supportedCalculations.contains(NumerologyCalculationType.RADICAL_NUMBER)) {
            val (rad, radicalTrace) = calculateRadical(request.birthDay, ruleset)
            radical = rad
            traces[NumerologyCalculationType.RADICAL_NUMBER] = radicalTrace
        }

        // 8. Destiny Number (Bhagyank / Life Path)
        var destiny: DestinyNumber? = null
        if (ruleset.supportedCalculations.contains(NumerologyCalculationType.DESTINY_NUMBER)) {
            val (dest, destinyTrace) = calculateDestiny(
                request.birthDay,
                request.birthMonth,
                request.birthYear,
                ruleset
            )
            destiny = dest
            traces[NumerologyCalculationType.DESTINY_NUMBER] = destinyTrace
        }

        // 7. Name Number (Expression) if name provided
        var nameNumber: NameNumber? = null
        var soulUrge: SoulUrgeNumber? = null
        var personality: PersonalityNumber? = null

        if (!request.fullName.isNullOrBlank()) {
            val (name, nameTrace) = calculateName(request.fullName, ruleset)
            nameNumber = name
            traces[NumerologyCalculationType.NAME_NUMBER] = nameTrace

            if (ruleset.supportedCalculations.contains(NumerologyCalculationType.SOUL_URGE_NUMBER)) {
                val (su, suTrace) = calculateSoulUrge(request.fullName, ruleset)
                soulUrge = su
                traces[NumerologyCalculationType.SOUL_URGE_NUMBER] = suTrace
            }

            if (ruleset.supportedCalculations.contains(NumerologyCalculationType.PERSONALITY_NUMBER)) {
                val (pers, persTrace) = calculatePersonality(request.fullName, ruleset)
                personality = pers
                traces[NumerologyCalculationType.PERSONALITY_NUMBER] = persTrace
            }
        }

        // 8. Personal Year & Month
        val personalYears = mutableListOf<PersonalYear>()
        val personalMonths = mutableListOf<PersonalMonth>()

        if (ruleset.supportedCalculations.contains(NumerologyCalculationType.PERSONAL_YEAR) && request.targetYear != null) {
            val (py, pyTrace) = calculatePersonalYear(
                birthDay = request.birthDay,
                birthMonth = request.birthMonth,
                targetYear = request.targetYear,
                ruleset = ruleset,
            )
            personalYears.add(py)
            traces[NumerologyCalculationType.PERSONAL_YEAR] = pyTrace

            if (ruleset.supportedCalculations.contains(NumerologyCalculationType.PERSONAL_MONTH) && request.targetMonth != null) {
                if (request.targetMonth in 1..12) {
                    val (pm, pmTrace) = calculatePersonalMonth(
                        personalYear = py.personalYearValue,
                        targetYear = request.targetYear,
                        targetMonth = request.targetMonth,
                        ruleset = ruleset,
                    )
                    personalMonths.add(pm)
                    traces[NumerologyCalculationType.PERSONAL_MONTH] = pmTrace
                }
            }
        }

        // 9. Pinnacles (Pythagorean 4 Pinnacles)
        val pinnacles = mutableListOf<Pinnacle>()
        if (ruleset.supportedCalculations.contains(NumerologyCalculationType.PINNACLE_CYCLES)) {
            val (pinnacleList, pinnacleTrace) = calculatePinnacles(
                birthDay = request.birthDay,
                birthMonth = request.birthMonth,
                birthYear = request.birthYear,
                destinyValue = destiny?.destinyValue ?: 0,
                ruleset = ruleset,
            )
            pinnacles.addAll(pinnacleList)
            traces[NumerologyCalculationType.PINNACLE_CYCLES] = pinnacleTrace
        }

        // 10. Radical & Destiny Combinations (Cheiro / Ank Jyotish tradition)
        val combinations = mutableListOf<NumerologyCombination>()
        if (radical != null && destiny != null) {
            val combination =
                calculateCombination(radical.radicalValue, destiny.destinyValue, ruleset.id)
            combinations.add(combination)
        }

        val planetaryAssoc = if (radical != null) {
            NumerologyPlanetaryRegistry.getAssociation(radical.radicalValue, ruleset.id)
        } else null

        val profile = NumerologyProfile(
            birthDateDisplay = dateDisplay,
            radical = radical,
            destiny = destiny,
            nameNumber = nameNumber,
            soulUrge = soulUrge,
            personality = personality,
            personalYears = personalYears,
            personalMonths = personalMonths,
            pinnacles = pinnacles,
            combinations = combinations,
            loShu = null,
            planetaryAssociation = planetaryAssoc,
            rulesetId = ruleset.id,
            calculationTraces = traces,
            engineStatus = NumerologyEngineStatus.IMPLEMENTED,
        )

        return AynvoraResult.Success(NumerologyResult(profile = profile))
    }

    // ========================================================================
    // CALCULATION LOGIC
    // ========================================================================

    fun calculateRadical(
        day: Int,
        ruleset: NumerologyRuleset
    ): Pair<RadicalNumber, NumerologyCalculationTrace> {
        val reduction = NumerologyReductionEngine.reduce(day, ruleset.masterNumberPolicy)
        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val steps = if (reduction.steps.isEmpty()) {
            listOf(
                ReductionStep(
                    stepNumber = 1,
                    startingValue = day,
                    digits = NumerologyReductionEngine.extractDigits(day),
                    reducedSum = reduction.finalValue,
                    equation = if (reduction.isMasterNumber) "$day (Master Number Preserved)" else "$day (Single Digit Root)",
                )
            )
        } else {
            reduction.steps
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.RADICAL_NUMBER,
            rulesetId = ruleset.id,
            rawInput = day.toString(),
            normalizedInput = day.toString(),
            compoundSum = day,
            reductionSteps = steps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val radical = RadicalNumber(
            rawDayOfBirth = day,
            radicalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_RADICAL",
        )

        return Pair(radical, trace)
    }

    fun calculateDestiny(
        day: Int,
        month: Int,
        year: Int,
        ruleset: NumerologyRuleset,
    ): Pair<DestinyNumber, NumerologyCalculationTrace> {
        val rawInput = "$day-$month-$year"

        val (finalValue, initialSum, isMaster, steps) = when (ruleset.dateReductionMethod) {
            ReductionMethod.DIGIT_SUM -> {
                val dayDigits = NumerologyReductionEngine.extractDigits(day)
                val monthDigits = NumerologyReductionEngine.extractDigits(month)
                val yearDigits = NumerologyReductionEngine.extractDigits(year)
                val sum = dayDigits.sum() + monthDigits.sum() + yearDigits.sum()

                val reduction = NumerologyReductionEngine.reduce(sum, ruleset.masterNumberPolicy)
                val allSteps = mutableListOf<ReductionStep>()
                allSteps.add(
                    ReductionStep(
                        stepNumber = 1,
                        startingValue = sum,
                        digits = dayDigits + monthDigits + yearDigits,
                        reducedSum = sum,
                        equation = "${(dayDigits + monthDigits + yearDigits).joinToString(" + ")} = $sum",
                    )
                )
                reduction.steps.forEachIndexed { idx, s ->
                    allSteps.add(s.copy(stepNumber = idx + 2))
                }
                Tuple4(reduction.finalValue, sum, reduction.isMasterNumber, allSteps)
            }

            ReductionMethod.COMPONENT_THEN_SUM -> {
                val mRed = NumerologyReductionEngine.reduce(month, ruleset.masterNumberPolicy)
                val dRed = NumerologyReductionEngine.reduce(day, ruleset.masterNumberPolicy)
                val yRed = NumerologyReductionEngine.reduce(year, ruleset.masterNumberPolicy)

                val sum = mRed.finalValue + dRed.finalValue + yRed.finalValue
                val reduction = NumerologyReductionEngine.reduce(sum, ruleset.masterNumberPolicy)

                val allSteps = mutableListOf<ReductionStep>()
                allSteps.add(
                    ReductionStep(
                        stepNumber = 1,
                        startingValue = sum,
                        digits = listOf(mRed.finalValue, dRed.finalValue, yRed.finalValue),
                        reducedSum = sum,
                        equation = "${mRed.finalValue} + ${dRed.finalValue} + ${yRed.finalValue} = $sum",
                    )
                )
                reduction.steps.forEachIndexed { idx, s ->
                    allSteps.add(s.copy(stepNumber = idx + 2))
                }
                Tuple4(reduction.finalValue, sum, reduction.isMasterNumber, allSteps)
            }

            ReductionMethod.FULL_INTEGER_SUM -> {
                val sum = day + month + year
                val reduction = NumerologyReductionEngine.reduce(sum, ruleset.masterNumberPolicy)
                val allSteps = mutableListOf<ReductionStep>()
                allSteps.add(
                    ReductionStep(
                        stepNumber = 1,
                        startingValue = sum,
                        digits = listOf(day, month, year),
                        reducedSum = sum,
                        equation = "$day + $month + $year = $sum",
                    )
                )
                reduction.steps.forEachIndexed { idx, s ->
                    allSteps.add(s.copy(stepNumber = idx + 2))
                }
                Tuple4(reduction.finalValue, sum, reduction.isMasterNumber, allSteps)
            }
        }

        val planet = getPlanetaryRuler(finalValue, ruleset.id)

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.DESTINY_NUMBER,
            rulesetId = ruleset.id,
            rawInput = rawInput,
            normalizedInput = "$day/$month/$year",
            compoundSum = initialSum,
            reductionSteps = steps,
            finalValue = finalValue,
            isMasterNumber = isMaster,
        )

        val destiny = DestinyNumber(
            rawSum = initialSum,
            destinyValue = finalValue,
            isMasterNumber = isMaster,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_DESTINY",
        )

        return Pair(destiny, trace)
    }

    fun calculateName(
        fullName: String,
        ruleset: NumerologyRuleset
    ): Pair<NameNumber, NumerologyCalculationTrace> {
        val normalized = NumerologyAlphabet.normalizeName(fullName)
        val values: List<Int> = normalized.map { c ->
            when (ruleset.nameNumberSystem) {
                NameNumberSystem.CHALDEAN -> NumerologyAlphabet.getChaldeanValue(c)
                NameNumberSystem.PYTHAGOREAN -> NumerologyAlphabet.getPythagoreanValue(c)
                NameNumberSystem.AGRIPPAN -> NumerologyAlphabet.getAgrippanValue(c)
                NameNumberSystem.HEBREW_GEMATRIA -> GematriaEngine.getHebrewLetterValue(c)
                NameNumberSystem.ARABIC_ABJAD -> AbjadEngine.getArabicLetterValue(c)
                NameNumberSystem.KATAPAYADI -> 0
            }
        }

        val initialSum: Int = values.sum()
        val reduction = NumerologyReductionEngine.reduce(
            if (initialSum > 0) initialSum else 1,
            ruleset.masterNumberPolicy
        )

        val allSteps = mutableListOf<ReductionStep>()
        allSteps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = initialSum,
                digits = values,
                reducedSum = initialSum,
                equation = "${values.joinToString(" + ")} = $initialSum",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            allSteps.add(s.copy(stepNumber = idx + 2))
        }

        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.NAME_NUMBER,
            rulesetId = ruleset.id,
            rawInput = fullName,
            normalizedInput = normalized,
            letterValues = values,
            compoundSum = initialSum,
            reductionSteps = allSteps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val nameNumber = NameNumber(
            fullName = normalized,
            nameValue = reduction.finalValue,
            system = ruleset.nameNumberSystem,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_NAME_${ruleset.nameNumberSystem.name}",
        )

        return Pair(nameNumber, trace)
    }

    fun calculateSoulUrge(
        fullName: String,
        ruleset: NumerologyRuleset
    ): Pair<SoulUrgeNumber, NumerologyCalculationTrace> {
        val normalized = NumerologyAlphabet.normalizeName(fullName)
        val vowels = normalized.filter { NumerologyAlphabet.isVowel(it) }
        val values: List<Int> = vowels.map { c ->
            when (ruleset.nameNumberSystem) {
                NameNumberSystem.CHALDEAN -> NumerologyAlphabet.getChaldeanValue(c)
                NameNumberSystem.PYTHAGOREAN -> NumerologyAlphabet.getPythagoreanValue(c)
                NameNumberSystem.AGRIPPAN -> NumerologyAlphabet.getAgrippanValue(c)
                NameNumberSystem.HEBREW_GEMATRIA -> GematriaEngine.getHebrewLetterValue(c)
                NameNumberSystem.ARABIC_ABJAD -> AbjadEngine.getArabicLetterValue(c)
                NameNumberSystem.KATAPAYADI -> 0
            }
        }

        val initialSum: Int = values.sum()
        val reduction = NumerologyReductionEngine.reduce(
            if (initialSum > 0) initialSum else 1,
            ruleset.masterNumberPolicy
        )

        val allSteps = mutableListOf<ReductionStep>()
        allSteps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = initialSum,
                digits = values,
                reducedSum = initialSum,
                equation = "${values.joinToString(" + ")} = $initialSum",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            allSteps.add(s.copy(stepNumber = idx + 2))
        }

        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.SOUL_URGE_NUMBER,
            rulesetId = ruleset.id,
            rawInput = fullName,
            normalizedInput = vowels,
            letterValues = values,
            compoundSum = initialSum,
            reductionSteps = allSteps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val soulUrge = SoulUrgeNumber(
            fullName = normalized,
            soulUrgeValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_SOUL_URGE",
        )

        return Pair(soulUrge, trace)
    }

    fun calculatePersonality(
        fullName: String,
        ruleset: NumerologyRuleset
    ): Pair<PersonalityNumber, NumerologyCalculationTrace> {
        val normalized = NumerologyAlphabet.normalizeName(fullName)
        val consonants = normalized.filter { !NumerologyAlphabet.isVowel(it) }
        val values: List<Int> = consonants.map { c ->
            when (ruleset.nameNumberSystem) {
                NameNumberSystem.CHALDEAN -> NumerologyAlphabet.getChaldeanValue(c)
                NameNumberSystem.PYTHAGOREAN -> NumerologyAlphabet.getPythagoreanValue(c)
                NameNumberSystem.AGRIPPAN -> NumerologyAlphabet.getAgrippanValue(c)
                NameNumberSystem.HEBREW_GEMATRIA -> GematriaEngine.getHebrewLetterValue(c)
                NameNumberSystem.ARABIC_ABJAD -> AbjadEngine.getArabicLetterValue(c)
                NameNumberSystem.KATAPAYADI -> 0
            }
        }

        val initialSum: Int = values.sum()
        val reduction = NumerologyReductionEngine.reduce(
            if (initialSum > 0) initialSum else 1,
            ruleset.masterNumberPolicy
        )

        val allSteps = mutableListOf<ReductionStep>()
        allSteps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = initialSum,
                digits = values,
                reducedSum = initialSum,
                equation = "${values.joinToString(" + ")} = $initialSum",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            allSteps.add(s.copy(stepNumber = idx + 2))
        }

        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.PERSONALITY_NUMBER,
            rulesetId = ruleset.id,
            rawInput = fullName,
            normalizedInput = consonants,
            letterValues = values,
            compoundSum = initialSum,
            reductionSteps = allSteps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val personality = PersonalityNumber(
            fullName = normalized,
            personalityValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_PERSONALITY",
        )

        return Pair(personality, trace)
    }

    fun calculatePersonalYear(
        birthDay: Int,
        birthMonth: Int,
        targetYear: Int,
        ruleset: NumerologyRuleset,
    ): Pair<PersonalYear, NumerologyCalculationTrace> {
        val mRed = NumerologyReductionEngine.reduce(birthMonth, ruleset.masterNumberPolicy)
        val dRed = NumerologyReductionEngine.reduce(birthDay, ruleset.masterNumberPolicy)
        val yRed = NumerologyReductionEngine.reduce(targetYear, ruleset.masterNumberPolicy)

        val initialSum = mRed.finalValue + dRed.finalValue + yRed.finalValue
        val reduction = NumerologyReductionEngine.reduce(initialSum, ruleset.masterNumberPolicy)
        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val allSteps = mutableListOf<ReductionStep>()
        allSteps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = initialSum,
                digits = listOf(mRed.finalValue, dRed.finalValue, yRed.finalValue),
                reducedSum = initialSum,
                equation = "${mRed.finalValue} + ${dRed.finalValue} + ${yRed.finalValue} = $initialSum",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            allSteps.add(s.copy(stepNumber = idx + 2))
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.PERSONAL_YEAR,
            rulesetId = ruleset.id,
            rawInput = "Day:$birthDay, Month:$birthMonth, TargetYear:$targetYear",
            normalizedInput = "$birthDay/$birthMonth/$targetYear",
            compoundSum = initialSum,
            reductionSteps = allSteps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val personalYear = PersonalYear(
            targetYear = targetYear,
            personalYearValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_PERSONAL_YEAR",
        )

        return Pair(personalYear, trace)
    }

    fun calculatePersonalMonth(
        personalYear: Int,
        targetYear: Int,
        targetMonth: Int,
        ruleset: NumerologyRuleset,
    ): Pair<PersonalMonth, NumerologyCalculationTrace> {
        val initialSum = personalYear + targetMonth
        val reduction = NumerologyReductionEngine.reduce(initialSum, ruleset.masterNumberPolicy)
        val planet = getPlanetaryRuler(reduction.finalValue, ruleset.id)

        val allSteps = mutableListOf<ReductionStep>()
        allSteps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = initialSum,
                digits = listOf(personalYear, targetMonth),
                reducedSum = initialSum,
                equation = "$personalYear + $targetMonth = $initialSum",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            allSteps.add(s.copy(stepNumber = idx + 2))
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.PERSONAL_MONTH,
            rulesetId = ruleset.id,
            rawInput = "PersonalYear:$personalYear, TargetMonth:$targetMonth",
            normalizedInput = "PY:$personalYear + TM:$targetMonth",
            compoundSum = initialSum,
            reductionSteps = allSteps,
            finalValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
        )

        val personalMonth = PersonalMonth(
            targetYear = targetYear,
            targetMonth = targetMonth,
            personalMonthValue = reduction.finalValue,
            isMasterNumber = reduction.isMasterNumber,
            rulingPlanet = planet,
            sourceRule = "${ruleset.id}_PERSONAL_MONTH",
        )

        return Pair(personalMonth, trace)
    }

    fun calculatePinnacles(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        destinyValue: Int,
        ruleset: NumerologyRuleset,
    ): Pair<List<Pinnacle>, NumerologyCalculationTrace> {
        val dRoot = NumerologyReductionEngine.reduceToRoot(birthDay)
        val mRoot = NumerologyReductionEngine.reduceToRoot(birthMonth)
        val yRoot = NumerologyReductionEngine.reduceToRoot(birthYear)
        val lpRoot = NumerologyReductionEngine.reduceToRoot(destinyValue)

        val p1EndAge = 36 - lpRoot
        val p2StartAge = p1EndAge + 1
        val p2EndAge = p1EndAge + 9
        val p3StartAge = p2EndAge + 1
        val p3EndAge = p2EndAge + 9
        val p4StartAge = p3EndAge + 1

        val p1Red = NumerologyReductionEngine.reduce(mRoot + dRoot, ruleset.masterNumberPolicy)
        val p2Red = NumerologyReductionEngine.reduce(dRoot + yRoot, ruleset.masterNumberPolicy)
        val p3Red = NumerologyReductionEngine.reduce(
            p1Red.finalValue + p2Red.finalValue,
            ruleset.masterNumberPolicy
        )
        val p4Red = NumerologyReductionEngine.reduce(mRoot + yRoot, ruleset.masterNumberPolicy)

        val pinnacles = listOf(
            Pinnacle(
                pinnacleOrder = 1,
                pinnacleValue = p1Red.finalValue,
                isMasterNumber = p1Red.isMasterNumber,
                startAge = 0,
                endAge = p1EndAge,
                sourceRule = "GOODWIN_PINNACLE_1",
            ),
            Pinnacle(
                pinnacleOrder = 2,
                pinnacleValue = p2Red.finalValue,
                isMasterNumber = p2Red.isMasterNumber,
                startAge = p2StartAge,
                endAge = p2EndAge,
                sourceRule = "GOODWIN_PINNACLE_2",
            ),
            Pinnacle(
                pinnacleOrder = 3,
                pinnacleValue = p3Red.finalValue,
                isMasterNumber = p3Red.isMasterNumber,
                startAge = p3StartAge,
                endAge = p3EndAge,
                sourceRule = "GOODWIN_PINNACLE_3",
            ),
            Pinnacle(
                pinnacleOrder = 4,
                pinnacleValue = p4Red.finalValue,
                isMasterNumber = p4Red.isMasterNumber,
                startAge = p4StartAge,
                endAge = null,
                sourceRule = "GOODWIN_PINNACLE_4",
            ),
        )

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.PINNACLE_CYCLES,
            rulesetId = ruleset.id,
            rawInput = "$birthDay-$birthMonth-$birthYear",
            normalizedInput = "D:$dRoot, M:$mRoot, Y:$yRoot, LP:$lpRoot",
            compoundSum = p1Red.finalValue + p2Red.finalValue + p3Red.finalValue + p4Red.finalValue,
            reductionSteps = listOf(
                ReductionStep(
                    1,
                    mRoot + dRoot,
                    listOf(mRoot, dRoot),
                    p1Red.finalValue,
                    "M($mRoot) + D($dRoot) = ${p1Red.finalValue}"
                ),
                ReductionStep(
                    2,
                    dRoot + yRoot,
                    listOf(dRoot, yRoot),
                    p2Red.finalValue,
                    "D($dRoot) + Y($yRoot) = ${p2Red.finalValue}"
                ),
                ReductionStep(
                    3,
                    p1Red.finalValue + p2Red.finalValue,
                    listOf(p1Red.finalValue, p2Red.finalValue),
                    p3Red.finalValue,
                    "P1(${p1Red.finalValue}) + P2(${p2Red.finalValue}) = ${p3Red.finalValue}"
                ),
                ReductionStep(
                    4,
                    mRoot + yRoot,
                    listOf(mRoot, yRoot),
                    p4Red.finalValue,
                    "M($mRoot) + Y($yRoot) = ${p4Red.finalValue}"
                ),
            ),
            finalValue = p1Red.finalValue,
            isMasterNumber = false,
        )

        return Pair(pinnacles, trace)
    }

    fun calculateCombination(
        radicalValue: Int,
        destinyValue: Int,
        rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
    ): NumerologyCombination {
        val rRoot = NumerologyReductionEngine.reduceToRoot(radicalValue)
        val dRoot = NumerologyReductionEngine.reduceToRoot(destinyValue)

        val relationship = evaluatePlanetaryFriendship(rRoot, dRoot)
        val planetR = getPlanetaryRuler(rRoot, rulesetId)
        val planetD = getPlanetaryRuler(dRoot, rulesetId)

        val sourceRule = if (rulesetId == NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id) {
            "KATAKKAR_SETHURAMAN_PANCHADHA_MAITRI"
        } else {
            "CHEIRO_PLANETARY_FRIENDSHIP_MATRIX"
        }

        return NumerologyCombination(
            radicalValue = radicalValue,
            destinyValue = destinyValue,
            relationshipType = relationship,
            planetaryRelationship = "$planetR ($rRoot) and $planetD ($dRoot)",
            sourceRule = sourceRule,
        )
    }

    private fun evaluatePlanetaryFriendship(r: Int, d: Int): NumerologyRelationshipType {
        if (r == d) return NumerologyRelationshipType.FRIENDLY

        val friends = when (r) {
            1 -> setOf(1, 2, 3, 9)
            2 -> setOf(1, 2, 3)
            3 -> setOf(1, 2, 3, 9)
            4 -> setOf(5, 6, 7)
            5 -> setOf(1, 5, 6)
            6 -> setOf(4, 5, 6, 7, 8)
            7 -> setOf(4, 6, 7)
            8 -> setOf(4, 5, 6)
            9 -> setOf(1, 2, 3)
            else -> emptySet()
        }

        val neutrals = when (r) {
            1 -> setOf(5)
            2 -> setOf(7, 9)
            3 -> setOf(5, 7)
            4 -> setOf(1, 8)
            5 -> setOf(3, 4, 8, 9)
            6 -> setOf(3, 9)
            7 -> setOf(2, 3, 5)
            8 -> setOf(3, 7)
            9 -> setOf(5)
            else -> emptySet()
        }

        return when {
            friends.contains(d) -> NumerologyRelationshipType.FRIENDLY
            neutrals.contains(d) -> NumerologyRelationshipType.NEUTRAL
            else -> NumerologyRelationshipType.CHALLENGING
        }
    }

    fun getPlanetaryRuler(number: Int, rulesetId: String = ""): String {
        if (rulesetId == NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id) {
            val root = NumerologyReductionEngine.reduceToRoot(number)
            return when (root) {
                1 -> "Surya (Sun)"
                2 -> "Chandra (Moon)"
                3 -> "Guru / Brihaspati (Jupiter)"
                4 -> "Rahu (North Node)"
                5 -> "Budha (Mercury)"
                6 -> "Shukra (Venus)"
                7 -> "Ketu (South Node)"
                8 -> "Shani (Saturn)"
                9 -> "Mangal (Mars)"
                else -> "Unknown"
            }
        }
        return when (number) {
            1 -> "Sun"
            2 -> "Moon"
            3 -> "Jupiter"
            4 -> "Rahu / Uranus"
            5 -> "Mercury"
            6 -> "Venus"
            7 -> "Ketu / Neptune"
            8 -> "Saturn"
            9 -> "Mars"
            11 -> "Master 11 (Moon Octave)"
            22 -> "Master 22 (Rahu Octave)"
            33 -> "Master 33 (Venus Octave)"
            else -> "Unknown"
        }
    }

    // ========================================================================
    // CALENDAR VALIDATION & HELPERS
    // ========================================================================

    fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun daysInMonth(month: Int, year: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 0
    }

    fun validateDate(day: Int, month: Int, year: Int): String? {
        if (year <= 0 || year > 9999) return "Year must be between 1 and 9999 (received: $year)"
        if (month !in 1..12) return "Month must be between 1 and 12 (received: $month)"
        val maxDays = daysInMonth(month, year)
        if (day < 1 || day > maxDays) {
            return "Invalid day $day for month $month in year $year (expected 1..$maxDays)"
        }
        return null
    }

    private fun formatBirthDate(day: Int, month: Int, year: Int): String {
        val dStr = if (day < 10) "0$day" else "$day"
        val mStr = if (month < 10) "0$month" else "$month"
        return "$dStr-$mStr-$year"
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
