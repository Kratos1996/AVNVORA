# Phase 10.12A — Native AI Context Optimization and Gita Inference Reliability

**Status: PARTIALLY COMPLETE — compact grounded Gita inference now completes and validates on device; broad Phase 10.12A acceptance remains incomplete.**

## Scope and outcome

Phase 10.12A adds bounded, deterministic prompt construction for local inference, prompt diagnostics, cancellation that reaches llama.cpp during prompt prefill, timeout reporting, and a process-wide guard against overlapping native generations. The diagnostic screen no longer claims an unverified llama.cpp revision or estimates first-token latency. This report records what was implemented and what was actually demonstrated; unrun domains are not treated as passing.

The Gita prompt is materially smaller: the prior request measured about 940 tokenizer tokens / 3,504 formatted bytes; the final compact request measured 266 tokens / 1,094 formatted JNI bytes (71.7% fewer tokens and 68.8% fewer formatted bytes). The builder diagnostic is 1,012 bytes (estimated 253 tokens) and excludes ChatML wrappers. Byte-derived token counts are estimates; llama.cpp's tokenizer count is authoritative.

## Acceptance matrix

| Test | Prompt Tokens | Native Tokens | First Token | Total Latency | Validator | Execution Mode | Result |
|---|---:|---:|---:|---:|---|---|---|
| Short Smoke | 24 | 3 | 2,242 ms | 2,916 ms | N/A | LOCAL_NATIVE | PASS — native runtime regression passed with no fallback; text was “Hello there!” (it did not satisfy the five-word wording) |
| Gita Explain | 266 | 96 | 21,226 ms | 37,917 ms | VALIDATED | LOCAL_NATIVE | PASS — grounded Gita answer completed; fallbackUsed=false |
| Gita Life Connection | — | — | — | — | — | — | NOT RUN |
| Astrology | — | — | — | — | — | — | NOT RUN |
| Tarot | — | — | — | — | — | — | NOT RUN |
| Numerology | — | — | — | — | — | — | NOT RUN |
| Palmistry | — | — | — | — | — | — | NOT RUN |
| Gemstone | — | — | — | — | — | — | NOT RUN |
| Garuda | — | — | — | — | — | — | NOT RUN |

The exact-request Gita test selected BG 2.47 and BG 3.35. Compact-builder diagnostics recorded 1,012 bytes, estimated 253 tokens, 2 evidence items (2 unique), system section 158 bytes, evidence section 517 bytes, source metadata 71 bytes, user context 52 bytes, and question 50 bytes. After JNI formatting, llama.cpp measured 1,094 bytes and 266 tokens. Prompt decode took 21,217 ms; first token was decoded at 21,226 ms. Generation completed 96 tokens in 37,903 ms (2.53 tokens/sec); the complete pipeline took 37,917 ms. The validated response attributed BG 2.47 and framed the output as source plus reflection, connected it to the explicit career context, and did not assert future certainty. `fallbackUsed=false` and execution mode was `LOCAL_NATIVE`.

A separate bounded-run attempt before the final configuration allowed 30 seconds and was cancelled safely after prompt decode (18,045 ms) and 107 partial native tokens; the engine discarded partial output and returned deterministic fallback. This demonstrates the timeout/cancellation path. The final Gita pipeline now requests at most 96 output tokens, and the native engine has a 45-second hard timeout; the successful run finished below that bound.

After the short smoke, native logs also showed an additional 263-token prompt followed by 128 generated tokens in 44,809 ms. No matching test-runner result or validator record was emitted for that request, so it is recorded as an observed native generation only and is not counted as a second validated Gita acceptance test.

## Implementation

- Added `AynvoraAiContextBudget` and `AynvoraAiContextBuilder` with per-section bounds, domain filtering, priority ordering, evidence deduplication, a three-item evidence ceiling, compact provenance, deterministic formatting, and sizing categories.
- Routed local intelligence synthesis through the bounded builder and exposed last-prompt and response diagnostics. Empty caller evidence can be filled from the existing evidence retrieval path.
- Added native abort callback wiring and cancellation checks between prompt batches. Prompt prefill batch size is 64.
- Added a 45-second native inference watchdog and typed timeout/failure handling, with deterministic fallback reporting. Gita requests cap output at 96 tokens to bound generation time.
- Added a process-wide nonblocking generation mutex. Overlapping JNI generations return a busy sentinel, preventing concurrent llama.cpp access within the process.
- Updated the diagnostic screen to show upstream revision unavailable / provenance partial and to show unmeasured token and first-token values as “Not measured.”
- Added JVM tests for context budgeting and timeout handling.

## Reliability findings and limits

Two Gita broadcasts overlapped before the global guard was added. Device logs showed requests of 263 and 267 prompt tokens active at the same time; the process then exited with SIGSEGV at roughly 1.6 GB RSS. Subsequent bounded single runs on the guard-enabled APK either cancelled safely at 30 seconds or completed successfully with the final 45-second/96-token Gita settings. Neither run crashed. A dedicated device concurrency-rejection test was not run, so the guard's behavior is implemented and compiled but not separately device-verified.

The device was a Samsung Galaxy S23 Ultra (SM-S918B), Android 16 / API 36, arm64-v8a. The installed model remained Qwen 1.5B Q5_K_M. Available-memory readings around the final run were 3,467 MB before and 3,607 MB after (ActivityManager available-memory readings, not process PSS).

The vendored llama.cpp source has no independently verifiable upstream Git revision. The prior diagnostic's `b3600-android` label was unsupported; the screen now reports provenance as partial and does not invent a revision. The model checksum and toolchain baseline are recorded in [Phase 10.12 native implementation report](PHASE_10_12_NATIVE_AI_IMPLEMENTATION_FINAL_REPORT.md).

## Verification performed

- `./gradlew -Pkotlin.incremental=false :aynvora-core:jvmTest :aynvora-qa-core:jvmTest :ui:jvmTest jvmTest :androidApp:assembleDebug` — **BUILD SUCCESSFUL**.
- After the final diagnostic-screen edit, `./gradlew -Pkotlin.incremental=false :ui:jvmTest :androidApp:assembleDebug` — **BUILD SUCCESSFUL**.
- `git diff --check` — **passed**.
- Connected-device exact Gita run on the final build — 266 tokenizer prompt tokens; 96 native tokens; `VALIDATED`; `LOCAL_NATIVE`; fallback false; 37,917 ms total.
- Connected-device “Say hello in five words” smoke on the final build — 24 prompt tokens; 3 native tokens; `LOCAL_NATIVE`; fallback false; 2,916 ms total; output “Hello there!”.
- Earlier 30-second bounded Gita run — cancellation reached native generation; partial tokens were discarded and deterministic fallback returned without a crash.
- Additional post-smoke native generation — 263 prompt tokens and 128 output tokens in 44,809 ms, but no corresponding runner/validator record; not counted as a validated test.

## Not verified / remaining blockers

- Gita Explain acceptance passed for the captured case. Gita Life Connection and a second distinct post-success Gita prompt were not separately validated. One additional native generation was logged without a test-runner or validator result and is not treated as acceptance evidence.
- Gita Life Connection and all listed non-Gita domain requests were not run.
- Offline behavior, repeated sequential model load/unload, lifecycle recovery, and dedicated device concurrency rejection were not verified in this phase.
- Interaction Sentinel was not implemented; the test mechanism remains an ad-hoc broadcast runner and logs.
- Device privacy/analytics payload behavior was not independently audited during these runs.
- The diagnostic screen changes were compiled and packaged; a final navigational/UI inspection on device was not performed.
- Upstream llama.cpp provenance remains unavailable.

## Final disposition

The compact Gita Explain path now completes natively and passes the existing validator on the tested device, while the short native smoke also succeeds. Phase 10.12A remains **incomplete** because all-domain coverage, offline and privacy checks, lifecycle and regression checks, Interaction Sentinel, and several requested device scenarios remain unverified.
