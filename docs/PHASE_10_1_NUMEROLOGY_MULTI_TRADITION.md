# PHASE 10.1 — AYNVORA MULTI-TRADITION NUMEROLOGY EXPANSION

**Indian / Vedic Ank Jyotish + Lo Shu 3x3 Magic Square + Multi-Tradition Registry**

---

## 1. Executive Summary

Phase 10.1 expands the AYNVORA Numerology foundation beyond Western Pythagorean and Chaldean/Cheiro
traditions into a true, versioned, source-gated multi-tradition architecture.

Crucially, **traditions are not merged into an invented "universal" calculation**. Each
numerological tradition operates as an isolated ruleset with its own:

- Primary historical source authority
- Exact digit extraction and reduction formulas
- Master number handling policies
- Script and alphabet validation rules
- Planetary associations and relationship matrices
- Calculation traces and EvidenceGraph representations

---

## 2. Supported Ruleset Registry

| Ruleset ID               | Tradition Name                    | Primary Source Authority                                                                                        | Supported Calculations                                                                               | Master Number Policy           | Name System                  | Status   |
|--------------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|--------------------------------|------------------------------|----------|
| `CHALDEAN_CHEIRO_V1`     | Chaldean / Cheiro Classical       | Cheiro (Count Louis Hamon), *Cheiro's Book of Numbers* (1926)                                                   | Radical, Destiny, Name Number, Personal Year                                                         | Preserve 11, 22                | Chaldean Sound (1–8)         | VERIFIED |
| `PYTHAGOREAN_WESTERN_V1` | Western Pythagorean               | Matthew Oliver Goodwin, *Numerology: The Complete Guide* (1981)                                                 | Life Path, Birth Day, Expression, Soul Urge, Personality, Personal Year, Personal Month, 4 Pinnacles | Preserve 11, 22, 33            | Pythagorean Sequential (1–9) | VERIFIED |
| `INDIAN_ANK_JYOTISH_V1`  | Indian / Vedic Ank Jyotish        | Pandit Sethuraman, *Science of Fortune* (1954); Dr. M. Katakkar, *Miracles of Numerology* (1989)                | Moolank (Driver), Bhagyank (Conductor), Namank, Personal Year, Panchadha Maitri                      | Reduce all to 1..9 (Navagraha) | Cheiro-Sethuraman (1–8)      | VERIFIED |
| `LO_SHU_CLASSICAL_V1`    | Classical Lo Shu 3x3 Magic Square | *I Ching* (Luoshu Scroll, Zhou Dynasty); Dr. David A. Phillips, *The Complete Book of Numerology* (1992), Ch. 5 | 3x3 Frequency Grid, 8 Planes, Arrows of Strength, Arrows of Weakness                                 | N/A (Direct Digit Extraction)  | N/A (No Name Calculations)   | VERIFIED |

---

## 3. Comparative Tradition Matrix

| Topic                     | Chaldean (`CHALDEAN_CHEIRO_V1`)                    | Pythagorean (`PYTHAGOREAN_WESTERN_V1`)                                 | Indian Ank Jyotish (`INDIAN_ANK_JYOTISH_V1`)                                                            | Lo Shu Grid (`LO_SHU_CLASSICAL_V1`)                                |
|---------------------------|----------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| **Alphabet Mapping**      | 1–8 sound vibration mapping (9 excluded as sacred) | 1–9 sequential Latin alphabet mapping (A=1..Z=8)                       | 1–8 sound vibration mapping (for Latin-transliterated names)                                            | Unsupported (N/A)                                                  |
| **Date Method**           | Full date decimal digit sum (`DIGIT_SUM`)          | Month, Day, Year reduced separately then summed (`COMPONENT_THEN_SUM`) | Full date decimal digit sum (`DIGIT_SUM`)                                                               | Direct digit extraction from `DD-MM-YYYY`, '0' filtered out        |
| **Day Number**            | Radical (Moolank), 11 & 22 preserved               | Birth Day, 11 & 22 preserved                                           | Moolank (Driver), strictly reduced to 1..9 root                                                         | Placed into canonical cell (no reduction)                          |
| **Master Numbers**        | 11 and 22 recognized                               | 11, 22, and 33 recognized                                              | Strictly reduced to 1..9 (Only 9 Navagrahas exist in Vedic framework)                                   | Not applicable                                                     |
| **Compound Numbers**      | Intermediate sums preserved in trace               | Intermediate sums preserved in trace                                   | Intermediate sums preserved in trace                                                                    | Counts frequencies per digit (0..n)                                |
| **Planetary Association** | Western Chaldean planetary assignments             | Western planetary octave assignments                                   | 9 Vedic Navagrahas with Sanskrit names (Surya, Chandra, Guru, Rahu, Budha, Shukra, Ketu, Shani, Mangal) | Chinese elements and directional trigrams (external content layer) |
| **Relationship Matrix**   | Cheiro planetary friendship matrix                 | N/A                                                                    | Katakkar / Sethuraman Panchadha Maitri matrix (Mitra, Sama, Shatru)                                     | Arrows across 8 planes (Strength, Weakness, Partial)               |
| **Additional Cycles**     | Personal Year                                      | Personal Year, Personal Month, 4 Pinnacles                             | Personal Year (Varshank)                                                                                | N/A                                                                |

---

## 4. Source Traceability & Historical Citations

1. **Indian / Vedic Ank Jyotish (`INDIAN_ANK_JYOTISH_V1`)**:
    - **Pandit Sethuraman**: *Science of Fortune* (1954), 1st Edition; *Adhristavijnanam* (Tamil
      original).
        - Standardized Moolank (birth day reduced to 1..9) and Bhagyank (total birth date reduced to
          1..9).
        - Defined Navagraha rulers: 1=Sun (Surya), 2=Moon (Chandra), 3=Jupiter (Guru), 4=Rahu,
          5=Mercury (Budha), 6=Venus (Shukra), 7=Ketu, 8=Saturn (Shani), 9=Mars (Mangal).
    - **Dr. M. Katakkar**: *Miracles of Numerology* (1989), Jaico Publishing House.
        - Documented the authoritative Panchadha Maitri number relationship matrix:
            - 1 (Surya): Mitra: 1, 2, 3, 9; Sama: 5; Shatru: 4, 6, 7, 8
            - 2 (Chandra): Mitra: 1, 2, 3; Sama: 7, 9; Shatru: 4, 5, 6, 8
            - 3 (Guru): Mitra: 1, 2, 3, 9; Sama: 5, 7; Shatru: 4, 6, 8
            - 4 (Rahu): Mitra: 5, 6, 7; Sama: 1, 8; Shatru: 2, 4, 9
            - 5 (Budha): Mitra: 1, 5, 6; Sama: 3, 4, 8, 9; Shatru: 2, 7
            - 6 (Shukra): Mitra: 4, 5, 6, 7, 8; Sama: 3, 9; Shatru: 1, 2
            - 7 (Ketu): Mitra: 4, 6, 7; Sama: 2, 3, 5; Shatru: 1, 8, 9
            - 8 (Shani): Mitra: 4, 5, 6; Sama: 3, 7; Shatru: 1, 2, 8, 9
            - 9 (Mangal): Mitra: 1, 2, 3; Sama: 5; Shatru: 4, 6, 7, 8, 9

2. **Lo Shu 3x3 Magic Square (`LO_SHU_CLASSICAL_V1`)**:
    - **Historical Antiquity**: *I Ching* (*Yijing*, Book of Changes), *Xici Shang* (Appended
      Remarks, Pt. 1), Luoshu River Scroll (Luo River, Henan, Zhou Dynasty).
        - Canonical 3x3 Magic Square arrangement summing to 15 along all 3 rows, 3 columns, and 2
          diagonals:
          ```
          4 | 9 | 2
          ---------
          3 | 5 | 7
          ---------
          8 | 1 | 6
          ```
    - **Dr. David A. Phillips**: *The Complete Book of Numerology* (1992), Hay House, Ch. 5 "The
      Chart and the Arrows".
        - Prescribes extraction of all calendar digits from birth date (DD-MM-YYYY).
        - Explicitly defines zero ('0') as void/unplaced.
        - Formalizes the 8 Planes and Arrow classifications:
            - 3 Horizontal Rows: Mind Plane (4-9-2), Soul Plane (3-5-7), Practical Plane (8-1-6)
            - 3 Vertical Columns: Thought Plane (4-3-8), Will Plane (9-5-1), Action Plane (2-7-6)
            - 2 Diagonals: Determination Plane (4-5-6), Compassion Plane (2-5-8)
            - Classification: `ARROW_OF_STRENGTH` (all 3 present), `ARROW_OF_WEAKNESS` (all 3
              absent), or `PARTIAL`.

---

## 5. Input Validation & Script Policy

- **Display Language vs. Calculation Alphabet**: UI localization to Hindi, Tamil, Arabic, etc.,
  never alters the calculation algorithm.
- **Latin Transliteration Contract**: Classical Cheiro and Western Pythagorean alphabet tables are
  defined only for the Latin alphabet (A–Z).
- **Explicit Script Rejection**:
    - If a user inputs non-Latin characters (e.g. Devanagari `"इशांत"`, Tamil, Arabic, Cyrillic),
      the engine **strictly rejects** the input with
      `AynvoraResult.Failure.InvalidInput("fullName", ...)`.
    - Non-Latin characters are never silently discarded or guessed.

---

## 6. Number Semantics: Separation of Calculation vs. Interpretation

- **The calculation engine returns structured mathematical facts only**:
    - Coordinates, counts, integer roots, master flags, planetary IDs, and relationship categories.
- **No psychological or predictive text in the calculation engine**:
    - Interpretive descriptions (e.g. "analytical, spiritual, perfectionist") belong exclusively to
      the content and EvidenceGraph layer.

---

## 7. Golden References & Test Results

All four rulesets were verified using dedicated golden reference test suites:

- `NumerologyChaldeanGoldenReferenceTest.kt`: PASS
- `NumerologyPythagoreanGoldenReferenceTest.kt`: PASS
- `NumerologyIndianGoldenReferenceTest.kt`: PASS
- `NumerologyLoShuGoldenReferenceTest.kt`: PASS
- `NumerologyCrossTraditionDifferenceTest.kt`: PASS
- `NumerologyPropertyInvariantTest.kt`: PASS

### JKR-117480 Reference Vector (DOB: 11-07-1996, Name: "ISHANT")

| Feature                | `CHALDEAN_CHEIRO_V1`    | `PYTHAGOREAN_WESTERN_V1` | `INDIAN_ANK_JYOTISH_V1` | `LO_SHU_CLASSICAL_V1`                                                        |
|------------------------|-------------------------|--------------------------|-------------------------|------------------------------------------------------------------------------|
| **Radical / Moolank**  | 11 (Master Moon Octave) | 11 (Master Preserved)    | 2 (Chandra / Moon)      | *Unsupported*                                                                |
| **Destiny / Bhagyank** | 7 (Ketu / Neptune)      | 7 (Life Path 7)          | 7 (Ketu / South Node)   | *Unsupported*                                                                |
| **Name Number**        | 1 (Sun)                 | 8 (Expression)           | 1 (Surya / Sun)         | *Unsupported*                                                                |
| **Soul Urge**          | *Unsupported*           | 1 (Heart's Desire)       | *Unsupported*           | *Unsupported*                                                                |
| **Personality**        | *Unsupported*           | 7 (Outer Persona)        | *Unsupported*           | *Unsupported*                                                                |
| **Pinnacles**          | *Unsupported*           | 9, 9, 9, 5               | *Unsupported*           | *Unsupported*                                                                |
| **Relationship**       | Neutral                 | *Unsupported*            | Neutral (Sama)          | *Unsupported*                                                                |
| **Lo Shu Grid**        | *Unsupported*           | *Unsupported*            | *Unsupported*           | Usable: 7 digits; Zeros: 1; Freq: 1:3, 6:1, 7:1, 9:2; Missing: 2, 3, 4, 5, 8 |
| **Lo Shu Arrows**      | *Unsupported*           | *Unsupported*            | *Unsupported*           | Weakness: Thought (4-3-8), Compassion (2-5-8); Strength: 0                   |
