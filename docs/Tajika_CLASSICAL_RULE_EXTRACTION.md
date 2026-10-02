# Tājika Nīlakaṇṭhī classical rule extraction

**Phase:** 10.25 — classical Tajika verification  
**Review date:** 2026-10-01  
**Primary source:** Nīlakaṇṭha Daivajña, *Tājika Nīlakaṇṭhī*, with Mahidhar Hindi commentary, Khemraj Shri Venkateshwar Steam Press, 1907 edition. The digital scan is 280 PDF pages. Scan SHA-256: `a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989`.  
**Scan record:** [Wikimedia Commons file record](https://commons.wikimedia.org/wiki/File:%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_%28%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%Aಸ%E0%Aಹ%E0%A4%BF%E0%A4%A4%E0%A4%BE%29.pdf) · [Wikisource index/transcription](https://sa.wikisource.org/wiki/%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_%28%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%B9%E0%A4%BF%E0%A4%A4%E0%A4%BE%29).

## Source rights and handling

| Item | Finding | Handling |
|---|---|---|
| 1907 scan | Commons records the 280-page scan as public domain in India and the United States; its record cautions that status may differ in other territories. | Retain metadata, scan hash, and territorial limitation. Do not redistribute the scan from this repository. |
| Wikisource transcription | Page transcription is labeled CC BY-SA 4.0 and reflects contributor revisions. | Attribute Wikisource contributors; adaptations of transcription text must retain CC BY-SA 4.0. |
| Text used here | English rule summaries and formulas below are concise original paraphrases from the scan and its Hindi commentary. | Record PDF folio and printed folio separately. The transcription is a navigation aid; the scan decides readings. |
| Retrieval | Full PDF and 280 transcription pages reviewed on 2026-10-01; selected folios 41, 87–89, 110–112, 120–122, 173–174, and 192 checked against rendered scan images. | Local research copies were temporary files under `/tmp`, not committed. |

## Work coverage and location map

PDF page is the physical scan page; printed page is the folio printed on the page. The book begins with front matter, so the numbers differ.

| Book portion | PDF pages | Printed pages | Contents / review note |
|---|---:|---:|---|
| Saṃjñā tantra | 1–107 | approximately 1–99 | Definitions, strengths, aspects, Tajika combinations, Sahams. Saham chapter occupies approximately PDF 86–107 (printed 78–99). |
| Varṣa tantra | 108–201 | approximately 100–193 | Year-lord results, Muntha, annual bhava results, month/day progressions and annual timing topics. Muntha chapter begins PDF 120 (printed 112). |
| Praśna tantra | 202–280 | approximately 194–272 | Horary material; outside the annual calculation scope of this phase. |

Chapter transitions around the tantra boundaries and Saham section were checked in the page transcription and sampled scans; subchapter spans are approximate where the print does not provide a distinct heading. Do not treat these spans as verse concordances. Rule rows below carry the exact verse/folio where available.

## Rule extraction

| Rule ID | Feature | Primary location | Sanskrit cue / source meaning | Operational reading and required inputs | Status |
|---|---|---|---|---|---|
| TN-MUN-01 | Muntha annual progression | Varṣa tantra, Muntha chapter, v.1; PDF 120 / printed 112 | “svajanma-lagnāt prati varṣam ekaika-rāśi…” Commentary: advance one sign yearly; add elapsed years to natal ascendant sign, divide by 12; retain natal ascendant's exact degree. | Input natal sidereal ascendant longitude and count of elapsed solar-return cycles. Output sign = (natal sign index + elapsed cycles) mod 12; longitude retains the within-sign natal degree. Do not substitute civil age if cycle count is unknown. | VERIFIED; implemented. |
| TN-MUN-02 | Muntha Lord | Varṣa tantra, Muntha chapter, v.3; PDF 121 / printed 113 | “yasyāṃ rāśau munthā … svāmī munthēśaḥ” Commentary directly defines the lord as ruler of the sign occupied by Muntha. | Derive the ordinary sign ruler after TN-MUN-01. Output lord is a graha enum. The source defines the relationship; sign ownership follows the text's standard planetary rulership scheme. | VERIFIED; implemented with the conventional seven-graha sign rulers. |
| TN-VAR-01 | Panchadhikāri candidates | Varṣa tantra, year-lord discussion, vv.5–7; PDF 110 / printed 102; see also PDF 41 / printed 33 v.62 | Candidates are natal ascendant lord, annual ascendant lord, Muntha sign lord, trirāśi lord, and day-Sun/night-Moon sign lord. | Candidate enumeration is clear, but selection also requires Panchavargiya strength and qualifying aspects to annual ascendant. The existing Shadbala score is not the specified fivefold strength. | NOT_VERIFIED for selection. |
| TN-VAR-02 | Varṣeshwara aspect/strength priority | Same passage; PDF 110–111 / printed 102–103, vv.5–8 | Stronger qualified candidate seeing annual ascendant may win; equality and non-qualification branches have multiple opinions. | Do not substitute an aspect count or modern strength score. Need a source-grounded Panchavargiya implementation and a resolved reading of alternatives. | AMBIGUOUS; not implemented. |
| TN-VAR-03 | Varṣeshwara tie/fallback | PDF 110–111 / printed 102–103, vv.7–8; PDF 41 / printed 33 v.62 | Commentary preserves alternative rules: Muntha lord, strongest candidate, annual ascendant lord, dignity-based choice; another view invokes day/night sign lord. Moon has an Ithasala qualification in one view. | No unique fallback verified from this edition. | AMBIGUOUS; not implemented. |
| TN-SAH-01 | Punya Saham | Saṃjñā tantra, Saham chapter, vv.5–6; PDF 87 / printed 79; scan checked | At day: Asc + Moon − Sun. At night: Asc + Sun − Moon. Commentary adds a one-sign correction when ascendant fails the prescribed arc inclusion condition. | Requires longitudes at the chosen event and a precisely formalized circular-arc convention. Day/night is tied to the event (birth or annual ingress), not civil clock hour. The arc wording and boundary inclusion require further independent verification before production. | NOT_VERIFIED; formula identified, correction edge semantics pending. |
| TN-SAH-02 | Vidyā / Guru Saham | Saham chapter, v.6; PDF 88 / printed 80 | Reverses the Punya subtraction: day Asc + Sun − Moon; night Asc + Moon − Sun; same one-sign correction. | Same unresolved arc-edge convention and event-time day/night requirement as TN-SAH-01. | NOT_VERIFIED; not implemented. |
| TN-SAH-03 | Yaśas Saham | Saham chapter, v.6; PDF 88 / printed 80 | Day Asc + Jupiter − Punya; night Asc + Punya − Jupiter; same correction. | Depends on verified Punya longitude and correction rule. | NOT_VERIFIED; not implemented. |
| TN-SAH-04 | Karma Saham | Saham chapter, v.12 (Hindi commentary numbering 37); PDF 89 / printed 81 | Day Asc + Mars − Mercury; night Asc + Mercury − Mars; same correction. | Requires event-time day/night and same correction. | NOT_VERIFIED; not implemented. |
| TN-ASP-01 | Tajika aspects and deeptāṃśa | Saṃjñā tantra, graha-chara-dṛṣṭi chapter, vv.13–14; PDF 49 / printed 41 | Gives planet-specific orbs (Sun 15°, Moon 12°, Mars 8°, Mercury 7°, Jupiter 9°, Venus 7°, Saturn/Rahu/Ketu 9°) and an alternative all-12° opinion; applying proximity relates to Itthasāla. | This passage alone does not settle every directional/applying case or compatibility with the SDK's existing non-Tajika aspect engine. Need full chapter mapping and rule-specific oracle cases. | AMBIGUOUS; not implemented. |
| TN-ASP-02 | Sixteen Tajika yogas | Yoga chapter following aspect section; approximately PDF 49–86, printed 41–78 | Text treats Itthasāla, Īsarāpha and related combinations with detailed conditions. | No complete verse-by-verse extraction or independently checked boundary cases completed in this sprint. Do not infer from an existing Parāśari aspect calculator. | NOT_VERIFIED; not implemented. |
| TN-MUD-01 | Mudda Dasha | Varṣa tantra month/day discussion; PDF 173 and 192 / printed 165 and 184 | PDF 192 names Mudda among annual/monthly dasha systems but directs the reader to another treatise for method; no complete executable sequence and duration rule is established here. | Secondary libraries describe a scaled Vimśottarī sequence, but that does not establish this book's rule. | UNSUPPORTED by this primary source; not implemented. |

### Implemented Muntha algorithm boundaries

The public calculation accepts a non-negative **elapsed solar-return cycle count**. In SDK integration, the target Gregorian return year minus the birth Gregorian year supplies that count because this engine solves the natal-Sun recurrence within that same Gregorian year. At the birth year itself this is zero. This mapping is documented as an SDK cycle-count convention; the source's mathematical rule is “elapsed years,” not a Gregorian date API specification.

The calculation preserves the natal ascendant's absolute sidereal longitude within each 30° sign. It optionally derives annual house by whole-sign distance from the annual ascendant. It does not calculate results/interpretations from the Muntha house, nor claim that a missing natal ascendant was calculated.

### Secondary implementation cross-check

MayaAstrolib's public project/changelog describes a one-sign-per-year Muntha, a simplified five-candidate year lord, Saham formulas, and a Mudda sequence. This was used only to locate likely comparison points. Its own documented strength simplifications and the primary-source forks above mean its output is not a valid oracle for Varṣeshwara or Mudda. No secondary implementation was treated as authority or copied.

## Production feature status (22 requested items)

Status labels: **VERIFIED** means direct source evidence and executable implementation; **AMBIGUOUS** means source disagreement or unresolved method; **NOT_VERIFIED** means insufficient primary-source extraction; **UNSUPPORTED** means this primary text expressly defers or omits the method; **PARTIAL** means only a stated subcomponent is executable.

| # | Requested item | Status | Production behavior |
|---:|---|---|---|
| 1 | Source identity, edition, folios | VERIFIED | Recorded above with scan hash and primary links. |
| 2 | Scan/transcription rights | VERIFIED with territorial limit | Scan rights statement is limited to India and US; transcription attribution and ShareAlike recorded. |
| 3 | Complete scan retrieval | VERIFIED | 280-page source hash checked; only temporary local copy. |
| 4 | Full transcription navigation | PARTIAL | 280 page records retrieved; unproofread text remains subordinate to scans. |
| 5 | Tantra/chapter map | PARTIAL | High-level tantra and chapter spans listed; approximate spans labeled. |
| 6 | Verse boundary map | PARTIAL | Exact verses listed for implemented/researched rules; no claim of a complete concordance. |
| 7 | Muntha sign progression | VERIFIED | Implemented. |
| 8 | Muntha longitude preservation | VERIFIED | Implemented. |
| 9 | Muntha Lord derivation | VERIFIED | Implemented from sign lord. |
| 10 | Muntha house in annual chart | VERIFIED when both ascendants exist | Calculated as whole-sign distance; otherwise omitted. |
| 11 | Panchadhikāri candidates | VERIFIED as source list only | Candidate set extracted; not independently selectable without missing strength/aspect pieces. |
| 12 | Panchavargiya strength | NOT_VERIFIED | Existing Shadbala is not used as substitute. |
| 13 | Varṣeshwara eligibility and aspect test | AMBIGUOUS | Not calculated. |
| 14 | Varṣeshwara tie-break/fallback | AMBIGUOUS | Alternative readings remain explicit. |
| 15 | Punya Saham | NOT_VERIFIED | Formula extracted, correction boundaries unresolved; no calculator. |
| 16 | Vidyā Saham | NOT_VERIFIED | Formula extracted, correction boundaries unresolved; no calculator. |
| 17 | Yaśas Saham | NOT_VERIFIED | Depends on unresolved correction and Punya. |
| 18 | Karma Saham | NOT_VERIFIED | Formula extracted, correction boundaries unresolved; no calculator. |
| 19 | Day/night classifier for Sahams | NOT_VERIFIED | Requires event-location solar altitude/sunrise rule aligned to source; no civil-time shortcut. |
| 20 | Tajika aspect/yoga engine | AMBIGUOUS / NOT_VERIFIED | Not calculated; existing non-Tajika engine is not substituted. |
| 21 | Mudda Dasha sequence and durations | UNSUPPORTED | Primary text defers; no secondary heuristic activated. |
| 22 | Tool registry/executor and differential evidence | PARTIAL | Registered-tool capability and source metadata exist; calculator is deterministic. No independent trusted oracle for ambiguous features; Muntha golden cases cover arithmetic only. |

## Independent golden cases

These are arithmetic oracles derived directly from the verse's one-sign rule, not from an independent astrology library.

| Natal ascendant | Elapsed cycles | Expected Muntha | Lord | Annual ascendant / expected house |
|---|---:|---|---|---|
| 15° Aries (15°) | 0 | Aries, 15° | Mars | Aries / 1 |
| 15° Aries (15°) | 1 | Taurus, 15° | Venus | Aries / 2 |
| 29° Pisces (359°) | 1 | Aries, 29° | Mars | Sagittarius / 5 |
| 15° Aries (15°) | 11 | Pisces, 15° | Jupiter | Pisces / 1 |
| 15° Aries (15°) | 12 | Aries, 15° | Mars | — |
| 15° Aries (15°) | 13 | Taurus, 15° | Venus | — |

Invalid longitude outside [0°, 360°) and negative elapsed-cycle inputs must be rejected. A complete Varṣaphala result must retain explicit statuses: Muntha CALCULATED only if natal ascendant longitude exists; otherwise NOT_VERIFIED. Varṣeshwara, Sahams, Tajika aspects, and Mudda remain unsupported/not verified as annotated in the result.

## Implementation files

- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/astrology/knowledge/tajika/MunthaEngine.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/astrology/knowledge/tajika/VarshaphalResult.kt`
- `aynvora-core/src/commonMain/kotlin/com/aynvora/core/internal/AstroEngineAdapter.kt`
- `docs/PHASE_10_25_STATUS.md`

Attribution for this source-derived adaptation: “Based on *Tājika Nīlakaṇṭhī* (1907), with Mahidhar commentary; Wikisource contributors' transcription, CC BY-SA 4.0. Adapted by AYNVORA SDK contributors; adaptation released under CC BY-SA 4.0.” The underlying scan is stated by Commons to be public domain in India and the United States; other territorial status may differ.
