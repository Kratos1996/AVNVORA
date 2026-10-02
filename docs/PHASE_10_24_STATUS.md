# Phase 10.24 Status

## 1. VARSHAPHAL CALCULATION STATUS

PARTIAL. `AynvoraSdk.calculateVarshaphal(request, targetYear)` calculates the return and annual chart. The result carries explicit status fields and diagnostics for components that are not implemented or verified.

## 2. SOLAR RETURN STATUS

IMPLEMENTED for supported existing engine profiles/ayanamsas. Solves the natal sidereal Sun longitude recurrence over the target UTC Gregorian year using the existing Meeus Sun and ayanamsa calculators. Output records UTC Julian Day, UTC timestamp, provider, model version, time-scale caveat, and accuracy diagnostics. No Delta-T conversion is applied; the existing Sun implementation documents about 0.01 degree accuracy near J2000.

## 3. ANNUAL CHART STATUS

PARTIAL. The SDK recalculates the full existing chart at the return instant and assembles it into the shared `AstroChart` model, including 12 houses and placements. Annual-chart conventions beyond the existing configured engine have not been independently verified against a Tajika authority.

## 4. MUNTHA STATUS

NOT_VERIFIED. The bundled TAJIKA_V1 material contains house indications, not a verified annual progression algorithm. No caller-supplied fact is represented as an engine calculation.

## 5. MUNTHA LORD STATUS

NOT_VERIFIED. Depends on a verified calculated Muntha sign.

## 6. VARSHESHWARA STATUS

UNSUPPORTED. The complete candidate selection and acceptance criteria are not available in the verified rule set. A Sun indication in TAJIKA_V1 is not used to select the year lord.

## 7. SAHAM STATUS

UNSUPPORTED. No production Saham formulas are implemented.

## 8. TAJIKA ASPECT STATUS

UNSUPPORTED. The ordinary aspect engine is not substituted for Tajika aspects.

## 9. MUDDA DASHA STATUS

UNSUPPORTED. No annual period sequence is calculated.

## 10. KNOWLEDGE PACK

- Sources: 2 records
- Chunks: 3
- Rules: 8
- Citations: source page/chapter references recorded on the scoped rules; no new production rule added in this phase
- Pack: TAJIKA_V1, version 1.0.0, checksum `513be82525a8a3b846a86b6aaf770ef1575b61fcafe196e0f41de0bd4213301d`
- Validation command: `python3 knowledge/tajika/build_pack.py` completed and emitted the same checksum.

## 11. TOOL EXECUTION

- Fixture tests: the existing scenario tests remain fixture-planner tests.
- Real calculator tests: `SolarReturnEngineTest` exercises the actual `AynvoraAstroEngine`; `VarshaphalSdkIntegrationTest` invokes the public SDK and validates the shared annual chart.
- Actual tool traces: NOT RUN. There is no registered Varshaphal calculator adapter in the production `AynvoraAiToolExecutor` yet; the SDK calculator API is separate from executor dispatch.
- Tool execution status: PARTIAL.

## 12. AI GROUNDING

PARTIAL. Existing grounded executor and validator are present, but no actual Varshaphal tool call or native-model end-to-end request was run. No fabricated traces or inference results are claimed.

## 13. NATIVE AI

NOT_VERIFIED. The Android arm64 `libllama.so` exists in the build intermediates. The catalog identifies `qwen2.5-0.5b-instruct-q4_k_m.gguf` (491,400,032 bytes; SHA-256 `74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db`), but no model artifact is present in the workspace and `adb` is unavailable. No connected-device load, prompt, token generation, latency, or memory trace exists.

## 14. TRAINING

- Dataset: `training/dataset-v1/verified_sft.jsonl` contains 8 examples; it was not expanded because the unsupported engine outputs cannot supply grounded examples for the requested coverage.
- Dataset SHA-256: `3078fef96deb29350b6930591303aa020c195d91477fed5f8969716b156b5018`.
- `training/verified_sft.jsonl` is empty (SHA-256 `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`).
- Training status: TRAINING_PREPARED; no fine-tuning was run.

## 15. OFFLINE TEST

NOT RUN as a network-disabled acceptance test. The solar return and annual chart calculations use local engine code; offline search/native executor acceptance remains unverified.

## 16. DESKTOP TEST

The headless SDK path was invoked by a JVM integration test. `:desktopApp:assemble` PASS. Local AI inference was not verified.

## 17. ANDROID TEST

Android ARM64 native library is present in build intermediates. `:androidApp:assembleDebug` PASS. Device execution is NOT_VERIFIED because no `adb` command/device is available and the cataloged GGUF file is absent.

## 18. BUILD / TEST RESULTS

PASS: `./gradlew :astro-engine:jvmTest :aynvora-core:jvmTest :aynvora-data:jvmTest test :desktopApp:assemble :androidApp:assembleDebug` (183 actionable tasks; 49 executed, 134 up-to-date). `git diff --check` PASS. Focused solar-return and SDK integration tests also PASS.

## 19. REMAINING BLOCKERS

- Verified sources for Muntha annual progression, complete Varsheshwara selection, Saham formulas, Tajika aspects, and Mudda Dasha.
- A production Varshaphal registered-tool adapter and real executor integration traces.
- A 50-example dataset built from actual outputs covering the still unsupported topics.
- The exact GGUF model artifact and an Android ARM64 device/ADB connection for native inference acceptance.
- Offline network-disabled and full end-to-end AI acceptance runs.
