# Phase 10.23 Knowledge Acquisition Report

## Acquired and ingested source material

One 1907 edition and its selected Wikisource transcription were acquired and transformed into the scoped `TAJIKA_V1` production knowledge pack. The scan contains 280 pages; this phase inspected and ingested selected folios 112, 122, and 124 (printed pages 104, 114, and 116). The selected Wikisource pages are marked unproofread. Each was manually cross-checked against its page image. The transcription source retains revision IDs and raw wikitext in `knowledge/tajika/source/source_pages.json`.

| Source | Rights and provenance | Ingested use |
|---|---|---|
| [1907 Tājika Nīlakaṇṭhī scan](https://commons.wikimedia.org/wiki/File:%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_(%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%B9%E0%A4%BF%E0%A4%A4%E0%A4%BE).pdf) | Commons marks the scan public domain in India and the United States. Territorial status may differ elsewhere. Edition: Khemraj Shri Venkateshwar Steam Press, Samvat 1964 / Śaka 1829 (1907). SHA-256: `a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989`. | Scan comparison for folios 112, 122, 124; no scan-image text redistributed. |
| [Sanskrit Wikisource transcription index](https://sa.wikisource.org/wiki/%E0%A4%85%E0%A4%A8%E0%A5%81%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%AE%E0%A4%A3%E0%A4%BF%E0%A4%95%E0%A4%BE:%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_(%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%B9%E0%A4%BF%E0%A4%A4%E0%A4%BE).pdf) | Selected transcription revisions 378434, 378444, 378446 are CC BY-SA 4.0. Attribution and ShareAlike are carried in the source record and pack. Transcription-content SHA-256: `3ba811a6b537fe0fbecbb89674299107060f14504205c28a5f9bd3d2d47eda76`. | Basis for concise attributed adaptations; pages 112, 122, 124 only. |

The edition's front matter includes a press rights-registration notice. The scoped reuse relies on the Commons public-domain determination for the scan in India and the United States and the separate CC BY-SA 4.0 grant for Wikisource transcriptions/adaptations. Distribution outside those scan territories is limited to the CC BY-SA transcription-derived adaptations; consult local rights before reusing the scan itself. This is a recorded source assessment, not legal advice.

## Pack build result

- Pack: `TAJIKA_V1`, version `1.0.0`, status `VERIFIED_SCOPED_CONTENT`.
- Source records: 2 (public-domain scan and CC BY-SA transcription).
- Source sections: 2; formal chapter headings were not established.
- Selected pages/sections: 3 (280-page edition; only 3 folios ingested).
- Chunks: 3.
- Structured rules: 8.
- Glossary terms: 6.
- Rule citation references: 8; chunk/page citations: 3.
- Language: English adaptations of Sanskrit/Hindi source material.
- Pack SHA-256: `513be82525a8a3b846a86b6aaf770ef1575b61fcafe196e0f41de0bd4213301d`.
- Build: deterministic generator at `knowledge/tajika/build_pack.py`; runtime pack factory at `aynvora-core/.../knowledge/tajika/TajikaKnowledgePack.kt`.

The content covers four house-specific Muntha indications (houses 3–6), Muntha affliction/support qualifications, and strong/middling Sun-as-Varshesha indications. Rule descriptions are attributed traditional themes, not factual predictions. It does not claim complete annual-chart doctrine or coverage of other houses/planets.

## Research-only material

KP source leads remain in the Phase 10.22 report. No 249-subdivision table is produced, because the independent algorithm, second reference, and boundary validation gates are not complete. See `knowledge/research/kp/README.md`. Lal Kitab remains isolated with no source text, remedy, or rule ingested; see `knowledge/research/lal-kitab/README.md`. Other traditions remain discovery backlog.

## Runtime, model, and training status

- Source acquisition, checksum build, structured rules, offline search, evidence fusion, and selected Muntha rule execution are implemented.
- `AynvoraAiToolExecutor` now parses a strict one-tool JSON call, restricts execution to registered available tools, runs the selected tool, fuses its result with local evidence, and passes the evidence through `AynvoraLocalIntelligence.synthesize` and its output validator.
- Remote search is opt-in and provider-injected. The tool executor passes only a query, strips common email/phone/date/time/coordinate/address patterns, and does not send `PageContext.visibleData`. Pattern redaction cannot identify every personal detail; remote use requires host policy and caller choice.
- A 30-scenario orchestration suite uses fake planner, generic tool outputs, and fake answer generation. It verifies dispatch/evidence/source-reference/validation wiring; it is not 30 real native-model conversations.
- No base model artifact is bundled in this workspace. Native model execution and full offline AI inference were not run or verified. Existing runtime can fall back; fallback must not be presented as native output.
- `training/dataset-v1/verified_sft.jsonl` contains 8 generated source-linked examples. The validator accepts them. No training job, base model, loss, adapter, or evaluation artifact exists; status is `TRAINING_PREPARED`.

## Implementation boundary

The current engine evaluates only caller-supplied Muntha house/qualification facts and selected caller-supplied Sun-as-Varshesha strength. It does not calculate the return instant, annual chart, annual ascendant, Muntha position/lord, Varsheshwara selection, Sahams, Tajika aspects, annual house analysis, or Mudda Dasha. These features remain partial/unsupported and are not represented as available production calculations.
