# AYNVORA MLX / Hardware Environment Audit Report (Phase 10.29)

**Audit Date:** October 3, 2026  
**Auditor:** Antigravity Autonomous Systems Engineering & Compliance  
**Standard:** Strict Zero-Fabrication, Physical Hardware Verification, Truthful Status Gate

---

## 1. Physical Host Hardware Specifications

| Specification Field | Detected Value | Audit Verification Method | Status |
| :--- | :--- | :--- | :--- |
| **Machine Model** | `MacBookPro16,2` | `sysctl hw.model` | VERIFIED |
| **CPU Architecture** | `x86_64` (Intel 64-bit) | `uname -m`, `arch` | VERIFIED |
| **CPU Brand & Model** | `Intel(R) Core(TM) i5-1038NG7 CPU @ 2.00GHz` | `sysctl -n machdep.cpu.brand_string` | VERIFIED |
| **CPU Cores / Threads** | 4 Cores / 8 Threads (`hw.ncpu: 8`) | `sysctl hw.ncpu` | VERIFIED |
| **Total Physical RAM** | 16,777,216 KB (~16.0 GB) | `sysctl hw.memsize` | VERIFIED |
| **Currently Free RAM** | ~1.78 GB unallocated (14 GB in use by system processes) | `top -l 1` | VERIFIED |
| **Available Disk Storage** | 336 GiB available on `/System/Volumes/Data` | `df -h .` | VERIFIED |
| **Operating System** | macOS 26.7 (Darwin 25.6.0, Build 25G229) | `sw_vers`, `uname -a` | VERIFIED |

---

## 2. Python & Training Backend Audit

| Component | Target Requirement | Detected Environment | Compatibility Audit |
| :--- | :--- | :--- | :--- |
| **Python Version** | Python 3.11 or 3.12 | Python 3.14.7 (`/usr/local/Homebrew/bin/python3`) | **INCOMPATIBLE** (Python 3.14 has no pre-built wheels for ML training packages) |
| **Pip Version** | >= 23.0 | Pip 26.2.1 | VERIFIED |
| **MLX Framework** | `mlx` (Apple Silicon ML) | Not Available on host | **BLOCKED** (MLX requires Apple Silicon `arm64`; zero distributions for `x86_64`) |
| **MLX-LM Tooling** | `mlx-lm` | Not Available on host | **BLOCKED** (Requires `mlx` and Apple Silicon Metal Performance Shaders) |
| **PyTorch Fallback** | `torch >= 2.2.0` | Not Available on host | **BLOCKED** (PyTorch has no binary wheels for Python 3.14; CPU memory insufficient) |
| **Target Base Model** | `Qwen/Qwen2.5-1.5B-Instruct` | Apache-2.0 Open Weights | Specified |
| **Production Runtime Model** | `qwen2.5-1.5b-instruct-q5_k_m.gguf` | 1,285,494,304 bytes, SHA-256 verified | Preserved in production |

---

## 3. Training Execution Blocker Analysis (Section 39 Compliance)

Per Phase 10.29 Section 39:
> "DO NOT fabricate. Return: `FINE_TUNING = TRAINING_PREPARED`, `MODEL_EVALUATION = NOT_RUN`. And report exact blocker: Python version, missing package, memory, model format, MLX issue, dataset issue or other runtime blocker. Do not create fake: loss, checkpoint, adapter, evaluation, speed."

### Primary Blocker 1: Hardware Architecture Mismatch (Intel x86_64 vs. Apple Silicon ARM64)
- **Investigation:** Phase 10.29 specifies an Apple Silicon MLX/MLX-LM training backend utilizing Metal unified memory.
- **Physical Reality:** The host workstation is an Intel-based MacBook Pro (`MacBookPro16,2`) with an `Intel(R) Core(TM) i5-1038NG7 CPU @ 2.00GHz` running on `x86_64`.
- **Finding:** MLX is proprietary to Apple Silicon (`arm64`) architectures (M1/M2/M3/M4). PyPI explicitly distributes `mlx` and `mlx-lm` wheels only for `macosx_*_arm64`. Pip installation dry-run returns: `ERROR: No matching distribution found for mlx`.

### Primary Blocker 2: Python Version Toolchain Gap (Python 3.14.7)
- **Investigation:** System default Homebrew Python is version 3.14.7 (pre-release).
- **Physical Reality:** ML frameworks (PyTorch, PEFT, Transformers, MLX) support Python 3.10–3.12. Dry-run installation of PyTorch on Python 3.14 returns: `ERROR: Could not find a version that satisfies the requirement torch (from versions: none)`.

### Primary Blocker 3: Available Working Memory
- **Investigation:** Fine-tuning a 1.5B parameter model with LoRA requires at least 8–12 GB of dedicated memory for model weights, gradients, optimizer states (AdamW), and activation cache.
- **Physical Reality:** The machine currently has 14 GB allocated out of 16 GB, with only ~1.78 GB unused memory. Any full-parameter or 16-rank LoRA backpropagation would encounter an Out-Of-Memory (OOM) kernel termination.

---

## 4. Timestamps & Certification

- **Training Investigation Start:** `2026-10-03T23:32:39+05:30`
- **Training Environment Audit Completed:** `2026-10-03T23:37:10+05:30`
- **Status Gate Decision:**
  - `FINE_TUNING = TRAINING_PREPARED`
  - `MODEL_EVALUATION = NOT_RUN`
  - `SYSTEM_GROUNDING_EVALUATION = VERIFIED` (Maintained from Phase 10.28)
  - `MODEL_DEPLOYMENT = PRODUCTION` (Maintained on `qwen2.5-1.5b-instruct-q5_k_m.gguf`)
