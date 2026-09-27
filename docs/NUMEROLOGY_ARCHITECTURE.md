# AYNVORA Numerology Domain Architecture & Foundation Specification

**Phase 10.0 — Production-Grade, Source-Gated, Deterministic, Offline-First KMP**

---

## 1. Architectural Principles

Numerology in AYNVORA is established as an independent, first-class core domain adhering strictly to
Clean Architecture, offline-first execution, and zero-compromise privacy.

- **Independent Domain**: Lives purely in `:aynvora-core` (`com.aynvora.core.numerology`) and
  `:aynvora-data` (`com.aynvora.data.numerology`).
- **Zero Cross-Contamination**: Completely decoupled from `:astro-engine` (astrological formulas),
  UI/Compose, Room DAOs, network layers, and Firebase.
- **Source-Gated Truth**: All calculations derive from documented historical authorities (Count
  Louis Hamon / Cheiro, Matthew Oliver Goodwin, Florence Campbell). No formula is invented from
  model memory.
- **Traceable Explainability**: Every calculation produces a structured, immutable
  `NumerologyCalculationTrace` documenting raw input, normalized input, intermediate sums, reduction
  steps, and final master/root resolutions.
- **AI/SLM Boundary**: Small Language Models consume structured `EvidenceGraph` nodes for
  explanation but are strictly prohibited from performing or overriding calculations.

---

## 2. Supported Rulesets & Authoritative Sources

| Ruleset ID                     | Tradition / Authority             | Primary Source                                                                                                  | Letter Mapping                                         | Master Numbers             | Date Reduction Method                                                  |
|--------------------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------|------------------------------------------------------------------------|
| `CHALDEAN_CHEIRO_V1`           | Chaldean / Cheiro Classical       | *Cheiro's Book of Numbers* (Count Louis Hamon, 1926), Ch. 1–5; Indian Ank Jyotish                               | 1–8 (9 sacred/omitted)                                 | 11, 22 preserved           | `DIGIT_SUM` (sum of all birth date digits)                             |
| `PYTHAGOREAN_WESTERN_V1`       | Western Pythagorean               | Matthew Oliver Goodwin, *Numerology: The Complete Guide* (1981); Florence Campbell (1931)                       | 1–9 sequential (A=1..Z=8)                              | 11, 22, 33 preserved       | `COMPONENT_THEN_SUM` (Month, Day, Year reduced separately then summed) |
| `INDIAN_ANK_JYOTISH_V1`        | Indian / Vedic Ank Jyotish        | Pandit Sethuraman, *Science of Fortune* (1954); Dr. M. Katakkar, *Miracles of Numerology* (1989)                | 1–8 sound vibration mapping (Latin-transliterated)     | Strictly 1..9 (Navagrahas) | `DIGIT_SUM` (Moolank: Day root, Bhagyank: Full date root)              |
| `LO_SHU_CLASSICAL_V1`          | Classical Lo Shu 3x3 Magic Square | *I Ching* (Luoshu Scroll, Zhou Dynasty); Dr. David A. Phillips, *The Complete Book of Numerology* (1992), Ch. 5 | N/A (Digit extraction from DOB)                        | N/A                        | 3x3 Magic Square placement (zero filtered out)                         |
| `HEBREW_GEMATRIA_CLASSICAL_V1` | Classical Hebrew Gematria (Ragil) | *Sefer Yetzirah* (2nd–6th c.); Rabbi Moses Cordovero, *Pardes Rimonim* (1591)                                   | Hebrew consonants (1..400 Ragil scale)                 | None (Root 1..9)           | N/A (Text alphanumeric equivalence)                                    |
| `HEBREW_MISPAR_GADOL_V1`       | Hebrew Gematria (Mispar Gadol)    | Rabbi Moses Cordovero, *Pardes Rimonim* (1591), Gate 30                                                         | Hebrew consonants (Final forms ך=500..ץ=900)           | None (Root 1..9)           | N/A (Text alphanumeric equivalence)                                    |
| `ARABIC_ABJAD_MASHRIQI_V1`     | Arabic Hisab al-Jummal (Mashriqi) | Ibn Khaldun, *The Muqaddimah* (1377 CE), Ch. 6; Ahmad al-Buni (c. 1225)                                         | Arabic consonants (1..1000 Kabir scale)                | None (Root 1..9)           | N/A (Text alphanumeric equivalence)                                    |
| `ARABIC_ABJAD_MAGHRIBI_V1`     | Arabic Hisab al-Jummal (Maghribi) | Ibn Khaldun, *The Muqaddimah* (1377 CE), Ch. 6, Sec. 28                                                         | Arabic consonants (Maghribi order: Sa'=60..Sheen=1000) | None (Root 1..9)           | N/A (Text alphanumeric equivalence)                                    |
| `AGRIPPAN_OCCULT_V1`           | Renaissance Agrippan Arithmancy   | Heinrich Cornelius Agrippa, *De Occulta Philosophia* (1533), Book II                                            | Latin consonants (1..500 scale, I=J=9, U=V=W=200)      | None (Root 1..9)           | N/A (Name arithmancy)                                                  |
| `INDIAN_KATAPAYADI_V1`         | Indian Katapayadi System          | Sankaravarman, *Sadratnamala* (1819 CE); Haridatta (683 CE)                                                     | Devanagari phonemes (Ka-Ta-Pa-Ya vargas 1..9, 0)       | None (Root 1..9)           | Digit extraction with *Ankānām Vāmato Gatiḥ* reversal                  |
| `CHINESE_NINE_STAR_KI_V1`      | Chinese Nine Star Ki              | *Xuan Kong Fei Xing*; Jean Meeus, *Astronomical Algorithms* (Li Chun 315°)                                      | N/A (Solar year Li Chun transition)                    | None (Cycle 1..9)          | Modulo 9 principal star: `11 - reduce(solarYear)`                      |
| `TAROT_BIRTH_CARD_V1`          | Modern Tarot Numerology           | Mary K. Greer, *Tarot for Your Self* (1984); Angeles Arrien (1987)                                              | N/A (Calendar date integer sum)                        | None                       | Full integer sum reduced to Major Arcana 1..22                         |

### Non-Interchangeability Rule

The 12 supported traditions span multiple distinct disciplines (`NUMEROLOGY`, `MAGIC_SQUARE`,
`GEMATRIA`, `ABJAD_ARITHMETIC`) and must **never** be silently mixed or averaged into a single
universal calculation. Calculations explicitly tag their results with their exact `rulesetId`.

---

## 3. Mathematical Calculations & Formulas

### 3.1 Radical Number (Moolank / Birth Day)

- **Authority**: Cheiro (1926) Ch. 2; Goodwin (1981) Vol 1.
- **Input**: Day of birth (1..31).
- **Formula**: `reduce(day, masterNumberPolicy)`.
- **Chaldean**: Preserves 11 and 22; otherwise reduces to single digit (1..9).
- **Pythagorean**: Preserves 11 and 22; otherwise reduces to single digit (1..9).
- **Ruling Planet Mapping**:
    - 1: Sun
    - 2: Moon
    - 3: Jupiter
    - 4: Rahu / Uranus
    - 5: Mercury
    - 6: Venus
    - 7: Ketu / Neptune
    - 8: Saturn
    - 9: Mars
    - 11: Master 11 (Moon Octave / Spiritual Messenger)
    - 22: Master 22 (Rahu Octave / Master Builder)
    - 33: Master 33 (Venus Octave / Master Teacher)

### 3.2 Destiny Number (Bhagyank / Life Path)

- **Authority**: Cheiro (1926); Goodwin (1981) Vol 1 Ch. 2.
- **Input**: Day, Month, Year.
- **Chaldean Method (`DIGIT_SUM`)**:
    - Sums all decimal digits of `DD + MM + YYYY`.
    - Example (11-07-1996): `1 + 1 + 0 + 7 + 1 + 9 + 9 + 6 = 34 -> 3 + 4 = 7`.
- **Pythagorean Method (`COMPONENT_THEN_SUM`)**:
    - Reduces Month, Day, and Year separately to root or master numbers:
        - `Month = reduce(month)` -> `7`
        - `Day = reduce(day)` -> `11` (Master preserved)
        - `Year = reduce(year)` -> `1996 -> 25 -> 7`
    - Sums reduced components: `7 + 11 + 7 = 25 -> 2 + 5 = 7`.

### 3.3 Name Number (Expression / Namank)

- **Input**: Full legal/birth name string.
- **Normalization**:
    - Strips spaces, punctuation, numbers, and symbols.
    - Converts European Latin diacritics to standard ASCII equivalents (e.g. `É -> E`, `ñ -> N`).
    - Uppercased to ASCII `A..Z`.
- **Chaldean Mapping (1–8)**:
    - `A, I, J, Q, Y` = 1
    - `B, K, R` = 2
    - `C, G, L, S` = 3
    - `D, M, T` = 4
    - `E, H, N, X` = 5
    - `U, V, W` = 6
    - `O, Z` = 7
    - `F, P` = 8
    - *(Number 9 is excluded from single letters as sacred in Chaldean tradition)*.
    - Example (`"ISHANT"`): `1 + 3 + 5 + 1 + 5 + 4 = 19 -> 10 -> 1`.
- **Pythagorean Mapping (1–9)**:
    - `A=1, B=2, C=3, D=4, E=5, F=6, G=7, H=8, I=9`
    - `J=1, K=2, L=3, M=4, N=5, O=6, P=7, Q=8, R=9`
    - `S=1, T=2, U=3, V=4, W=5, X=6, Y=7, Z=8`
    - Example (`"ISHANT"`): `9 + 1 + 8 + 1 + 5 + 2 = 26 -> 8`.

### 3.4 Soul Urge (Heart's Desire) & Personality Numbers

- **Supported Ruleset**: `PYTHAGOREAN_WESTERN_V1` (Goodwin 1981). *(Marked unsupported in Chaldean
  Cheiro ruleset)*.
- **Soul Urge**: Sum of vowels (`A, E, I, O, U`) reduced.
    - Example (`"ISHANT"`): Vowels `I(9) + A(1) = 10 -> 1`.
- **Personality Number**: Sum of consonants (`S, H, N, T`) reduced.
    - Example (`"ISHANT"`): `S(1) + H(8) + N(5) + T(2) = 16 -> 7`.
- **Pythagorean Mathematical Invariant**:
  $$\text{Root}(\text{SoulUrge}) + \text{Root}(\text{Personality}) = \text{Root}(\text{NameNumber})$$
  Example: `1 + 7 = 8` (Expression `8`). Verified across automated invariant suites.

### 3.5 Four Pinnacles (Life Cycle Periods)

- **Supported Ruleset**: `PYTHAGOREAN_WESTERN_V1` (Goodwin 1981 Vol 1 Ch. 8).
- **Roots**: Let $D$ = root day, $M$ = root month, $Y$ = root year, $LP$ = root Life Path.
    - **Pinnacle 1**: $\text{reduce}(M + D)$. Age: $0 \dots (36 - LP)$.
    - **Pinnacle 2**: $\text{reduce}(D + Y)$. Age: $(36 - LP + 1) \dots (36 - LP + 9)$.
    - **Pinnacle 3**: $\text{reduce}(P_1 + P_2)$. Age: $(36 - LP + 10) \dots (36 - LP + 18)$.
    - **Pinnacle 4**: $\text{reduce}(M + Y)$. Age: $(36 - LP + 19) \dots \text{end of life}$.
- **Verification Vector (11-07-1996, ISHANT)**:
    - $M=7, D=2, Y=7, LP=7$.
    - $P_1 = 7+2 = 9$ (Age 0..29)
    - $P_2 = 2+7 = 9$ (Age 30..38)
    - $P_3 = 9+9 = 18 \rightarrow 9$ (Age 39..47)
    - $P_4 = 7+7 = 14 \rightarrow 5$ (Age 48+)
    - Pinnacles: `[9, 9, 9, 5]`. Matches reference baseline.

### 3.6 Personal Year & Personal Month

- **Formula**:
    - Personal
      Year: $\text{reduce}(\text{reduce}(M) + \text{reduce}(D) + \text{reduce}(\text{TargetYear}))$.
    - Personal Month: $\text{reduce}(\text{PersonalYear} + \text{TargetMonth})$.

### 3.7 Radical & Destiny Planetary Combinations

- **Source**: Cheiro (1926) Ch. 24 & Indian Ank Jyotish planetary relationship matrix.
- Evaluates the dynamic between Radical root and Destiny root into `FRIENDLY`, `NEUTRAL`, or
  `CHALLENGING`.

### 3.8 Hebrew Gematria — Mispar Gadol Variant

- **Authority**: Rabbi Moses Cordovero, *Pardes Rimonim* (1591), Gate 30 (Sha'ar Ha-Tzeruf).
- **Ruleset**: `HEBREW_MISPAR_GADOL_V1`.
- **Final Consonants (Sofiyot)**:
    - Final Kaf (ך) = 500
    - Final Mem (ם) = 600
    - Final Nun (ן) = 700
    - Final Pe (ף) = 800
    - Final Tsadi (ץ) = 900
- **Contrast with Ragil**: In standard Ragil (`HEBREW_GEMATRIA_CLASSICAL_V1`), final letters retain
  base consonant values (ך=20, ם=40, ן=50, ף=80, ץ=90). In Gadol, they expand to hundreds (
  500..900). Example: "שלום" -> Ragil 376 vs Gadol 936.

### 3.9 Arabic Hisab al-Jummal — Maghribi Order Variant

- **Authority**: Ibn Khaldun, *The Muqaddimah* (1377 CE), Ch. 6, Sec. 28.
- **Ruleset**: `ARABIC_ABJAD_MAGHRIBI_V1`.
- **Maghribi Alphanumeric Mnemonic**: *Abjad, Hawwaz, Hutti, Kalaman, Sa'fadh, Qarast, Thakhadh,
  Zaghsh*.
- **Contrasting Values with Mashriqi**:
    - ص (Sa') = 60 (vs Mashriqi 90)
    - ض (Da) = 90 (vs Mashriqi 800)
    - س (Sin) = 300 (vs Mashriqi 60)
    - ظ (Zha) = 800 (vs Mashriqi 900)
    - غ (Ghayn) = 900 (vs Mashriqi 1000)
    - ش (Sheen) = 1000 (vs Mashriqi 300)
- Example: "شمس" -> Mashriqi 400 (300+40+60) vs Maghribi 1340 (1000+40+300).

### 3.10 Indian Katapayadi Numerical Mnemonic System

- **Authority**: Sankaravarman, *Sadratnamala* (1819 CE), Prakarana 1, Verses 3–5; Haridatta,
  *Grahacaranibandhana* (683 CE).
- **Ruleset**: `INDIAN_KATAPAYADI_V1`.
- **Core Principles**:
    - Consonants grouped into Ka, Ta, Pa, Ya vargas mapped to 1..9 and 0 (ञ=0, न=0).
    - Standalone independent vowels assign 0 (*dhiśūnyam svarāstvakṣaram*).
    - In conjunct consonants (*Samyuktakshara*), only the last consonant takes numerical value (
      *miśre tūpāntyahal saṅkhyā*).
    - Vowel matras and diacritics carry zero value.
    - Multi-digit numbers composed in reverse (*aṅkānām vāmato gatiḥ*): extracted digits read
      right-to-left.
- Example: "खगो" -> Digits [2, 3] -> Reversed: 32. "जलधि" -> Digits [8, 3, 9] -> Reversed: 938.

### 3.11 Chinese Nine Star Ki (Feng Shui Flying Stars)

- **Authority**: *Xuan Kong Fei Xing*; I Ching Solar Calendar; Jean Meeus, *Astronomical
  Algorithms* (1998).
- **Ruleset**: `CHINESE_NINE_STAR_KI_V1`.
- **Astronomical Year Boundary**: Solar year begins at astronomical Li Chun (立春, apparent solar
  longitude = 315.0°). Dates before Li Chun belong to $(Year - 1)$.
- **Principal Star Formula**:
    - Solar year digits reduced: $Y_{red} = \text{reduce}(\text{SolarYear})$.
    - Principal Star: $S = 11 - Y_{red} \pmod 9$ (adjusted to 1..9 range).
- Associated with 9 Stars, 5 Elements (Water, Earth, Wood, Metal, Fire), and 8 Trigrams (Kan, Kun,
  Zhen, Xun, Qian, Dui, Gen, Li, Taiji).

### 3.12 Modern Tarot Numerology (Major Arcana Birth Cards)

- **Authority**: Mary K. Greer, *Tarot for Your Self: A Workbook for the Inward Journey* (1984), Ch.
  2; Angeles Arrien (1987).
- **Ruleset**: `TAROT_BIRTH_CARD_V1`.
- **Algorithm**:
    - Full calendar sum: $S_{raw} = \text{MM} + \text{DD} + \text{YYYY}$.
    - Sum digits of $S_{raw}$; if $> 22$, sum digits again. Result (1..22) = Personality Card.
    - Sum digits of Personality Card = Soul Card.
    - Special Triad: 19 yields The Sun (19) / Wheel of Fortune (10) / The Magician (1).
    - Special Card 22: The Fool (22/0).
    - Personality $\le 9$: Personality and Soul cards represent the same single archetype.
- Isolated from reading logic, tarot draws, and spread mechanics.

---

## 4. Public SDK API

The public API is exposed via `AynvoraSdk`:

```kotlin
// Full structured request
suspend fun calculateNumerology(
    request: NumerologyRequest
): AynvoraResult<NumerologyResult>

// Convenience overload
suspend fun calculateNumerology(
    birthDay: Int,
    birthMonth: Int,
    birthYear: Int,
    fullName: String? = null,
    rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
): AynvoraResult<NumerologyResult>
```

Repository contract (`NumerologyRepository`):

```kotlin
interface NumerologyRepository {
    suspend fun getProfile(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        fullName: String? = null,
        rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
    ): AynvoraResult<NumerologyResult>

    suspend fun calculate(
        request: NumerologyRequest
    ): AynvoraResult<NumerologyResult>
}
```

Implementation lives in `:aynvora-data` (`com.aynvora.data.numerology.NumerologyRepositoryImpl`).

---

## 5. EvidenceGraph & AI Non-Authority Boundary

`NumerologyEvidenceGraphFactory` generates a deterministic `EvidenceGraph`:

- **Nodes**:
    - `FACT`: Normalized birth date, normalized name.
    - `TRADITIONAL_RULE`: Ruleset authority reference, reduction policy.
    - `DERIVED_FACT`: Radical, Destiny, Name Number, Soul Urge, Personality, Pinnacles, Personal
      Year, Combinations.
- **Edges**: `DERIVES`, `APPLIES_RULE`, `CONTRIBUTES`.
- **Provenance**: Declares domain (`CoreFeatureId.NUMEROLOGY`), ruleset, source book citation, and
  engine version.
- **AI Guarantee**: The SLM may read `EvidenceGraph` nodes to generate natural-language reflection
  text, but can never recompute, overwrite, or mutate the numbers.

---

## 6. Event SDK & Analytics Privacy

- **Events Registered**:
    - `dashboard.numerology.open_clicked` (`AynvoraEventType.CLICK`, payload:
      `FeatureOpenPayload(CoreFeatureId.NUMEROLOGY)`) -> mapped to
      `AnalyticsEvent.NumerologyOpened`.
    - `numerology.calculate_clicked` (`AynvoraEventType.CLICK`, payload:
      `NumerologyCalculatePayload(rulesetId, hasName, hasTargetYear)`) -> mapped to
      `AnalyticsEvent.NumerologyCalculationStarted`.
- **Privacy Boundary**:
    - NO PII (no birth day, month, year, name string, or calculated personal numbers) is ever sent
      to Firebase or analytics.
    - Only anonymous technical metrics (`ruleset_id`, error codes) pass through
      `AnalyticsSafePayload`.

---

## 7. Localization Boundary

- Calculations produce pure numerical values, system enums, and calculation traces.
- No language branching (`if (language == "hi")`, `when (locale)`) exists in `:aynvora-core` or
  `:ui`.
- All presentational text, accessibility semantics, and labels are resolved strictly via
  `:aynvora-localization`'s 11-locale key-driven catalog across 740 canonical translation keys.

---

## 8. Consumer UI/UX Architecture (Phase 10.4)

The consumer interface follows strict unidirectional data flow:
`Composable` $\to$ `NumerologyUiEvent` $\to$ Event SDK $\to$ `NumerologyViewModel` $\to$ Engine /
Repositories $\to$ `EvidenceGraph` $\to$ `NumerologyUiState` $\to$ Localized Composable
Presentation.

### 8.1 Principles

1. **Zero Math in UI**: Composables contain NO formulas, modulo operations, digit reductions, or
   gematria lookups. All numbers originate from `NumerologyResultPackage`.
2. **Zero Direct Repo Access**: Composables dispatch `NumerologyUiEvent` to `NumerologyViewModel`.
   Composables never call repositories directly.
3. **No Language Branching**: Zero `when (locale)` or `if (language == ...)` in UI. All labels,
   traditions, categories, and errors resolve through `StringResolver` and
   `TranslationKey.NumerologyExperience.*`.
4. **Rich Visualizers**:
    - Core Numbers cards (Radical, Destiny, Name Number, Soul Urge, Personality)
    - 3x3 Lo Shu visual grid with row/col/diagonal lines and accessibility semantics
    - Hebrew Gematria letter breakdown table & Ragil / Mispar Gadol values
    - Arabic Abjad letter-by-letter table & Mashriqi / Maghribi values
    - Indian Katapayadi phoneme chips & digit reversal display
    - Chinese Nine Star Ki star card & Li Chun solar boundary notice
    - Modern Tarot Birth Card Major Arcana display with dual-card archetypes
    - Step-by-step reduction trace card ("How This Was Calculated")
    - Source authority provenance banner ("Method & Provenance")
    - Traditional reflective insights card (from content packages)
    - Cross-tradition comparison sheet/table
    - Full calculation history management

---

## 9. Offline-First History & Persistence

- **Contract**: `NumerologyHistoryRepository` in `:aynvora-core`.
- **Implementation**: `NumerologyHistoryRepositoryImpl` in `:aynvora-data`, backed by
  `StorageDriver`.
- **Key Namespace**: `aynvora_numerology_history`.
- **Serialization**: JSON-serialized list of `NumerologyHistoryEntry` models containing:
    - `id`: Unique UUID
    - `timestampEpochMs`: Execution timestamp
    - `rulesetId`: Evaluated ruleset identifier
    - `rulesetName`: Human-readable ruleset name
    - `primaryNumbers`: Key-value pairs of primary calculated numbers
    - `summary`: One-line overview of the calculation
- **Operations**: `save(entry)`, `getAll(): AynvoraResult<List<NumerologyHistoryEntry>>`,
  `clear(): AynvoraResult<Unit>`.
- **Error Handling**: Wrapped in `AynvoraResult.Failure.StorageFailure` on I/O or JSON corruption.

---

## 10. Numerology Report Generation Engine

- **Generator**: `NumerologyReportGenerator` implementing `ReportGenerator<NumerologyReportInput>`
  in `:aynvora-core`.
- **Registration**: Registered in `ReportGeneratorRegistry` inside `ReportEngine`.
- **Document Structure**: Produces a standardized `ReportDocument` with:
    - `ReportHeader`: Tradition title, ruleset name, authority citation.
    - `ReportSection` (Inputs): Birth date, evaluated name, target year.
    - `ReportSection` (Core Results): Modality-specific tables, Key-Value pairs, or Grid structures.
    - `ReportSection` (Calculation Trace): Deterministic step-by-step reduction paths.
    - `ReportSection` (Provenance): Canonical book citations, ruleset versions, and methodology
      notes.
    - `ReportFooter`: Deterministic engine version and non-medical/non-deterministic disclaimer.

---

## 11. Event SDK Registry & Zero-PII Telemetry

9 specialized numerology events registered in `AynvoraEventRegistry`:

1. `dashboard.numerology.open_clicked` $\to$ `AnalyticsEvent.NumerologyOpened`
2. `numerology.ruleset_selected` $\to$ `AnalyticsEvent.NumerologyRulesetSelected(rulesetId)`
3. `numerology.calculate_clicked` $\to$ `AnalyticsEvent.NumerologyCalculationStarted(rulesetId)`
4. `numerology.calculation_completed` $\to$
   `AnalyticsEvent.NumerologyCalculationCompleted(rulesetId)`
5. `numerology.calculation_failed` $\to$ `AnalyticsEvent.NumerologyCalculationFailed(errorCode)`
6. `numerology.result_viewed` $\to$ `AnalyticsEvent.NumerologyResultViewed(rulesetId)`
7. `numerology.trace_viewed` $\to$ `AnalyticsEvent.NumerologyTraceViewed(rulesetId)`
8. `numerology.method_compared` $\to$ `AnalyticsEvent.NumerologyMethodCompared`
9. `numerology.report_requested` $\to$ `AnalyticsEvent.NumerologyReportRequested(rulesetId)`
10. `numerology.history_opened` $\to$ `AnalyticsEvent.NumerologyHistoryOpened`

**Zero-PII Guarantee**: No user names, raw input scripts, birth dates, or personal result numbers
are tracked. Only anonymous ruleset identifiers and technical error codes enter analytics.

---

## 12. Complete 11-Locale Localization System

- **Canonical Key Count**: 740 keys (all 11 locales complete, 0 missing keys, 0 placeholder
  mismatches).
- **Supported Locales**:
    - English (`en`)
    - Hindi (`hi`)
    - Arabic (`ar` — RTL enabled)
    - Bengali (`bn`)
    - Gujarati (`gu`)
    - Kannada (`kn`)
    - Malayalam (`ml`)
    - Marathi (`mr`)
    - Punjabi (`pa`)
    - Tamil (`ta`)
    - Telugu (`te`)

---

## 13. AI Future Boundary (Non-Authoritative)

The "Explain with AYNVORA AI" entry point is clearly demarcated:

- Reads exclusively from deterministic `EvidenceGraph` nodes.
- Cannot mutate, re-evaluate, or contradict engine calculations.
- Labeled with strict "Coming Soon" and AI boundary disclaimer in Phase 10.4 UI.

