# Phase 10.12 — Native Android AI Runtime Implementation Report

**Status: core native Android runtime verified; Phase 10.12 acceptance remains incomplete.**

The final device smoke test proved that the packaged `libllama.so` loaded, JNI called llama.cpp, the on-device GGUF model generated tokens, and the request completed in `LOCAL_NATIVE` mode. The larger Gita request did not complete within the observed run, and broad feature, offline, privacy, failure, and lifecycle acceptance checks remain unverified.

## Host and build environment

| Item | Observed value |
|---|---|
| Device | Samsung Galaxy S23 Ultra (`SM-S918B`), Android 16 / API 36, `arm64-v8a` |
| Android NDK | `26.1.10909125` |
| CMake | `3.22.1` |
| NDK Clang | `17.0.2` (based on r487747d) |
| Java | `21.0.11` |
| Gradle | `9.7.1` |
| Native build | Android `arm64-v8a`; OpenMP enabled; `GGML_NATIVE=OFF`; Debug compilation flags verified as `-O3 -g` |

The llama.cpp sources are vendored under `native/llama.cpp`, not a Git submodule. The project source snapshot is identified by repository commit `9e6d03ed95c19f18d88174fa36c25a783aea9302` and `HEAD:native/llama.cpp` tree `a03fdbc9b85db7438516fd725ee6e115d7acc03c`. The vendored tree has no independent Git history, and CMake reported that it could not find a Git repository to generate build information. Therefore, an upstream llama.cpp commit/tag cannot be confirmed. The current diagnostics label `b3600` is static and was not independently validated as source provenance. The vendored license is MIT.

## Packaged native artifacts

The Debug APK was built and installed on the connected device. Its arm64 libraries are:

| APK entry | Bytes |
|---|---:|
| `lib/arm64-v8a/libllama.so` | 1,370,896 |
| `lib/arm64-v8a/libggml.so` | 719,888 |
| `lib/arm64-v8a/libomp.so` | 940,600 |
| `lib/arm64-v8a/libc++_shared.so` | 1,330,832 |

- `libllama.so` SHA-256: `c57ffecbfb8af2fd7180bfc7e19e94e7a74fcf4c1335deae77f8717cb23edd8e`
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk` (65,773,229 bytes)
- APK SHA-256: `b7a84644a7ddabd2da90934c05a1d2739491db4ab0c077bc76335d0ce5d0b328`
- ELF inspection identified the library as ELF64 AArch64 shared object (DYN).

## Model artifact and on-device load

| Field | Value |
|---|---|
| Catalog ID / version | `qwen2.5-1.5b-instruct-q5_k_m` / `1.0.0` |
| Format / license | GGUF / Apache-2.0 |
| Size | 1,285,494,304 bytes |
| SHA-256 | `b46661073c18e5b56a41fa320975f866a00def1ff08feef4718e013258896f8c` |
| Device path | `/data/data/com.aynvora.app/files/models/ondevice/qwen2.5-1.5b-instruct-q5_k_m.gguf` |
| Catalog source | `https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q5_k_m.gguf` |

The installed model’s size and checksum matched the catalog, and native model loading returned a valid context. The source URL uses the mutable `main` branch; checksum validation pins the bytes used here, but the URL itself does not pin an immutable upstream revision.

## Native smoke test evidence

The test runner directly exercised `AiInferenceEngine` with a short request (“Say hello in five words.”, 16-token limit, temperature 0). Captured device logs and the runner result recorded:

- Native library status: `VERIFIED (libllama.so loaded)`.
- JNI status: `LOADED (libllama.so verified and linked via JNI)`.
- Execution mode: `LOCAL_NATIVE`.
- Result: `Hello there!`.
- Actual native-generated token count: 3 (read from the JNI context counter).
- Prompt: 24 tokens; prompt decode: 2,000 ms.
- First token: 2,008 ms; total generation: 2,430 ms; observed rate: 1.23 tokens/sec.
- Device app memory reported by the runner: 3,739 MB before and 3,733 MB after.

This satisfies the critical runtime criteria for a real native request: the packaged library loaded, JNI executed, the matching local GGUF loaded, native tokens were generated, and the request completed as `LOCAL_NATIVE` without fallback on that request.

## Gita request and remaining acceptance work

The Gita request used a large grounded prompt. Device logs recorded a 3,504-byte prompt (940 tokens); prompt decoding completed after 966,173 ms. The native path then sampled and decoded an output token, but no completed response, validator result, or fresh test-runner result was captured. A second request with a 3,484-byte / 938-token prompt also began, and its origin could not be established from the captured evidence. The device disconnected while attempting to stop the lingering run. **Gita generation and validation are therefore not verified.**

The captured memory readings do not establish memory pressure as the cause. The runtime presently does not demonstrate that cancellation can interrupt an in-progress native `llama_decode`; the long prompt decode remained active until the native call returned. Do not treat timeout/cancellation behavior as accepted until separately tested and, if needed, implemented.

| Acceptance area | Status | Evidence / gap |
|---|---|---|
| Android arm64 native library packaged and loadable | **VERIFIED** | APK entries, ELF inspection, and device JNI logs |
| Exact local model artifact and native load | **VERIFIED** | Device path, byte size, checksum, successful context creation |
| Native token generation and `LOCAL_NATIVE` result | **VERIFIED** | Direct short-request runner and native logs |
| Real native token count and timing diagnostics | **VERIFIED** | JNI counter and native timing logs for smoke request |
| Gita grounded response and validator | **NOT VERIFIED** | Long prompt prefill observed; no complete result or validator artifact |
| Astrology, Tarot, Numerology, Palmistry, Gemstone, Garuda | **NOT VERIFIED** | No device acceptance results captured |
| Cross-feature and repeated lifecycle/model-load checks | **NOT VERIFIED** | No device acceptance results captured |
| Offline operation and privacy behavior | **NOT VERIFIED** | No network-disabled or privacy acceptance run captured |
| Forced native failure and fallback behavior | **NOT VERIFIED** | No device failure injection / fallback result captured |
| Dynamic authoritative diagnostic status | **NOT VERIFIED** | Native status was observed in smoke run; full diagnostic behavior was not validated |
| Device regression acceptance | **NOT VERIFIED** | JVM suites and Android Debug assembly passed; full device regression suite was not run to completion |
| Upstream llama.cpp source revision provenance | **UNVERIFIED** | Vendored tree has no independent Git metadata; build-info generation warned Git was unavailable |

## Changes and checks completed

The implementation work in this pass forwards the requested token limit through the local intelligence path, records the actual native token count through JNI, adds native generation-stage and timing diagnostics, and compiles the llama/ggml Debug targets with `-O3 -g`. The direct Android smoke runner uses a minimal prompt to exercise the native engine independently of a large knowledge prompt. A common test verifies that the native-reported token count is surfaced rather than inferred from whitespace.

Completed checks:

- `:aynvora-core:jvmTest`, `:aynvora-qa-core:jvmTest`, `:ui:jvmTest`, and root `jvmTest` passed in the recorded run.
- Android Debug APK assembly and installation succeeded.
- The connected-device direct native smoke request succeeded as described above.
- `git diff --check` passed.

Phase 10.12 remains incomplete until the long-form Gita request returns and passes its validator, and the remaining device acceptance areas are run and recorded. This report does not claim those checks passed.
