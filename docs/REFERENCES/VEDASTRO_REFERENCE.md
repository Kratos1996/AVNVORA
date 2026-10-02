# VedAstro reference audit

**Purpose:** architecture and capability reference only. VedAstro is not an AYNVORA calculation authority or a source of golden values.

## Pinned source

- Repository: <https://github.com/VedAstro/VedAstro>
- Inspected commit: `fcb4dede360372545eb244c53e9a80ec3510e194` (HEAD when audited on 2026-10-01)
- Repository-declared license: MIT, in [`LICENSE.md`](https://github.com/VedAstro/VedAstro/blob/fcb4dede360372545eb244c53e9a80ec3510e194/LICENSE.md)
- Local checkout used for read-only inspection: `/tmp/VedAstro-reference`; no source, data, or text was copied into AYNVORA.

The project README describes 200+ API operations and a data + logic + time event model. The audit below checks source paths at the pinned commit; advertised API coverage is not treated as proof of correctness.

## Areas inspected

| Area | Source locations at pinned commit | Audit finding |
|---|---|---|
| Main library and data | `Library/Library.csproj`, `Library/Data/`, `Library/Data/Enum/`, `Library/Data/Delegate/` | Domain types, enums, delegates, cache and data access are present. The project is a broad .NET library with service and storage dependencies. |
| Calculation logic | `Library/Logic/Calculate/` | Concrete calculator source exists; individual algorithms and conventions need independent review. |
| Event and horoscope data | `Library/XMLData/EventDataList.xml`, `HoroscopeDataList.xml`, corresponding `Library/Data/EventData*`, `HoroscopeData*`, `EventNameAttribute.cs` | Event definitions are separated from calculator methods in places. Files named `*-not-proved.xml` explicitly indicate unproved material; neither these nor other text was imported. |
| Dasha | `Library/Logic/Calculate/VimshottariDasa.cs`; `Library/Data/Dasa.cs`, `DasaEvent.cs`, `DasaChart.cs`, `TimeRange.cs` | Nested periods and current-period APIs exist, including methods beyond three levels. Their date boundaries and year conventions are not assumed equivalent to AYNVORA. |
| Panchang and solar events | `Library/Logic/Calculate/Core.cs` (Panchang, sunrise/sunset and SwissEph calls); `Library/Data/PanchangaTable.cs`, `LunarDay.cs`, `Karana.cs`, `NithyaYoga.cs` | Panchang data/calculation paths exist. Core uses SwissEphNet for at least certain astronomy/sunrise paths, so repository MIT does not establish dependency or data rights. |
| Divisional charts | `Library/Logic/Calculate/Vargas.cs`; `Library/Data/Enum/ChartType.cs`; `Library/Logic/Factory/NorthChartFactory.cs`, `SouthChartFactory.cs` | Varga transformation helpers and chart rendering selections exist. Each varga’s traditional mapping still requires rule-by-rule comparison. |
| Bhava Chalit / houses | `Library/Data/Enum/ChartType.cs` (`BhavaChalit`); `Library/Logic/Factory/NorthChartFactory.cs`, `SouthChartFactory.cs`; house calculations in `Core.cs` | Chart type/rendering support and SwissEph house code are present. Exact cusp convention, boundary rules and placement parity require test vectors. |
| Ashtakavarga | `Library/Logic/Calculate/Ashtakavarga.cs`; `Library/Data/Bhinnashtakavarga.cs`, `Sarvashtakavarga.cs`, `Prastaraka.cs` | BAV/SAV and Prastaraka data/calculation source exists. No AYNVORA values were replaced from it. |
| Strength / Shadbala | Calls such as `IsPlanetStrongInShadbala` in `Library/Logic/Calculate/Muhurtha.cs`; related types include `Library/Data/HouseSubStrength.cs` | Strength-related operations are referenced. The audit did not establish complete parity for all six Shadbala components or their formulas from this search alone. |
| KP | `Library/Logic/CalculateKP-ORI.cs`; KP-related enums/options in `Library/Data/Enum/` | KP-specific source exists, but this is not evidence of a verified complete KP product or a compatible license for any source material. |
| Varshaphal | Repository-wide search for `Varshaphal`, `Varsha`, annual chart APIs and types at this commit | No sufficiently traceable implemented Varshaphal calculator was confirmed in the inspected core calculation paths; treat as unverified. |
| Prashna | Repository-wide search for `Prashna` and question-chart logic | No sufficiently traceable Prashna calculator was confirmed; treat as unverified. |
| Transit / event charts | `Library/Data/EventsChart.cs`, `EventSlices.cs`, `EventTag.cs`; `Library/Logic/Factory/EventsChartFactory.cs`; `API/FrontDesk/EventsChartAPI.cs`; `Library/Logic/EventManager.cs` | Time-range event chart and API structure exist. These are reference architectures, not proof of forecast quality. |
| Prediction architecture | `Library/Data/Event.cs`, `EventData.cs`, `EventNameAttribute.cs`, `Delegate/EventGenerator.cs`, `Logic/EventManager.cs`, `XMLData/EventDataList.xml` and `HoroscopeDataList.xml` | Supports a useful separation of event data/identity from calculation methods and time ranges. Some rule/source material has separate provenance and quality concerns. |
| API | `API/FrontDesk/`, `API/Program.cs`, `Library/Data/OpenAPI*`, `OpenAPIStaticTable.cs` | API and generated operation metadata are present. API count is not an accuracy measure. |
| AI/search | `Library/Logic/Calculate/ChatAPI.cs`, `NLPTools.cs`, `LLMEmbeddingManager.cs`; `Library/Logic/LLMEmbeddingManager.cs`; `DocToEmbeddings/Program.cs`; `Library/Data/*Embedding*`; `LLMCoder/` | AI, embedding, and documentation ingestion code is present. It is outside this deterministic SDK phase; no AI output/data is adopted. |

## Useful patterns and AYNVORA decisions

1. **Keep rule data separate from evaluator code and time ranges.** AYNVORA adds typed event-definition and occurrence metadata models. This is a forward-compatible domain contract only; no prediction prose or VedAstro rules were imported.
2. **Return nested Dasha periods as structured data.** AYNVORA exposes a request-based Dasha API over its existing natal chart result. Dasha formulas and boundary conventions stay AYNVORA-owned.
3. **Expose provider and convention metadata with outputs.** AYNVORA strengthens ephemeris metadata while preserving its analytical engine.
4. **Reference adapter:** `AstroReferenceComparison` records input/profile/conventions/provider outputs/tolerance/status/source references with `authority = REFERENCE_ONLY`. There is no runtime VedAstro SDK/network dependency. A live provider and verified normalized comparison fixtures remain future work.

## Candidate calculations and validation required

Candidates for future comparisons are planetary longitude/motion, houses/cusps, Nakshatra/Pada, Dasha periods, Panchang limbs, Varga sign mapping, Ashtakavarga intermediate rows, strength components, and transit positions. Every comparison must first align location, UTC conversion, time scale, ayanamsha, node selection, house convention, boundary semantics, formula version and rounding. A VedAstro mismatch alone is not a reason to change AYNVORA.

## Rights, provenance, and reuse decision

- **VedAstro code:** repository MIT notice applies to repository software under that grant. AYNVORA copied/adapted no code in this phase; therefore no VedAstro attribution was added to runtime code.
- **Embedded text and rule data:** separate source provenance and rights are not established by the repository-level MIT file. No `Library/XMLData`, `Others/NotCode/Books`, prediction text, or datasets were copied.
- **Swiss Ephemeris:** `Library/Library.csproj` pins `SwissEphNet` `2.8.0.2`; `Core.cs` contains SwissEph calls and `Library/README.md` credits SwissEph/SWISSEPH. Their distribution and licensing terms are separate from the MIT license and were not cleared for AYNVORA. No Swiss Ephemeris dependency was added.
- Astrodienst's [official Swiss Ephemeris licensing documentation](https://www.astro.com/swisseph/swisseph.htm?lang=r&nho2=14) describes dual licensing (AGPL or a signed professional license) and says the choice is required before distributing software or activating a public service. AYNVORA has made no Swiss Ephemeris selection or purchase; integration remains unapproved and out of scope.
- The project README credits books by B.V. Raman and B. Suryanarain Rao as source material, but that attribution does not itself grant rights to reproduce their text or datasets. No such text was copied.
- **Other package dependencies:** the project file lists external packages. No dependency has been imported into AYNVORA, and this document is not a complete legal audit of their licenses.
- **Numerical correctness:** repository code is a comparison provider candidate, not ground truth. No VedAstro output is marked as `GROUND_TRUTH`.

## Audit limits

This is a source-path capability audit at one pinned revision, not a complete line-by-line algorithm review, test-suite execution, legal opinion, or reproducibility claim about deployed VedAstro API responses. Features not conclusively found are marked unverified in the gap matrix instead of inferred from the README.
