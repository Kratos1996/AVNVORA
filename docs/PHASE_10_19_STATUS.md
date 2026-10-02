# Phase 10.19 implementation status

This phase builds on the uncommitted Phase 10.18 work in the checkout. The Phase 10.18 baseline includes the shared Transit/Panchang feature engines, observation contexts, event/knowledge data models, and source audit. Those files remain preserved.

## This phase

- Replaced unchecked generic Transit/Panchang output casts with checked payload mapping. The feature output is inspected as `FeatureOutput<*>`, then its value is checked against the expected result type.
- Added `AstroEventEvaluationRequest` and SDK event evaluation. The evaluator consumes caller supplied feature facts; it does not calculate planetary positions or invent event rules.
- Event occurrences now preserve their producing feature ID, typed conditions, period, matched evidence IDs, source references, status, and calculation provenance.
- Added snapshot event storage and deterministic EvidenceGraph nodes/edges. Event facts connect to a derived event node only when supported/partial evidence actually matched.
- Bumped Kundali JSON to schema 2. Schema 1 inputs migrate by adding the new optional event and evidence fields; saved chart opening reports `MIGRATE` and returns the migrated snapshot.
- Added a deterministic knowledge rule matcher that preserves tradition, rule ID, pack/source version, source reference, rights status, and matched evidence. It does not emit interpretation prose.
- Added an advanced feature capability registry that reports dependencies and missing requirements. KP and Lal Kitab and Varshaphal remain `NOT_VERIFIED`; Phaladesh remains `UNSUPPORTED`.

## Advanced engines

| Feature | Status | Missing material |
|---|---|---|
| KP | `NOT_VERIFIED` | Verified KP ayanamsha/cusp convention, source table for 249 divisions, and licensed significator/timing rules. |
| Lal Kitab | `NOT_VERIFIED` | Approved source edition with reproduction rights and a normalized source referenced rule pack. |
| Varshaphal/Tajika | `NOT_VERIFIED` | Verified solar return/annual chart conventions and source backed Tajika/Mudda Dasha rules. |
| Phaladesh | `UNSUPPORTED` | Supported event/rule packs and monthly evidence integration. |

The audit at `docs/REFERENCES/VEDASTRO_REFERENCE.md` records that the available reference is for research only; it does not supply cleared production code or source data. No golden tradition outputs are created without verified fixtures.

## Validation

The Phase 10.19 build matrix passed: `:astro-engine:jvmTest`, `:aynvora-core:jvmTest`, `:aynvora-data:jvmTest`, `:ui:jvmTest`, `test`, `:desktopApp:assemble`, `:androidApp:assembleDebug`, and `git diff --check`. Event evaluator fixtures use explicitly test-only source identifiers and are not production rule packs.
