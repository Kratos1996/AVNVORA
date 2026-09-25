# Garuda Puran Source Selection & Comparative Evaluation

This document outlines the source evaluation methodology, selection rationale, and comparative
analysis across the 7 candidate Garuda Purana editions investigated for AYNVORA.

---

## 1. Selection Decision Summary

1. **NO CONTENT INGESTED IN THIS PHASE**:
   In strict accordance with the Phase 8.4A specification, **no scripture text or interpretive
   content is bundled or made available in the application**. Feature content status remains
   `CONTENT_UNAVAILABLE` (Status: `ComingSoon`).

2. **PRIMARY SELECTED CANDIDATE FOR FUTURE INGESTION**:
    - **`garuda-puran-wood-1911-en` (Ernest Wood & S.V. Subrahmanyam, 1911, Panini Office,
      Allahabad)**:
        - **Tradition:** Garuda Purana Saroddhara (Pretakalpa) in 16 chapters.
        - **Legal Status:** `APPROVED_FOR_DISTRIBUTION` (Public Domain worldwide).
        - **Strengths:** Matches the exact devotional and ritual recension known across North
          India (Saroddhara compiled by Pt. Naunidhirama). Contains rigorous Sanskrit text with
          accurate verse-by-verse English translations and scholarly notes.
        - **Ingestion Status:** `CONTENT_NOT_INGESTED`. Clean digital scans from the Library of
          Congress and Internet Archive are identified.

3. **SECONDARY SELECTED CANDIDATE FOR FUTURE INGESTION**:
    - **`garuda-puran-dutt-1908-en` (Manmatha Nath Dutt, 1908, Calcutta)**:
        - **Tradition:** Complete Garuda Mahapurana (Purvakhanda & Pretakhanda).
        - **Legal Status:** `APPROVED_FOR_DISTRIBUTION` (Public Domain worldwide).
        - **Strengths:** Covers the broader philosophical, cosmological, and astrological chapters (
          Agada, Nitisara, Dharma).
        - **Ingestion Status:** `CONTENT_NOT_INGESTED`.

4. **REJECTED FROM APP REDISTRIBUTION (REFERENCE ONLY)**:
    - **`garuda-puran-s2-gitapress-pdf` (Gita Press Code 1416)**: Proprietary modern copyright ©
      Gita Press. Relegated strictly to `REFERENCE_ONLY_NON_DISTRIBUTABLE`.
    - **`garuda-puran-mlbd-1978-en` (Motilal Banarsidass 1978)**: Proprietary modern copyright ©
      MLBD. Relegated strictly to `REFERENCE_ONLY_NON_DISTRIBUTABLE`.
    - **`garuda-puran-gretil-etext`**: Restrictive CC BY-NC-SA 4.0 license forbids commercial
      software distribution.
    - **`garuda-puran-sanskritdocs-etext`**: Terms of use restrict text to personal study only.
    - **`garuda-puran-s1-summary-pdf`**: Anonymous, unverified provenance, garbled font encoding,
      incomplete text.

---

## 2. Multi-Criteria Source Evaluation

| Candidate Source                   | Text Completeness                             | Text Extraction Quality                           | Translation Quality                   | Legal Safety for Bundling              | Selection Recommendation                     |
|------------------------------------|-----------------------------------------------|---------------------------------------------------|---------------------------------------|----------------------------------------|----------------------------------------------|
| **Source 1: Summary PDF**          | Incomplete (10 pages only; cuts off in Ch. 2) | Garbled (legacy KrutiDev mapping)                 | Poor / Modern prose retelling         | **Zero (Uncertain)**                   | **REJECTED**                                 |
| **Source 2: Gita Press Code 1416** | Comprehensive for Saroddhara (275 pages)      | Visual OCR clear; programmatic text layer garbled | High (Fluent Hindi + Sanskrit)        | **Zero (Proprietary Copyright)**       | **REFERENCE ONLY** (Index benchmark only)    |
| **Source 3: GRETIL E-Text**        | Comprehensive Purvakhanda (229 chapters)      | Clean Unicode text layer                          | Sanskrit only (no translation)        | **Zero (CC BY-NC-SA copyleft)**        | **REFERENCE ONLY** (Sanskrit collation only) |
| **Source 4: SanskritDocs**         | Complete Saroddhara (16 chapters)             | Clean Unicode text layer                          | Sanskrit only                         | **Zero (Personal study restriction)**  | **REFERENCE ONLY**                           |
| **Source 5: Wood 1911**            | Complete Saroddhara (16 chapters, 169 pages)  | Clean text layer from scans                       | High (Scholarly English + Sanskrit)   | **100% (Public Domain worldwide)**     | **APPROVED FOR FUTURE INGESTION**            |
| **Source 6: Dutt 1908**            | Complete Mahapurana (784 pages)               | Clean text layer from scans                       | High (Comprehensive English prose)    | **100% (Public Domain worldwide)**     | **APPROVED FOR FUTURE INGESTION**            |
| **Source 7: MLBD 1978**            | Complete Critical Edition (1,184 pages)       | Clean print                                       | Excellent (Modern critical scholarly) | **Zero (Strict commercial copyright)** | **REFERENCE ONLY** (Scholarly validation)    |

---

## 3. Edition Discrepancy & Recension Analysis

### A. The "Two Garuda Puranas": Mahapurana vs. Saroddhara

A critical finding from cross-source research is that "Garuda Purana" in Indian religious practice
refers to two distinct but related textual traditions:

1. **The Classical Garuda Mahapurana**:
    - Contains two massive Khandas: **Purvakhanda** (approx. 229 chapters covering cosmology,
      astrology, gems, medicine, Vishnu worship, and dharma) and **Uttarakhanda / Pretakhanda** (
      approx. 34–49 chapters covering death, rites, and the afterlife).
    - Represented by: Source 3 (GRETIL, 229 chapters), Source 6 (Dutt 1908), and Source 7 (MLBD
      1978, 3 volumes).
2. **The Garuda Purana Saroddhara (प्रेतकल्प सारोद्धार)**:
    - An anthology of the Pretakalpa compiled around the 16th century by **Pandit Naunidhirama** (
      son of Sri Sayana Bhardwaja) in Rajasthan.
    - Naunidhirama synthesized the core teachings on death, the 16 cities of Yamamarga, Vaitarani
      river, funeral rites (Antyeshti), pinda offerings, and Moksha from the Garuda Purana,
      Bhagavata, and related texts into **16 focused chapters**.
    - Represented by: Source 2 (Gita Press Code 1416), Source 4 (SanskritDocuments), and Source 5 (
      Ernest Wood 1911).

### B. Gita Press 16 vs. 17 Chapter Inconsistency

- In Gita Press Code 1416, the editorial introduction by Radheshyam Khemka (page 6) states: *"
  प्रस्तुत ग्रन्थ गरुडपुराण-सारोद्धार (प्रेतकल्प)-में उपलब्ध है। यह सोलह अध्यायों में सुगुम्फित
  है।"* (This text is woven into 16 chapters).
- However, the Contents page (pages 8–9) and body explicitly number 17 sections, treating the
  concluding **श्रवण-फल** (Phala-shruti) as Chapter 17 (pages 269–272).
- By contrast, Ernest Wood (1911) maintains the standard 16 chapters, including the Phala-shruti at
  the end of Chapter 16 (verses 115–120).
- **AYNVORA Resolution:** The system must record edition-specific numbering without attempting to
  force-merge differing chapter boundaries.

---

## 4. Next Phase Roadmap Recommendation

For Phase 8.4B or future ingestion phases:

1. Ingest digital text exclusively from **Source 5 (Wood 1911)** for the 16 chapters of the
   Saroddhara.
2. Ingest complementary Sanskrit shlokas and English translations from **Source 6 (Dutt 1908)**.
3. Keep Gita Press (Source 2) and MLBD (Source 7) strictly as non-bundled reference benchmarks.
4. Maintain `CONTENT_UNAVAILABLE` until an end-to-end editorial verification and evidence graph
   linking is signed off.
