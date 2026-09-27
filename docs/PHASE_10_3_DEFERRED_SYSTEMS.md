# AYNVORA Phase 10.3 — Deferred Numerical Traditions Implementation Specification

**Document ID:** `DOC-PHASE-10-3-DEFERRED-SYSTEMS`  
**Discipline Isolation & Source Verification Report**  
**Compliance:** Rule 72, Source-Gated Epistemology, Offline-First, Privacy Boundary

---

## 1. Executive Summary

Phase 10.3 systematically evaluates, sources, implements, and verifies the 5 numerical and
alphanumeric traditions deferred from Phase 10.2:

1. **Hebrew Gematria — Mispar Gadol Variant** (`HEBREW_MISPAR_GADOL_V1`)
2. **Arabic Hisab al-Jummal — Maghribi Order Variant** (`ARABIC_ABJAD_MAGHRIBI_V1`)
3. **Indian Katapayadi Numerical Mnemonic System** (`INDIAN_KATAPAYADI_V1`)
4. **Chinese Nine Star Ki (Flying Star Feng Shui)** (`CHINESE_NINE_STAR_KI_V1`)
5. **Modern Tarot Numerology (Major Arcana Birth Cards)** (`TAROT_BIRTH_CARD_V1`)

In addition, Phase 10.3 reviews and formally classifies the remaining non-algorithmic or composite
traditions:

- **Chinese Homophonic Number Symbolism** (`CHINESE_HOMOPHONIC_SYMBOLISM`): `SYMBOLIC_ONLY`
- **Western Angel Numbers** (`WESTERN_ANGEL_NUMBERS`): `SYMBOLIC_ONLY`
- **Arbitrary Internet Composite Numerology** (`INTERNET_COMPOSITE_POPULAR`): `DEPRECATED`

---

## 2. Comprehensive System Documentation

### 2.1 Hebrew Gematria — Mispar Gadol Variant

- **Discipline**: `GEMATRIA`
- **Cultural Context**: Medieval Jewish Kabbalah & Rabbinic textual analysis.
- **Primary Source**: Rabbi Moses Cordovero, *Pardes Rimonim* (1591 CE), Gate 30 (*Sha'ar
  Ha-Tzeruf*); Sefer HaBahir.
- **Variant**: `MISPAR_GADOL` (Large Number), isolated from `MISPAR_RAGIL` (
  `HEBREW_GEMATRIA_CLASSICAL_V1`).
- **Accepted Input**: Native Hebrew consonants (Alef-Bet א–ת) including final forms (Sofiyot: ך, ם,
  ן, ף, ץ).
- **Mapping**:
    - Regular consonants: א=1, ב=2, ג=3, ד=4, ה=5, ו=6, ז=7, ח=8, ט=9, י=10, כ=20, ל=30, מ=40, נ=50,
      ס=60, ע=70, פ=80, צ=90, ק=100, ר=200, ש=300, ת=400.
    - Final letters (Sofiyot):
        - Final Kaf (ך) = 500 (vs Ragil 20)
        - Final Mem (ם) = 600 (vs Ragil 40)
        - Final Nun (ן) = 700 (vs Ragil 50)
        - Final Pe (ף) = 800 (vs Ragil 80)
        - Final Tsadi (ץ) = 900 (vs Ragil 90)
- **Formula & Reduction**:
    - Absolute Value: $\sum \text{value}(c_i)$ across normalized consonants.
    - Mispar Katan (Small Root): Digit sum reduction to single digit $1 \dots 9$.
- **Boundary & Validation Rules**:
    - Hebrew vowel points (niqqud) and cantillation accents (te'amim) are deterministically
      stripped.
    - Punctuation, maqaf, and spaces are ignored.
    - Latin, Arabic, Devanagari, and other non-Hebrew scripts are strictly rejected with typed
      `AynvoraResult.Failure.InvalidInput`.
- **Golden Test Vectors**:
    - "שלום": ש(300) + ל(30) + ו(6) + ם(600) = 936 -> reduced to 9. (In Ragil: 376 -> 7).
    - "מלך": מ(40) + ל(30) + ך(500) = 570 -> reduced to 3. (In Ragil: 90 -> 9).
    - "גן": ג(3) + ן(700) = 703 -> reduced to 1. (In Ragil: 53 -> 8).
    - "כסף": כ(20) + ס(60) + ף(800) = 880 -> reduced to 7. (In Ragil: 160 -> 7).
    - "ארץ": א(1) + ר(200) + ץ(900) = 1101 -> reduced to 3. (In Ragil: 291 -> 3).
- **Implementation Status**: `DOCUMENTED_AND_IMPLEMENTED` (Ruleset: `HEBREW_MISPAR_GADOL_V1`).

---

### 2.2 Arabic Hisab al-Jummal — Maghribi Order Variant

- **Discipline**: `ABJAD_ARITHMETIC`
- **Cultural Context**: Historical North African (Maghreb) and Andalusian Islamic scholarship and
  scribal traditions.
- **Primary Source**: Ibn Khaldun, *The Muqaddimah* (1377 CE), Chapter 6, Section 28 (*Ilm
  al-Huruf*).
- **Variant**: `MAGHRIBI`, isolated from `MASHRIQI` (`ARABIC_ABJAD_MASHRIQI_V1`).
- **Accepted Input**: Native Arabic consonants (28 letters: ا–غ).
- **Mapping (Maghribi Order)**:
    - Mnemonic: *Abjad (1–4), Hawwaz (5–7), Hutti (8–10), Kalaman (20–50), Sa'fadh (60–90), Qarast (
      100–400), Thakhadh (500–700), Zaghsh (800–1000)*.
    - Divergent Letters from Eastern (Mashriqi) Standard:
        - ص (Sa') = 60 (Mashriqi: 90)
        - ض (Da) = 90 (Mashriqi: 800)
        - س (Sin) = 300 (Mashriqi: 60)
        - ظ (Zha) = 800 (Mashriqi: 900)
        - غ (Ghayn) = 900 (Mashriqi: 1000)
        - ش (Sheen) = 1000 (Mashriqi: 300)
    - Ta Marbuta (ة) = 400 (grammatical variant of Taa).
    - Hamza forms (ء, أ, إ, آ, ٱ) canonicalized to Alif (1); ؤ to Waw (6); ئ, ى to Yaa (10).
- **Formula & Reduction**:
    - Jummal Kabir (Great Sum): $\sum \text{value}(c_i)$ across normalized consonants.
    - Jummal Saghir (Small Reduction): Digit sum reduction to single root $1 \dots 9$.
- **Boundary & Validation Rules**:
    - Tashkeel (harakat), tanwin, sukun, shaddah, dagger alif, and tatweel are deterministically
      stripped.
    - Latin, Hebrew, Indic, and other scripts strictly rejected with typed
      `AynvoraResult.Failure.InvalidInput`.
- **Golden Test Vectors**:
    - "شمس": ش(1000) + م(40) + س(300) = 1340 -> reduced to 8. (In Mashriqi: 300+40+60 = 400 -> 4).
    - "صبر": ص(60) + ب(2) + ر(200) = 262 -> reduced to 1. (In Mashriqi: 90+2+200 = 292 -> 4).
    - "ضوء": ض(90) + و(6) + ء(1) = 97 -> reduced to 7. (In Mashriqi: 800+6+1 = 807 -> 6).
    - "ظل": ظ(800) + ل(30) = 830 -> reduced to 2. (In Mashriqi: 900+30 = 930 -> 3).
- **Implementation Status**: `DOCUMENTED_AND_IMPLEMENTED` (Ruleset: `ARABIC_ABJAD_MAGHRIBI_V1`).

---

### 2.3 Indian Katapayadi Numerical Mnemonic System

- **Discipline**: `NUMEROLOGY` (Alphanumeric Mnemonic Encoding)
- **Cultural Context**: Classical Sanskrit mathematics and South Indian astronomy (Kerala School,
  Aryabhata tradition).
- **Primary Source**:
    - Sankaravarman, *Sadratnamala* (1819 CE), Prakarana 1, Verses 3–5:
      *"najñāvacāśca śūnyāni saṅkhyāḥ kaṭapayādayaḥ | miśre tūpāntyahal saṅkhyā na ca cintyo halaḥ
      svaraḥ || aṅkānāṁ vāmato gatiḥ"*
    - Haridatta, *Grahacaranibandhana* (683 CE).
    - Aryabhata commentaries (Suryadevayajvan, Nilakantha Somayaji).
- **Variant**: Standard Sadratnamala Kerala/Sanskrit variant.
- **Accepted Input**: Native Devanagari Sanskrit phonemes and words.
- **Mapping (Ka-Ta-Pa-Ya Groups)**:
    - **Ka-varga**: क=1, ख=2, ग=3, घ=4, ङ=5, च=6, छ=7, ज=8, झ=9, ञ=0
    - **Ta-varga** (retroflex & dental): ट=1, ठ=2, ड=3, ढ=4, ण=5, त=6, थ=7, द=8, ध=9, न=0
    - **Pa-varga**: प=1, फ=2, ब=3, भ=4, म=5
    - **Ya-varga**: य=1, र=2, ल=3, व=4, श=5, ष=6, स=7, ह=8, ळ=9
- **Phoneme & Conjunct Rules**:
    - Vowel signs (matras), anusvara, visarga carry zero value (*na ca cintyo halah svarah*).
    - Standalone independent vowels (अ, आ, इ, etc.) assign 0 (*dhisunyam svarastvaksaram*).
    - Conjunct consonants (*Samyuktakshara*): Only the *last* consonant in the cluster takes value (
      *misre tupantyahal sankhya*). For example, in सूर्य (su-rya), the cluster र्य has consonants
      र् and य; the last consonant य=1 takes value.
- **Digit Direction & Reversal (*Ankānām Vāmato Gatiḥ*)**:
    - Extracted digits represent units, tens, hundreds, etc., and are read from right to left (
      reversed to form the final numerical integer).
- **Boundary & Validation Rules**:
    - Devanagari script strictly required (Unicode `0x0900..0x097F`).
    - Latin transliteration strictly rejected.
- **Golden Test Vectors**:
    - "खगो": ख(2), गो(3) -> Digits: [2, 3] -> Reversed: 32.
    - "नभ": न(0), भ(4) -> Digits: [0, 4] -> Reversed: 40.
    - "जलधि": ज(8), ल(3), धि(9) -> Digits: [8, 3, 9] -> Reversed: 938.
    - "सूर्य": सू(7), र्य(1) -> Digits: [7, 1] -> Reversed: 17.
    - "अचल": अ(0), च(6), ल(3) -> Digits: [0, 6, 3] -> Reversed: 360.
- **Implementation Status**: `DOCUMENTED_AND_IMPLEMENTED` (Ruleset: `INDIAN_KATAPAYADI_V1`).

---

### 2.4 Chinese Nine Star Ki (Flying Star Feng Shui)

- **Discipline**: `NUMEROLOGY` (Feng Shui Astrological Numerology)
- **Cultural Context**: Ancient Chinese Five Elements (*Wu Xing*), I Ching Bagua, and Japanese
  Kyuusei Kigaku.
- **Primary Source**:
    - *Xuan Kong Fei Xing* (Flying Star Feng Shui classical treatises).
    - *I Ching* (Book of Changes, Luoshu Sequence).
    - Takashi Yoshikawa, *The Ki: The Japanese Art of Divination* (1981).
    - Jean Meeus, *Astronomical Algorithms* (1998), Chapters 7 & 25 (Li Chun apparent solar
      longitude 315.0°).
- **Accepted Input**: Gregorian calendar birth date and time with UTC timezone.
- **Astronomical Solar Year Determination**:
    - The Nine Star Ki year does NOT start on January 1, nor does it start on an approximate
      hardcoded February 4.
    - It transitions at the exact astronomical instant the Sun reaches an apparent ecliptic
      longitude of 315.0° (*Li Chun*, 立春).
    - Calculated deterministically via `SolarTermEngine` using Jean Meeus's solar coordinate model
      and iterative refinement.
    - If birth instant $< \text{Li Chun}$, $\text{SolarYear} = \text{CalendarYear} - 1$.
    - If birth instant $\ge \text{Li Chun}$, $\text{SolarYear} = \text{CalendarYear}$.
- **Principal Star Formula**:
    1. Reduce solar year digits: $Y_{red} = \text{reduce}(\text{SolarYear})$.
    2. Compute raw star number: $S = 11 - Y_{red}$.
    3. Modulo 9 wrap to 1..9 range.
- **Star & Element Correspondences**:
    - 1: 1 White Water (Kan / Water / North)
    - 2: 2 Black Earth (Kun / Earth / Southwest)
    - 3: 3 Jade Wood (Zhen / Wood / East)
    - 4: 4 Green Wood (Xun / Wood / Southeast)
    - 5: 5 Yellow Earth (Taiji / Earth / Center)
    - 6: 6 White Metal (Qian / Metal / Northwest)
    - 7: 7 Red Metal (Dui / Metal / West)
    - 8: 8 White Earth (Gen / Earth / Northeast)
    - 9: 9 Purple Fire (Li / Fire / South)
- **Golden Test Vectors**:
    - Feb 1, 1996 (Before Li Chun): Solar Year 1995 -> 1+9+9+5 = 24 -> 6 -> 11 - 6 = 5 (5 Yellow
      Earth).
    - Feb 10, 1996 (After Li Chun): Solar Year 1996 -> 1+9+9+6 = 25 -> 7 -> 11 - 7 = 4 (4 Green
      Wood).
    - Jan 15, 2000 (Before Li Chun): Solar Year 1999 -> 1+9+9+9 = 28 -> 10 -> 1 -> 11 - 1 = 1 (1
      White Water).
    - Jul 15, 2000 (After Li Chun): Solar Year 2000 -> 2 -> 11 - 2 = 9 (9 Purple Fire).
    - Dec 1, 2024: Solar Year 2024 -> 2+0+2+4 = 8 -> 11 - 8 = 3 (3 Jade Wood).
- **Implementation Status**: `DOCUMENTED_AND_IMPLEMENTED` (Ruleset: `CHINESE_NINE_STAR_KI_V1`).

---

### 2.5 Modern Tarot Numerology (Major Arcana Birth Cards)

- **Discipline**: `NUMEROLOGY` (Archetypal Tarot Numerology)
- **Cultural Context**: 20th-century Western Esoteric / Archetypal Tarot psychology.
- **Primary Source**:
    - Mary K. Greer, *Tarot for Your Self: A Workbook for the Inward Journey* (1984), Chapter 2.
    - Angeles Arrien, *The Tarot Handbook: Practical Applications of Ancient Visual Symbols* (1987).
- **Accepted Input**: Gregorian birth date (DD-MM-YYYY).
- **Algorithm (Greer 1984)**:
    1. Full calendar sum: $S_{raw} = \text{Month} + \text{Day} + \text{Year}$.
    2. Sum digits of $S_{raw}$. If the sum exceeds 22, sum digits again until a number
       in $1 \dots 22$ is obtained. This is the **Personality Card**.
    3. Sum digits of Personality Card to derive the **Soul Card**.
- **Special Configurations**:
    - **Triad 19**: If Personality Card is 19 (The Sun), the Soul Card is 10 (Wheel of Fortune), and
      the Shadow/Teacher Card is 1 (The Magician). (19 / 10 / 1 triad).
    - **Card 22**: Represents 22 (The Fool), with secondary resonance 4 (The Emperor).
    - **Single Archetypes**: For Personality Cards $\le 9$, Personality Card and Soul Card are
      identical.
- **Cross-Feature Isolation**:
    - Strictly isolated from `:ui/tarot` and `TarotQuestionEngine`.
    - Does NOT alter card draws, spread evaluations, reversed orientations, or the 24-hour reading
      lock.
- **Golden Test Vectors**:
    - Nov 15, 1954 (Greer 1984 published vector): 11 + 15 + 1954 = 1980 -> 1+9+8+0 = 18 (The Moon) /
      1+8 = 9 (The Hermit).
    - Jan 9, 1980 (19 Triad): 1 + 9 + 1980 = 1990 -> 1+9+9+0 = 19 (The Sun) / 1+9 = 10 (Wheel of
      Fortune) / 1 (The Magician).
    - Nov 7, 1996 (Single Archetype): 11 + 7 + 1996 = 2014 -> 2+0+1+4 = 7 (The Chariot) / Soul: 7 (
      The Chariot).
    - May 5, 1950 (Pair Card): 5 + 5 + 1950 = 1960 -> 1+9+6+0 = 16 (The Tower) / Soul: 1+6 = 7 (The
      Chariot).
- **Implementation Status**: `DOCUMENTED_AND_IMPLEMENTED` (Ruleset: `TAROT_BIRTH_CARD_V1`).

---

## 3. Review of Remaining Deferred / Symbolic / Deprecated Traditions

| Tradition ID                   | Canonical Name                      | Discipline         | Source Basis                                     | Status          | Rationale                                                                                                                      |
|:-------------------------------|:------------------------------------|:-------------------|:-------------------------------------------------|:----------------|:-------------------------------------------------------------------------------------------------------------------------------|
| `CHINESE_HOMOPHONIC_SYMBOLISM` | Chinese Homophonic Number Symbolism | `NUMBER_SYMBOLISM` | Spoken Cantonese/Mandarin homophony (8=Fa, 4=Si) | `SYMBOLIC_ONLY` | Folk linguistic superstition; lacks biographical calculation algorithms.                                                       |
| `WESTERN_ANGEL_NUMBERS`        | Modern Angel Numbers                | `NUMBER_SYMBOLISM` | Doreen Virtue (2008)                             | `SYMBOLIC_ONLY` | Channeled subjective digit repetitions (111, 222); no reproducible birth calculation.                                          |
| `INTERNET_COMPOSITE_POPULAR`   | Arbitrary Internet SEO Calculators  | `NUMEROLOGY`       | Anonymous blogs                                  | `DEPRECATED`    | Arbitrary mixing of Pythagorean letter values with Cheiro compounds without source foundation. Explicitly rejected by Rule 72. |

---

## 4. Verification & Regression Matrix

All 12 rulesets and supporting components have been subjected to rigorous deterministic unit tests,
golden reference tests, cross-tradition isolation tests, and mathematical invariant tests:

1. `NumerologyChaldeanGoldenReferenceTest`: Passed.
2. `NumerologyPythagoreanGoldenReferenceTest`: Passed.
3. `NumerologyIndianGoldenReferenceTest`: Passed.
4. `NumerologyLoShuGoldenReferenceTest`: Passed.
5. `NumerologyHebrewGematriaGoldenReferenceTest`: Passed.
6. `NumerologyHebrewGadolGoldenReferenceTest`: Passed.
7. `NumerologyArabicAbjadGoldenReferenceTest`: Passed.
8. `NumerologyArabicMaghribiGoldenReferenceTest`: Passed.
9. `NumerologyAgrippanGoldenReferenceTest`: Passed.
10. `NumerologyKatapayadiGoldenReferenceTest`: Passed.
11. `NumerologyNineStarKiGoldenReferenceTest`: Passed.
12. `NumerologyTarotBirthCardGoldenReferenceTest`: Passed.
13. `NumerologyCrossTraditionDifferenceTest`: Passed (Zero cross-tradition leakage across all 12
    rulesets).
14. `NumerologyPropertyInvariantTest`: Passed (Invariants, termination, and script validation
    verified).
