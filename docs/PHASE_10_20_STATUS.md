# Phase 10.20 status

Phase 10.20 uses VedAstro only as a pinned engineering reference. The inspected files were `Library/Data/EventData.cs` and `Library/Data/TimeRange.cs` at commit `fcb4dede360372545eb244c53e9a80ec3510e194`. Those show event metadata linked to calculator methods and an explicit time-range type. AYNVORA contains its own Kotlin implementation and does not copy reference code, source text, or astrology data. The source audit is in [REFERENCES/VEDASTRO_REFERENCE.md](REFERENCES/VEDASTRO_REFERENCE.md).

## Capability report

| Area | Status | Delivered in this phase / remaining boundary |
|---|---|---|
| A. Reference techniques | IMPLEMENTED | Adopted data/logic separation, metadata links, explicit ranges, deterministic local access, and result provenance as engineering patterns. |
| B. Data and logic separation | PARTIAL | Event/rule metadata and evaluators are separate types. Existing planetary tables and advanced tradition packs still need their own verified datasets. |
| C. Calculator registry | IMPLEMENTED | `AstroCalculatorRegistry` projects the existing feature engines into queryable descriptors, including dependencies, profiles, versions, and readiness. |
| D. Common calculation result | IMPLEMENTED | `AstroCalculationResult<T>` carries output, dependencies, evidence, source refs, provenance, version, status, and warnings. |
| E. Deterministic caching | IMPLEMENTED | Bounded process-local cache; keys include birth time, timezone, coordinates, location metadata, profile/conventions, provider/engine/contract/data versions, and dependency provenance. Seeded calculations bypass cache. |
| F. Observation context | IMPLEMENTED | Observation context is separate from birth data and includes location, local/UTC time, timezone data version, requested range, and purpose. |
| G. Event catalog | IMPLEMENTED | Versioned catalog is searchable by tradition, feature, tag, rule, source, and status; definitions reference calculators by ID. |
| H. Time-sliced event generation | PARTIAL | Generates bounded Julian Day slices, reuses one caller-supplied base fact set, evaluates time-varying facts, sorts and merges adjacent matches. A caller must supply the domain calculations; no complete natal chart is recalculated inside this layer. |
| I. Event range tests | IMPLEMENTED | Coverage includes 1, 7, 30, and 365 day ranges and asserts base-fact reuse and deterministic merging. |
| J. KP | NOT_VERIFIED | No KP calculation or 249 subdivision table added; verified convention, licensed table, and source-backed golden fixtures remain missing. |
| K. Lal Kitab | NOT_VERIFIED | No rules added; approved source edition, rights, and normalized referenced rule pack remain missing. |
| L. Varshaphal/Tajika | NOT_VERIFIED | No annual-return formulas or rules added; verified conventions and sourced fixtures remain missing. |
| M. Phaladesh | UNSUPPORTED | No monthly predictions added; event packs and evidence integration are not yet source-backed. |
| N. Knowledge packs | PARTIAL | Existing typed source, rule, chunk, and pack metadata remain separate from evaluation logic. This phase adds chapter/page/rule and embedding model version metadata; no new copyrighted content was bundled. |
| O. Deterministic search | IMPLEMENTED | Local phrase/token search with metadata filters, deterministic ordering, and optional semantic candidates ranked after text results. |
| P. Embeddings | PARTIAL | Chunk metadata supports an embedding model/version, and a host may supply semantic search. No embedding generation pipeline or model is included. |
| Q. Web search | UNSUPPORTED | No network search provider was added. Basic SDK use remains offline. |
| R. Tool registry and AI discovery | UNSUPPORTED | A calculator registry exists; the requested general AI tool registry, permission model, and web/source tools do not. |
| S. AI calculation grounding | PARTIAL | The deterministic event layer accepts typed facts and emits no prose; this phase adds no AI orchestration or output validator. |
| T. Answer/report pipeline | UNSUPPORTED | No ask pipeline or report document/render pipeline added. Existing snapshot JSON remains the structured data surface. |
| U. Headless SDK | PARTIAL | Grouped astrology APIs expose calculator metadata, feature calculations, events, knowledge search, and existing location operations without requiring UI. Ask, report, and dedicated JSON wrappers remain unimplemented. |
| V. Reusable UI components | UNSUPPORTED | No component SDK was added. |
| W. Chart model and page contract | PARTIAL | Existing house-owned chart projection is preserved; the broad `AstroPage` contract is not implemented. |
| X. Fixture runner / golden coverage | PARTIAL | Focused event, cache, and search tests were added. No 1000-fixture runner or new reference golden data was invented. |
| Y. Performance | PARTIAL | Range generation reuses base facts, bounds slices to 10,000, and merges contiguous event intervals. No benchmark suite or transit-state cache was added. |
| Z. Source and license records | PARTIAL | Existing source and license metadata are retained; separate comprehensive records for code, ephemeris, datasets, books, and embeddings remain future work. |

## Verification

Passed with `./gradlew :astro-engine:jvmTest :aynvora-core:jvmTest :aynvora-data:jvmTest :ui:jvmTest test :desktopApp:assemble :androidApp:assembleDebug --offline --console=plain`. The run includes the 1/7/30/365 day event ranges, cache input separation, and local/semantic search ordering tests. `git diff --check` passed.
