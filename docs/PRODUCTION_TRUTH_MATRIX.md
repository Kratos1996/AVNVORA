# AYNVORA Production Truth Matrix (Phase 10.28)

**Audit Date:** October 2, 2026  
**Auditing Standard:** Strict Zero-Fabrication, Source-First Provenance, Deterministic Verification

---

## Production Status Key
- **VERIFIED:** Implemented, covered by deterministic golden tests, mathematically proven, legally clear rights, tool registered, AI grounded, device verified offline.
- **PARTIAL:** Core calculation implemented and verified, but full sub-features or secondary dynamic charts remain unverified or awaiting full specification.
- **RESEARCH_ONLY:** Textual or algorithmic research complete; legally or empirically restricted from production runtime invocation without explicit user study consent.
- **UNVERIFIED:** Spec exists without deterministic mathematical engine or golden reference tests.
- **UNSUPPORTED:** Explicitly outside current SDK operational capabilities; tools reject and guardrails refuse.

---

| Feature | Status | Implementation File | Production Source | Source Rights | Algorithm Verified | Golden Tests | Differential Tests | Tool Registered | AI Grounded | Device Verified | Offline Verified | Known Ambiguities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Solar Return / Varshaphal** | **VERIFIED** | `astro-engine/.../SolarReturnEngine.kt` | *Tajika Neelakanthi* (1907 Scan) | Public Domain | YES | YES | YES | YES (`getVarshaphal`) | YES | YES (S23 Ultra) | YES | None; exact mean tropical solar return crossing sidereal base. |
| **Muntha Calculation** | **VERIFIED** | `astro-engine/.../MunthaEngine.kt` | *Tajika Neelakanthi* v. 1 | Public Domain | YES | YES | YES | YES (`getMuntha`) | YES | YES (S23 Ultra) | YES | Annual sign advancement 30° per year mod 12. |
| **Varsheshwara (Year Lord)** | **VERIFIED** | `astro-engine/.../VarsheshwaraEngine.kt` | *Tajika Neelakanthi* v. 3 | Public Domain | YES | YES | YES | YES (`getVarsheshwara`) | YES | YES (S23 Ultra) | YES | Five office bearers, Lagna aspect gating, Panchavargiya bala ranking. |
| **Classical Sahams** | **VERIFIED** | `astro-engine/.../SahamsEngine.kt` | *Tajika Neelakanthi* v. 5 | Public Domain | YES | YES | YES | YES (`getSahams`) | YES | YES (S23 Ultra) | YES | Day/Night inversion; Shodhya-Shuddhyashraya 30° correction when crossing Lagna. |
| **Tajika Aspects** | **VERIFIED** | `astro-engine/.../TajikaAspectsEngine.kt` | *Tajika Neelakanthi* v. 7 | Public Domain | YES | YES | YES | YES (`getTajikaAspects`) | YES | YES (S23 Ultra) | YES | Deeptamsha planetary orbs; Itthashala, Ishrafa, Nakta, Yamaya. |
| **Mudda Dasha** | **VERIFIED** | `astro-engine/.../MuddaDashaEngine.kt` | *Tajika Neelakanthi* v. 10 | Public Domain | YES | YES | YES | YES (`getMuddaDasha`) | YES | YES (S23 Ultra) | YES | Vimshottari 120-year proportions scaled to annual solar return interval (365.2422 days). |
| **KP 249 Table** | **VERIFIED** | `astro-engine/.../KP249Engine.kt` | Independent Mathematical Derivation | Algorithmic Public Domain | YES | YES | YES | YES (`getKP`) | YES | YES (S23 Ultra) | YES | Exactly 6 subs split at 6 sign boundaries (30°, 90°, 150°, 210°, 270°, 330°). 60°, 180°, 300° align with sub-boundaries. |
| **KP Cusps & Lords** | **VERIFIED** | `astro-engine/.../KPEngine.kt` | Krishnamurti Ayanamsha (291 AD) & Placidus | Algorithmic Public Domain | YES | YES | YES | YES (`getKP`) | YES | YES (S23 Ultra) | YES | Algorithm only; protected literary expression unbundled. |
| **KP 4-Fold Significators** | **VERIFIED** | `astro-engine/.../KPEngine.kt` | KP 4-fold Levels A, B, C, D | Algorithmic Public Domain | YES | YES | YES | YES (`getKP`) | YES | YES (S23 Ultra) | YES | Strict hierarchy Level A > B > C > D. |
| **Jaimini Chara Karakas** | **VERIFIED** | `astro-engine/.../JaiminiEngine.kt` | *Jaimini Upadesha Sutras* 1.1 | Public Domain | YES | YES | YES | YES (`getJaimini`) | YES | YES (S23 Ultra) | YES | 7 Chara Karakas (AK to DK) strictly by descending sign longitude. |
| **Jaimini Arudhas (AL, UL)** | **VERIFIED** | `astro-engine/.../JaiminiEngine.kt` | *Jaimini Upadesha Sutras* 1.1.30-31 | Public Domain | YES | YES | YES | YES (`getJaimini`) | YES | YES (S23 Ultra) | YES | Distance projection with 10-house jump rule on 1st/7th landfall. |
| **Jaimini Rashi Drishti** | **VERIFIED** | `astro-engine/.../JaiminiEngine.kt` | *Jaimini Upadesha Sutras* 1.1.3-5 | Public Domain | YES | YES | YES | YES (`getJaimini`) | YES | YES (S23 Ultra) | YES | Movable aspects Fixed (except adjacent); Fixed aspects Movable (except adjacent); Dual aspect mutually. |
| **Prashna Core** | **VERIFIED** | `astro-engine/.../PrashnaEngine.kt` | *Prasna Marga* & KP Horary 1-249 | Public Domain | YES | YES | YES | YES (`getPrashna`) | YES | YES (S23 Ultra) | YES | Intent classification, house significations, KP 1-249 seed resolution. |
| **Prashna Full** | **PARTIAL** | `astro-engine/.../PrashnaEngine.kt` | *Prasna Marga* Dynamic Chart Casting | Research Only | PARTIAL | NO | NO | NO | PARTIAL | NO | NO | Full dynamic chart casting at horary moment pending integration with live ephemeris pipeline. |
| **Chaldean Horas** | **VERIFIED** | `astro-engine/.../MuhurtaEngine.kt` | *Muhurta Chintamani* | Public Domain | YES | YES | YES | YES (`getMuhurta`) | YES | YES (S23 Ultra) | YES | 24 Horas in descending orbital speed from local sunrise. |
| **Choghadiyas** | **VERIFIED** | `astro-engine/.../MuhurtaEngine.kt` | *Muhurta Chintamani* | Public Domain | YES | YES | YES | YES (`getMuhurta`) | YES | YES (S23 Ultra) | YES | 7 Day & 7 Night Choghadiyas proportional to actual diurnal span. |
| **Rahu Kalam / Yamaganda** | **VERIFIED** | `astro-engine/.../MuhurtaEngine.kt` | *Muhurta Chintamani* | Public Domain | YES | YES | YES | YES (`getMuhurta`) | YES | YES (S23 Ultra) | YES | Exact 1/8th daytime fraction; zero 6 AM/6 PM approximation. |
| **Ashtakoota (36 Gunas)** | **VERIFIED** | `astro-engine/.../CompatibilityEngine.kt` | *Brihat Parashara Hora Shastra* Ch. 22 | Public Domain | YES | YES | YES | YES (`getCompatibility`) | YES | YES (S23 Ultra) | YES | Varna, Vashya, Tara, Yoni, Graha Maitri, Gana, Bhakoot, Nadi. |
| **South Indian 10-Poruthams**| **VERIFIED** | `astro-engine/.../CompatibilityEngine.kt` | *Kalaprakasika* / *Jataka Chandrika* | Public Domain | YES | YES | YES | YES (`getCompatibility`) | YES | YES (S23 Ultra) | YES | Dina, Gana, Mahendra, Stree Deergha, Yoni, Rasi, Rasiyathipathi, Vasya, Rajju, Vedha. |
| **Planetary Yogas** | **VERIFIED** | `astro-engine/.../YogaDoshaEngine.kt` | *BPHS* & *Saravali* | Public Domain | YES | YES | YES | YES (`getYoga`) | YES | YES (S23 Ultra) | YES | Gajakesari, Budhaditya, Raj Yoga, Pancha Mahapurusha. |
| **Planetary Doshas** | **VERIFIED** | `astro-engine/.../YogaDoshaEngine.kt` | *BPHS*, *Brihat Samhita*, Shastras | Public Domain | YES | YES | YES | YES (`getDosha`) | YES | YES (S23 Ultra) | YES | Kemadruma, Manglik, Kala Sarpa, Pitru; with classical cancellation checks (Bhanga). |
| **Upagrahas (Aprakash)** | **VERIFIED** | `astro-engine/.../UpagrahaEngine.kt` | *BPHS* Ch. 3 & *Jyotish Tattva* | Public Domain | YES | YES | YES | YES (`getUpagraha`) | YES | YES (S23 Ultra) | YES | Dhuma, Vyatipata, Parivesha, Indrachapa, Upaketu (Identity verified: Upaketu + 30° == Sun). |
| **Lal Kitab** | **RESEARCH_ONLY** | `aynvora-core/.../AstroGroundingTools.kt` | Pandit Roop Chand Joshi (1939-1952) | Research Only | PARTIAL | NO | NO | YES (`getLalKitab` status RESEARCH_ONLY) | REFUSED | NO | YES | Fixed-house remedial principles; strictly isolated from Parashari horoscopes. |
| **Vastu Shastra** | **RESEARCH_ONLY** | `aynvora-core/.../AstroGroundingTools.kt` | *Brihat Samhita* Vastu chapters | Research Only | PARTIAL | NO | NO | YES (`getVastu` status RESEARCH_ONLY) | REFUSED | NO | YES | Directional geometry verified; commercial claims refused. |
| **Numerology (Chaldean/Pyth/LoShu)** | **VERIFIED** | `aynvora-core/.../numerology/...` | Cheiro & Lo Shu classics | Public Domain | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | Radical, Destiny, Name, Lo Shu grid 3x3 balance. |
| **Palmistry** | **VERIFIED** | `aynvora-core/.../palmistry/...` | Classical Cheiromancy Canon | Public Domain | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | Hand shape, mounts, major lines, curvature. |
| **Tarot** | **VERIFIED** | `aynvora-core/.../tarot/...` | Classical Rider-Waite 78 cards | Public Domain (1909) | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | Major/Minor arcana, Celtic cross, upright/reversed. |
| **Bhagavad Gita Wisdom** | **VERIFIED** | `aynvora-data/.../gita/...` | Complete 701 Sanskrit Verses | Public Domain | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | 701 verses seeded into local SQLite Room database. |
| **Garuda Purana** | **VERIFIED** | `aynvora-core/.../garudapuran/...` | Garuda Purana Saroddhara | Public Domain | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | 16 chapters, karma-samskara, Shraddha timings. |
| **Gemstones / Ratna** | **VERIFIED** | `aynvora-core/.../gemstones/...` | Navaratna classical texts | Public Domain | YES | YES | YES | YES | YES | YES (S23 Ultra) | YES | Anukul/Pratikul gemstone recommendations based on Lagna/Kendra lords. |
