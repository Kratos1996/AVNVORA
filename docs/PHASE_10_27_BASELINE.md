# Phase 10.27 Baseline Status & Master Pending Feature Inventory

**Timestamp:** 2026-10-02T23:05:00+05:30  
**Phase:** 10.27 — AYNVORA Advanced Astrology Knowledge Expansion, KP / Lal Kitab / Prashna / Jaimini / Muhurta / Vastu Research, Production Knowledge Packs, SFT/LoRA Fine-Tuning & Evaluation.

---

## 1. System Baseline State (Post Phase 10.26)

| Subsystem | Exact Status | Implementation / Artifact |
| :--- | :--- | :--- |
| **NATIVE_AI** | `VERIFIED` | `libllama.so` + `libaynvora_llama_jni.so` via llama.cpp b3800+ |
| **MODEL** | `VERIFIED` | `qwen2.5-1.5b-instruct-q5_k_m.gguf` (1,285,494,304 bytes, SHA-256: `b46661073c18e5b56a41fa320975f866a00def1ff08feef4718e013258896f8c`) |
| **AI_GROUNDING** | `VERIFIED` | Primary source *Tājika Nīlakaṇṭhī* (1907) + `EvidenceFusion` + `PromptBuilder` |
| **TOOL_EXECUTION** | `VERIFIED` | Real deterministic tools: `getMuntha`, `getMunthaLord`, `getVarsheshwara`, `getSahams`, `getTajikaAspects`, `getMuddaDasha`, `getVarshaphal` |
| **OFFLINE_NATIVE** | `VERIFIED` | 100% on-device inference with network disabled, zero cloud fallback |
| **DEVICE** | `VERIFIED` | Physical Samsung Galaxy S23 Ultra (`SM-S918B`, `arm64-v8a`) via wireless ADB |
| **DETERMINISM** | `VERIFIED` | 3 consecutive Varshaphal runs produce byte-for-byte identical canonical JSON hashes |
| **FINE_TUNING** | `TRAINING_PREPARED` | `training/dataset-v2/verified_sft.jsonl` (100 verified examples, 17 categories); training runtime not executed |

---

## 2. Training Infrastructure Audit

- **Operating System:** macOS (Darwin arm64)
- **Host Python:** `/usr/local/Homebrew/bin/python3` (Python 3.14.3)
- **Deep Learning Frameworks:** `torch`, `transformers`, `peft`, `bitsandbytes`, `accelerate` are **NOT INSTALLED** on host machine.
- **Hardware Acceleration:** Apple Silicon MPS available in hardware; no CUDA runtime.
- **Policy Enforcement:** Per Section 28 & 40 instructions, because no valid local/remote deep learning training cluster with PyTorch/PEFT is currently active on the host, fine-tuning status remains strictly **`TRAINING_PREPARED`**. Model weights will **NOT** be fabricated.

---

## 3. Master Pending Feature Inventory

Grouped according to Phase 10.27 Section 2 specifications:

### Group 1: ASTROLOGY_CALCULATION
- **KP Cusps (Placidus / Semi-arc):** `UNSUPPORTED`
- **KP Star Lord / Sub Lord / Sub-Sub Lord Calculation:** `NOT_VERIFIED`
- **KP 249 Mathematical Subdivision Table:** `NOT_VERIFIED`
- **Yogini Dasha (36-year cycle: Mangala to Sankata):** `UNSUPPORTED`
- **Ashtottari Dasha (108-year cycle: Ardra/Krittika):** `UNSUPPORTED`
- **Chara Dasha (Jaimini sign-based Dasha progression):** `UNSUPPORTED`
- **Narayana Dasha / Kalachakra Dasha:** `UNSUPPORTED`
- **Upagrahas (Gulika, Mandi, Dhuma, Vyatipata, Parivesha, Indrachapa, Upaketu):** `UNSUPPORTED`
- **Solar & Lunar Eclipse Ingress & Contact Timing:** `UNSUPPORTED`

### Group 2: ASTROLOGY_KNOWLEDGE
- **Tajika Classical Knowledge:** `PRODUCTION` (`TAJIKA_V1` verified from 1907 *Tājika Nīlakaṇṭhī*)
- **Classical Parashari Principles:** `PARTIAL` (Panchanga, core planets/houses calculated; complex divisional rules partial)
- **Source Citation & Attribution Framework:** `PRODUCTION` (`AstroKnowledgeSourceRegistry`)

### Group 3: PREDICTION
- **Phaladesh (Event Ingress & Transit Reflection):** `PARTIAL` (Phase 10.20 event engine implemented; comprehensive classic rule pack pending)
- **Sade Sati / Panoti / Double Transit:** `UNSUPPORTED`
- **Tarabala / Chandrabala Daily Reflection:** `PARTIAL`

### Group 4: PRASHNA (Horary Astrology)
- **Prashna Query Moment Chart Calculation:** `PARTIAL` (Basic chart calculator available; dedicated Prashna pipeline unsupported)
- **Horary Significators & Ruling Planets:** `UNSUPPORTED`
- **KP Horary (Number 1–249 Query Seed):** `RESEARCH_ONLY`
- **Prashna Classical Interpretive Rules:** `RESEARCH_ONLY`

### Group 5: MUHURTA (Electional Astrology)
- **Hora (Planetary Hour Calculation):** `PARTIAL` (Sun cycle available; sunrise/sunset planetary hour division needs verified engine)
- **Choghadiya (7-part day/night divisions: Udveg, Char, Labh, Amrit, Kaal, Shubh, Rog):** `UNSUPPORTED`
- **Rahu Kalam, Yamaganda, Gulika Kalam:** `UNSUPPORTED`
- **Abhijit Muhurta (8th Muhurta of diurnal day):** `UNSUPPORTED`
- **Event-Specific Muhurta Rules (Marriage, Business, Griha Pravesh):** `RESEARCH_ONLY`

### Group 6: COMPATIBILITY (Synastry / Porutham)
- **Ashtakoota (Guna Milan 36-Point System: Varna, Vashya, Tara, Yoni, Graha Maitri, Gana, Bhakoot, Nadi):** `UNSUPPORTED`
- **South Indian 10/12 Porutham (Dina, Gana, Mahendra, Stree Deergha, Yoni, Rasi, Rasyadhipathi, Vashya, Rajju, Vedha):** `UNSUPPORTED`
- **Manglik Dosha & Koota Cancellation Overrides:** `RESEARCH_ONLY`

### Group 7: VASTU
- **Classical 16 Directions & Ashta-Dikpalas:** `RESEARCH_ONLY` (*Mayamatam*, *Brihat Samhita*)
- **Brahmasthan & Spatial Energy Balances:** `RESEARCH_ONLY`
- **Modern Commercial / Online Vastu Claims:** `REJECTED_UNVERIFIED` (Strict source-first policy)

### Group 8: JAIMINI ASTROLOGY
- **Chara Karakas (Atmakaraka to Darakaraka 7/8 planet schemes):** `UNSUPPORTED`
- **Arudha Padas (AL, UL - Upapada Lagna, A1 to A12):** `UNSUPPORTED`
- **Karakamsha & Swamsha:** `UNSUPPORTED`
- **Jaimini Rashi Aspects (Movable aspects Fixed except adjacent, etc.):** `UNSUPPORTED`
- **Jaimini Classical Sutra Knowledge Pack:** `RESEARCH_ONLY` (*Jaimini Upadesha Sutras*)

### Group 9: KP (Krishnamurti Paddhati)
- **KP Ayanamsha (Krishnamurti Ayanamsha ~50.2388" per year from 291 AD):** `UNSUPPORTED`
- **Placidus Semi-Arc Cuspal Tri-sections:** `UNSUPPORTED`
- **249 Subdivisions Table & Algorithm:** `NOT_VERIFIED`
- **Cuspal Sub Lord & Significator Determination (4-fold signification):** `RESEARCH_ONLY`
- **Ruling Planets & 1-249 Horary Seeds:** `RESEARCH_ONLY`

### Group 10: LAL KITAB
- **Planetary House Classifications (Kismat Jagane Wale, Soe Hue Grah, etc.):** `RESEARCH_ONLY`
- **Masnui (Artificial) Planets & Pukka Ghar Rules:** `RESEARCH_ONLY`
- **Remedies (Upaye) & Precautionary Invariants:** `RESEARCH_ONLY` (Only 1939/1942/1952 Pandit Roop Chand Joshi editions eligible for rights clearance)

### Group 11: NUMEROLOGY
- **Chaldean & Pythagorean Radical / Destiny / Name Numbers:** `PRODUCTION`
- **Lo Shu Grid & Directional Planes:** `PRODUCTION`

### Group 12: GITA
- **Bhagavad Gita 18-Chapter Verses & Socratic Reflection Engine:** `PRODUCTION`

### Group 13: PALMISTRY
- **Major Lines (Heart, Head, Life, Fate) Geometric Models:** `PRODUCTION`
- **Classical Hand Mounts & Texture Indices:** `PRODUCTION`

### Group 14: GEMSTONE
- **Planetary Gemstone Associations & Navaratna Guidance:** `PRODUCTION`

### Group 15: GARUDA PURAN
- **Puranic Philosophical & Eschatological Exegesis:** `PRODUCTION`

### Group 16: OTHER
- **Remote Web Search Provider (Opt-in with PII redaction):** `PARTIAL`
- **Semantic Vector Embeddings Index:** `PARTIAL`
