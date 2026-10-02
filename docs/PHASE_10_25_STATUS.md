# Phase 10.25 Status — Classical Tajika verification

## 1. Source review

Reviewed the complete 280-page 1907 *Tājika Nīlakaṇṭhī* scan, SHA-256 `a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989`, and retrieved all 280 Wikisource transcription page records as navigation aid. The scan was the authority; selected pages were rendered and visually checked. Source rights and territorial limitation are recorded in [Tajika classical rule extraction](Tajika_CLASSICAL_RULE_EXTRACTION.md).

## 2. Implemented verified subset

- `MunthaEngine` advances one sign per elapsed solar-return cycle, retains the natal ascendant degree, derives the Muntha Lord from the occupied sign, and optionally derives its annual whole-sign house.
- `calculateVarshaphal` now includes the source-calculated Muntha result when natal ascendant longitude is available. Missing longitude yields NOT_VERIFIED.
- `getMunthaLord` is AVAILABLE in the public tool registry and executable through `TajikaRegisteredTools.verifiedSubset()` in the grounded executor tool map. Output includes primary-source evidence.
- Golden arithmetic tests cover cycle offsets 0, 1, 11, 12, and 13; zodiac wrap; lord; annual house; invalid inputs; and tool registration/execution.

## 3. Remaining status

| Component | Status | Reason |
|---|---|---|
| Varshaphala solar return / annual chart | PARTIAL | Existing Phase 10.24 calculations retained. |
| Muntha sign / longitude / lord | VERIFIED / CALCULATED | Source rules: Varṣa tantra Muntha chapter verses 1 and 3, PDF 120–121 (printed 112–113). |
| Varṣeshwara | AMBIGUOUS | Panchadhikāri candidate set is verified, but Panchavargiya strength and source's competing eligibility/tie-break opinions are not resolved. |
| Punya, Vidyā, Yaśas, Karma Sahams | NOT_VERIFIED | Formulas identified; correction arc boundary semantics and day/night event handling need another verification pass. |
| Tajika aspects / sixteen yogas | AMBIGUOUS / NOT_VERIFIED | The source gives multiple orb opinions; full operational chapter mapping is incomplete. Existing non-Tajika aspects are not substituted. |
| Mudda Dasha | UNSUPPORTED | This text names it but refers to another treatise for its method. No secondary heuristic was activated. |

## 4. Tool execution

The new `getMunthaLord` adapter is deterministic and real; the broader Varshaphala calculation API is not yet an AI tool because chart input construction is host-specific. Other phase tools remain unavailable or partial as reflected by `AstroToolRegistry`. No external reference engine was treated as oracle for disputed rules.

## 5. Verification

PASS: `./gradlew :aynvora-core:jvmTest --tests 'com.aynvora.core.astrology.knowledge.tajika.MunthaEngineTest' --tests 'com.aynvora.core.VarshaphalSdkIntegrationTest'`; `git diff --check` PASS.

The SDK maps elapsed cycles as `targetYear - birthYear` because its return solver searches within the target Gregorian year. The source says elapsed years; it does not specify Gregorian API semantics. This is recorded as an SDK convention and the pure engine accepts an explicit cycle count.

## 6. Attribution

Adapted from *Tājika Nīlakaṇṭhī* (1907), with Mahidhar commentary; Wikisource contributors' transcription, CC BY-SA 4.0. Adaptation by AYNVORA SDK contributors under CC BY-SA 4.0. Wikimedia Commons states the scan is public domain in India and the US and cautions that status may vary elsewhere.
