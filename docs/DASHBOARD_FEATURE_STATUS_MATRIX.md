# AYNVORA DASHBOARD FEATURE STATUS MATRIX
## Phase 10.34 Production Reconciliation and Runtime Truth

| Feature | Current Runtime | Backend | Knowledge | Source | Rights | SDK | Tool | Offline | Persistence | UI | AI Grounding | Final Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Astrology** | Full Workflow | AstroEngine (Parashari, Tajika, Jaimini, KP, Panchanga) | AstroKnowledgePack, TajikaPack, JaiminiPack | BPHS, Phaladeepika, Tajika Neelakanthi, Jaimini Sutras | Verified Public Domain | Headless SDK | `calculateBirthChart`, `calculateDasha`, etc. | 100% Offline | Room DB (Saved Kundali) | Full-screen interactive chart & tabs | Grounded | **PRODUCTION** |
| **Tarot** | Full Workflow | TarotRepository, TarotSessionStorage | 78 Archetypes, Celtic Cross, Three-card | RWS (1909) Public Domain | Verified Public Domain | Headless SDK | `drawTarotCard`, `analyzeSpread` | 100% Offline | DriverTarotSessionStorage | Full-screen card drawing & reflection | Grounded | **PRODUCTION** |
| **Numerology** | Full Workflow | NumerologyRepository | Pythagorean & Chaldean systems | Cheiro, Sepharial, Balliett classical texts | Verified Public Domain | Headless SDK | `calculateNumerology` | 100% Offline | Room DB (History) | Full-screen calculator & profiles | Grounded | **PRODUCTION** |
| **Palmistry** | Full Workflow | PalmSessionRepository | Hastrekha Samhita Mount & Line analysis | Classical Hastrekha texts | Verified Public Domain | Headless SDK | `analyzePalmLine` | 100% Offline | DriverPalmSessionStorage | Full-screen palm scanner & line analysis | Grounded | **PRODUCTION** |
| **Gemstone** | Full Workflow | GemstoneRepository | Navaratna wearing rules & lab provenance | Garuda Purana Ratna Pariksha, Brihat Samhita | Verified Public Domain | Headless SDK | `recommendGemstones` | 100% Offline | Room DB (Inventory) | Full-screen inventory & recommendation | Grounded | **PRODUCTION** |
| **Bhagavad Gita** | Full Workflow | RoomGitaRepository | 701 Sanskrit verses, translations | Gita Press & Public Domain recensions | Public Domain | Headless SDK | `searchGita` | 100% Offline | Room DB (701 Verses) | Full-screen chapter/verse reader | Grounded | **PRODUCTION** |
| **On-Device AI** | Full Workflow | Native Llama.cpp JNI + Fallback | Orchestrator, Evidence Graph | Local Knowledge Packs & Engine Output | Proprietary Engine + Open Models | Headless SDK | AiToolRegistry (All Domain Tools) | 100% Offline | Local Storage | Full-screen Assistant & Diagnostics | Grounded | **PRODUCTION** |
| **Garuda Puran** | Reconciled Full Workflow | ContentBackedGarudaPuranRepository | 16 Saroddhara Chapters, 10 Topics | Ernest Wood & S.V. Subrahmanyam (1911) | Public Domain (1911) | Headless SDK | `getGarudaPuran` | 100% Offline | Room DB / Bundled Package | Full-screen Chapter/Topic Browser & PDF | Grounded | **PRODUCTION** |
| **Daily Guidance** | Full Workflow | DailyGuidanceService | Daily Panchanga, Muhurta, Routines | Surya Siddhanta, Classical Panchanga | Verified Public Domain | Headless SDK | `getDailyGuidance` | 100% Offline | Cache / Room DB | Full-screen Today, Panchanga, Windows | Grounded | **PRODUCTION** |
| **Wallpaper Studio** | Full Workflow | WallpaperStudioService | Sacred Geometry, Mandala, Constellations | Traditional Vedic Sacred Art Patterns | Public Domain / Original Code | Headless SDK | `getWallpaperTemplates`, `generateWallpaperSpecification` | 100% Offline | Local Storage | Full-screen Template & Preview Canvas | Grounded | **PRODUCTION** |
| **Rudraksha** | Full Workflow | RudrakshaRepositoryImpl | 1–14 Mukhi Classification & Deities | Shiva Purana (Vidyeshvara), Padma Purana | Public Domain | Headless SDK | `getRudraksha` | 100% Offline | Room DB (Favorites) | Full-screen Mukhi Catalog & Details | Grounded | **PRODUCTION** |
| **Jadi / Sacred Roots** | Full Workflow | JadiRepositoryImpl | 9 Planetary Sacred Roots | Atharva Veda, Classical Jyotish Herbal Texts | Public Domain | Headless SDK | `getJadi` | 100% Offline | Room DB (Favorites) | Full-screen Root Catalog & Context | Grounded | **PRODUCTION** |
| **Yantra** | Full Workflow | YantraRepositoryImpl | 13 Sacred Geometric Diagrams | Devi Bhagavata, Tantric Mathematical Canons | Public Domain / Mathematical Code | Headless SDK | `getYantra` | 100% Offline | Room DB (Favorites) | Full-screen Yantra Catalog & Diagram Canvas | Grounded | **PRODUCTION** |
| **Lal Kitab** | Dedicated Research Workflow | LalKitabResearchRepository | 1939–1952 Traditions & Planetary Rules | Pt. Roop Chand Joshi (1939–1952) | Research Only | Headless SDK | `getLalKitab` (RESEARCH_ONLY) | 100% Offline | In-Memory / Docs | Dedicated Research Screen & Disclaimer | Grounded (Label: RESEARCH_ONLY) | **RESEARCH_ONLY** |

---

### Reconciliation Summary
1. **Garuda Puran Conflict Resolved**:
   - Previous dashboard marked Garuda Puran as "Configuration Required" because the UI route was not connected in `AynvoraAppViewModel` and relied on runtime DB seeding.
   - Code inspection confirmed `GarudaWood1911ContentPackage` is completely written (785 lines, 16 chapters, all 10 topics) with public-domain 1911 Wood & Subrahmanyam translation.
   - Reconciled status: **PRODUCTION**. Wired default offline package fallback so no manual configuration is needed.
2. **Lal Kitab Kept RESEARCH_ONLY**:
   - Not promoted to production calculation or automatic Parashari chart mixing.
   - Promoted from generic foundation bottom sheet to a dedicated, rich Research Screen with explicit disclaimers and methodology breakdown.
3. **Pending Features Activated into Real Production Workflows**:
   - **Daily Guidance**: Panchanga-backed sunrise/sunset, tithi, nakshatra, Rahu Kalam, Choghadiya, morning/evening practices, Gita reflection.
   - **Wallpaper Studio**: Offline procedural templates (Sacred Geometry, Mandala, Constellations), customization, aspect ratio, safe areas, save/export.
   - **Rudraksha**: 14 classical Mukhis catalog, ruling deities, planetary associations, wearing protocol, strict non-medical disclaimer.
   - **Jadi / Sacred Roots**: 9 planetary sacred roots catalog, botanical/regional names, traditional context, strict non-medical disclaimer.
   - **Yantra**: 13 sacred geometric diagrams, mathematical vector geometry, purpose, ritual context, disclaimer.
