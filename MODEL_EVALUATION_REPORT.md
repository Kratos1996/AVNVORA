# AYNVORA Model Evaluation Report (Phase 10.29)

**Audit Date:** October 3, 2026  
**Auditor:** Antigravity Autonomous Systems Engineering & Compliance  
**Standard:** Strict Zero-Fabrication, True Model Evaluation Isolation, Hardware Verification  

---

## 1. Executive Summary & Evaluation Status

| Metric Domain | Status | Notes |
| :--- | :---: | :--- |
| **SYSTEM_GROUNDING_EVALUATION** | **VERIFIED** | Completed in Phase 10.28 across 100 holdout cases comparing ungrounded base model vs. deterministic tool-grounded engine. |
| **MODEL_EVALUATION (Base vs Fine-Tuned)** | **NOT_RUN** | Strictly `NOT_RUN` per Phase 10.29 Section 39. Training did not execute on host because hardware is Intel `x86_64` (lacks Apple Silicon MLX/Metal framework). Zero fabricated scores. |
| **FINE_TUNING** | **TRAINING_PREPARED** | Dataset v4 audited (508 examples, 0% leakage, train/val/test splits created and hashed). Ready for launch on Apple Silicon M-series. |
| **MODEL_DEPLOYMENT** | **PRODUCTION** | Preserved active production model `qwen2.5-1.5b-instruct-q5_k_m.gguf` (SHA-256: `b46661...`). No unverified adapter deployed. |

---

## 2. Fundamental Distinction: System Grounding vs. Model Evaluation

In accordance with Phase 10.29 Section 16:
- **System Grounding Evaluation (Phase 10.28 - VERIFIED):**
  Evaluated the end-to-end SDK pipeline (`AiAssistant` $\to$ `AynvoraAiToolExecutor` $\to$ Deterministic Astrological Engine $\to$ `PromptBuilder` $\to$ Grounded Output). This verified that runtime tool calling and deterministic calculation injection prevent hallucinations and preserve exact numbers.
- **True Model Evaluation (Phase 10.29 - NOT_RUN):**
  Requires direct head-to-head inference between:
  - **Model A:** Base Model (`Qwen2.5-1.5B-Instruct`)
  - **Model B:** Fine-Tuned Model (`Qwen2.5-1.5B-Instruct + AYNVORA LoRA Adapter`)
  Both running with identical prompts, identical system instructions, and identical tool registries against the held-out set (`holdout_validation_v5.jsonl`).
  Because no fine-tuning adapter was generated on this Intel x86_64 host, this evaluation is truthfully designated **NOT_RUN**.

---

## 3. Dataset Audit & Cryptographic Integrity

Holdout and training splits have been audited and cryptographically locked:

| Dataset Artifact | Examples | SHA-256 Checksum | Purpose |
| :--- | :---: | :--- | :--- |
| `verified_sft.jsonl` | 508 | `34f89af97dd5e1012a37bd36100ecde040081cfa568d2664106597a43a4a1a5c` | Curated SFT dataset |
| `train.jsonl` | 406 | `9dbffa6a0443f40f1425b536159b1fbdc2239df169f8047dd0aad030d618dd4b` | 80% training split (Seed 42) |
| `valid.jsonl` | 50 | `1c89ece312f5dbdb4f1b60212f63f103a79e55c9ff940b9c43a041fe6cac0fc2` | 10% validation split (Seed 42) |
| `test.jsonl` | 52 | `d4ea6b204a388b40a86cc94904a310ad95fa11fc07b6ddc0474d171458db4da3` | 10% test split (Seed 42) |
| `holdout_validation_v5.jsonl` | 100 | `aca6f25a974cc9efcd73ecb4bf45f6ea30d28814731c12df9e20645db71abc29` | 100% held-out test set (0% training overlap) |

---

## 4. Hardware & Toolchain Blocker Record

Detailed findings recorded in `training/ENVIRONMENT_REPORT.md`:
1. **Physical Host:** Intel MacBook Pro (`MacBookPro16,2`), CPU: `Intel(R) Core(TM) i5-1038NG7 CPU @ 2.00GHz` (4 cores / 8 threads), Architecture: `x86_64`.
2. **Apple Silicon Incompatibility:** MLX / MLX-LM requires Apple Silicon (`arm64`) unified memory architecture and Metal 3 hardware shaders. Pip installation fails on `x86_64`: `ERROR: No matching distribution found for mlx`.
3. **Python Incompatibility:** Installed Homebrew Python is version 3.14.7, which lacks wheels for machine learning frameworks.
4. **Memory Constraint:** Available free physical RAM is ~1.78 GB, which is insufficient for backward pass gradient computation of a 1.5B parameter model.

---

## 5. Candidate Deployment & Rollback Gate

1. **Current Production Model Preserved:** `qwen2.5-1.5b-instruct-q5_k_m.gguf` remains active in production and verified on physical hardware (Samsung Galaxy S23 Ultra SM-S918B).
2. **No Automatic Replacement:** Per Phase 10.29 Section 28 & 35, candidate models may only be promoted to `CANDIDATE` after physical training, checksum verification, GGUF conversion, and device benchmarks.
3. **Rollback Safety:** The model catalog (`AiModelCatalog.kt`) and storage repository maintain distinct keys for active production and candidate models.
