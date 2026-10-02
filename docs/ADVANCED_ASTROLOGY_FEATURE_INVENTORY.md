# Advanced Astrology Feature Inventory

Statuses describe this SDK after Phase 10.23. `DISCOVERED` means a research lead exists; `NOT_VERIFIED` means no production source/rule gate passed; `UNSUPPORTED` means the SDK does not expose a verified calculation/knowledge pack today. Tajika production content is limited to the specifically cited and rights-recorded `TAJIKA_V1` subset; it does not make full Varshaphal available.

| Feature | Source | Reference implementation | Source rights | AYNVORA status | Calculation needed | Knowledge needed | Tests needed |
|---|---|---|---|---|---|---|---|
| KP ayanamsha | Research candidates only | MayaAstrolib; vedic-calc | MIT repo with separate Swiss dependency; AGPL repo | UNSUPPORTED | Yes | Yes | Independent fixtures, boundary cases |
| KP cusps / Placidus | Research candidates only | MayaAstrolib; vedic-calc | As above | UNSUPPORTED | Yes | Method and assumptions | Cross-implementation fixtures |
| KP star/sub/sub-sub lords; 249 divisions | Research candidates only | MayaAstrolib; vedic-calc; KP-Astrology discovery lead | Data rights unresolved | NOT_VERIFIED | Yes | Verified algorithm/table | 0°/360°, all boundaries, gaps/overlaps, count=249 |
| KP significators / ruling planets / timing / Dasha / transit / horary 1–249 | None accepted | Candidate repos | Not cleared | UNSUPPORTED | Yes | Tradition-specific rules | Golden fixtures, timing tests |
| Lal Kitab houses, conditions, exceptions, remedies | Historical editions required | None accepted | Edition/text rights unresolved | UNSUPPORTED | Some rules | Yes | Rule exceptions, provenance, translation checks |
| Solar return / annual chart | `TAJIKA_V1` supports selected annual indications, but no calculation source | none activated | Selected source text CC BY-SA 4.0; scan public domain in India/US | UNSUPPORTED | Yes | Partial | Return instant, timezone, location fixtures |
| Muntha house indications | `TAJIKA_V1`, 1907 Tājika Nīlakaṇṭhī selected pages 122/124 | First-party rule evaluator accepts caller-calculated house/facts | CC BY-SA 4.0 transcription adaptation, source refs retained | PARTIAL; house positions are not calculated | Yes | Selected source rules ingested | Independent annual-chart fixtures |
| Muntha Lord / Varsheshwara | `TAJIKA_V1` includes selected Sun-as-Varshesha indications only | `getVarsheshaSun` evaluates supplied strength; does not select year lord | CC BY-SA 4.0 transcription adaptation | PARTIAL | Yes | Selected source rules ingested | Lord selection and strength fixtures |
| Sahams / Tajika aspects / Mudda Dasha / Patyayini | No production source/engine accepted | Research candidates only | Not cleared for these topics | UNSUPPORTED | Yes | Yes | Formula-specific golden fixtures |
| Phaladesh | Existing Phase 10.20 event framework | AYNVORA event catalog/engine | First-party code | PARTIAL; no verified rule pack | Per feature | Yes | Sourced rule + deterministic evidence |
| Yogas / Doshas | No accepted source | None | Unresolved | UNSUPPORTED | Rule evaluation | Classical definitions and exceptions | Conflicting definitions, source fixtures |
| Jaimini / Chara Karakas / Arudha / Narayana Dasha / Chara Dasha | No accepted source | None | Unresolved | UNSUPPORTED | Yes | Yes | Convention and tie-break fixtures |
| Yogini / Ashtottari / Kalachakra Dasha | No accepted source | None | Unresolved | UNSUPPORTED | Yes | Yes | Period balance/boundary fixtures |
| Muhurta / Hora / Choghadiya / Rahu Kalam / Abhijit | Existing panchang foundations; no approved rules pack | AYNVORA panchang components | First-party calculations; rule sources open | PARTIAL | Yes | Yes | Location/daylight/timezone fixtures |
| Sade Sati / Panoti / Tarabala / Vedha / Double Transit | No accepted source | None | Unresolved | UNSUPPORTED | Yes | Yes | Transit conventions and edge cases |
| Ashtakoota / South Indian Porutham | No accepted source | None | Unresolved | UNSUPPORTED | Yes | Yes | Tradition- and region-specific fixtures |
| Prashna | No accepted source | KP horary candidate only | Unresolved | UNSUPPORTED | Yes | Yes | Question-time fixtures |
| Sphutas / Upagrahas / Gulika / Mandi | No accepted source | None | Unresolved | UNSUPPORTED | Yes | Yes | Sunrise/sunset and regional convention fixtures |
| Eclipse calculations | Astronomy source review required | None accepted in this phase | Ephemeris licensing review required | UNSUPPORTED | Yes | Limited | Independent ephemeris checks |
| Source-aware offline search and evidence | First-party SDK + `TAJIKA_V1` | `KnowledgeSearchEngine`, `KnowledgeRetriever` | First-party runtime; CC BY-SA pack adaptation | IMPLEMENTED for the scoped pack | No | Scoped content available | Provenance, filters, rights gate |
| Optional local semantic search | Host-provided model/index | First-party interfaces/index | Host model terms apply | PARTIAL | No | Embeddings optional | Namespacing, ranking, empty-index |
| Remote web evidence | Host-configured legal provider | First-party adapter interface | Provider terms apply | PARTIAL | No | Search provider and source assessment | Sanitization, allow/block, TTL |
| Registered AI tool execution | First-party SDK | `AynvoraAiToolExecutor` | First-party | PARTIAL; orchestration and simulated 30-scenario coverage, native model execution not verified | Per tool | Production knowledge available only for scoped Tajika rules | Real model/native and offline tests |
