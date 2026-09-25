# Garuda Puran License & Rights Matrix (Jurisdiction-Aware)

This document defines the engineering rights governance and licensing compliance matrix for all
candidate Garuda Purana materials considered for the AYNVORA Kotlin Multiplatform SDK.

> [!CRITICAL]
> **ENGINEERING RIGHTS MANDATE & NOT LEGAL ADVICE**:
> This document records engineering rights metadata and verification tracking, not a formal legal
> opinion.
> AYNVORA is engineered as a commercial-grade, multi-platform SDK. Under no circumstances may
> copyrighted text, modern translations, or restrictive copyleft materials be redistributed or bundled
> into client application packages.
>
> - **Publicly accessible ≠ Licensed for redistribution**: A PDF or website being downloadable
    online does not confer distribution rights.
> - **Ancient scripture ≠ Public domain modern edition**: While the ancient Sanskrit text of the
    Purana is millennia old, any translation, selection, preface, or modern typography receives
    distinct, independent copyright protection.
> - **Jurisdiction-Aware Rights**: "Public Domain Worldwide" is avoided. Rights eligibility is
    evaluated per target distribution jurisdiction.

---

## 1. Applicable Legal Frameworks by Jurisdiction

1. **United States (Title 17, U.S.C.)**:
    - Works published **before January 1, 1929** are in the public domain in the United States,
      regardless of author death date (17 U.S.C. § 305).
    - Both Dutt (1908) and Wood (1911) were published before 1929 and are in the US public domain.

2. **India (Indian Copyright Act, 1957 as amended)**:
    - **Section 22 (Term of copyright in published literary works)**: Copyright subsists until **60
      years from the beginning of the calendar year next following the year in which the author dies
      **.
    - **Ernest Wood** died December 27, 1965. The 60-year term began January 1, 1966 and concluded
      on **December 31, 2025**. As of **January 1, 2026**, the Wood 1911 translation entered the
      public domain in India.
    - **S.V. Subrahmanyam** died prior to 1940 (>85 years post-mortem; term expired).
    - **Manmatha Nath Dutt** died in 1912 (>110 years post-mortem; term expired).
    - **Gita Press (Code 1416 / Naunidhirama Hindi translation)**: Modern typography and Hindi
      translation copyrighted by Gobind Bhavan Karyalaya / Gita Press. All rights reserved. Strictly
      non-distributable.

3. **European Union & United Kingdom (Life + 70 Years)**:
    - For jurisdictions observing Life + 70 years:
        - **Manmatha Nath Dutt** (d. 1912): Life + 70 expired December 31, 1982. In public domain.
        - **Ernest Wood** (d. 1965): Life + 70 extends through **December 31, 2035**. In Life+70
          jurisdictions without application of the Rule of the Shorter Term (Berne Convention Art.
          7(8)), Wood's text remains under rights review.
        - **Classification**: `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` with documented geographic
          scope (US, India, and jurisdictions applying the Rule of the Shorter Term to country of
          origin).

4. **Copyleft & Non-Commercial Creative Commons**:
    - Creative Commons **CC BY-NC** or **CC BY-NC-SA** licenses expressly forbid commercial
      exploitation. Bundling CC-NC text in commercial apps or commercial SDKs constitutes copyright
      infringement. CC-SA licenses also impose viral copyleft.

---

## 2. Comprehensive Rights Matrix for Inspected Sources

| #     | Source ID                         | Publication Year & Origin                               | Author / Translator Lifespan                                                           | Jurisdiction & Legal Basis                                                                                         | Verification Date | Rights Status Enum                     | Commercial Bundling Permitted?                            | Remaining Uncertainty                                                    |
|-------|-----------------------------------|---------------------------------------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|-------------------|----------------------------------------|-----------------------------------------------------------|--------------------------------------------------------------------------|
| **1** | `garuda-puran-s1-summary-pdf`     | Unspecified (PDF created 2021, India)                   | Anonymous / `ADMIN`                                                                    | Author unknown; publication date unverified; no license statement                                                  | 2026-09-25        | `LICENSE_UNCERTAIN`                    | **NO**                                                    | Origin, author, and copyright status completely unknown                  |
| **2** | `garuda-puran-s2-gitapress-pdf`   | सं. २०७४ (2017 CE reprint, India)                       | Sanskrit: Pt. Naunidhirama (~16th c.); Hindi: Gita Press Editorial Board               | India Copyright Act 1957; all rights reserved by Gobind Bhavan Karyalaya                                           | 2026-09-25        | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | **NO**                                                    | None; modern Hindi text is strictly copyrighted                          |
| **3** | `garuda-puran-gretil-etext`       | 1906 (base text) / 2000s (digital compilation, Germany) | Base: Public domain; Digital corpus: Sansknet / GRETIL                                 | CC BY-NC-SA 4.0; German/EU database terms                                                                          | 2026-09-25        | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | **NO** (NC prohibits commercial use; SA creates copyleft) | None; NC license precludes commercial app embedding                      |
| **4** | `garuda-puran-sanskritdocs-etext` | 2005 (digital compilation, India/USA)                   | Volunteer transcription of classical Sanskrit                                          | Volunteer digital compilation terms strictly limit use to personal non-commercial study                            | 2026-09-25        | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | **NO**                                                    | Personal use terms prohibit redistribution                               |
| **5** | `garuda-puran-wood-1911-en`       | 1911 (Allahabad, British India)                         | Ernest Wood (d. Dec 27, 1965); S.V. Subrahmanyam (d. <1940); Major B.D. Basu (d. 1930) | **US**: 17 U.S.C. § 305 (pre-1929 publication). **India**: Indian Copyright Act §22 (Life+60 expired Jan 1, 2026). | 2026-09-25        | `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` | **YES** (In verified jurisdictions: US, India)            | Life+70 jurisdictions (EU/UK) pending review of Rule of the Shorter Term |
| **6** | `garuda-puran-dutt-1908-en`       | 1908 (Calcutta, British India)                          | Manmatha Nath Dutt (d. 1912)                                                           | **US**: Pre-1929 publication. **India**: Life+60 expired 1973. **EU/UK**: Life+70 expired 1983.                    | 2026-09-25        | `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` | **YES** (Secondary cross-reference only)                  | None identified                                                          |
| **7** | `garuda-puran-mlbd-1978-en`       | 1978 (Delhi, India)                                     | Prof. J.L. Shastri (d. 1980s); Board of Scholars                                       | Indian §22 (author life+60 active); US Title 17 (post-1978 copyright active)                                       | 2026-09-25        | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | **NO**                                                    | None; proprietary active commercial copyright                            |

---

## 3. Strict Operational Guidelines for Ingestion

1. **RULE OF COMPLETE EXCLUSION**:
   Any source marked `REFERENCE_ONLY_NON_DISTRIBUTABLE` or `LICENSE_UNCERTAIN` must NEVER have its
   text bundled into any shipped asset, resource file, SQLite database, or client binary.
2. **METADATA-ONLY USAGE FOR REFERENCE SOURCES**:
   Non-distributable sources (including Gita Press Code 1416) are used exclusively for:
    - Bibliographic metadata and file integrity tracking (SHA-256).
    - Cross-edition chapter and section mapping.
    - Textual comparison and variant classification (`SOURCE_VARIANT`, `EDITION_VARIANT`,
      `TEXT_UNCERTAIN`).
3. **APPROVED PACKAGE INGESTION**:
   Only `garuda-puran-wood-1911-en` (`garuda-content-wood-1911-en-v1`) is ingested as approved
   scripture text for English (`en`).
4. **HINDI SCRIPTURE CONTENT POLICY (OPTION B)**:
   Because no legally verified, approved public-domain Hindi translation of the Garuda Purana has
   been ingested in this phase, Hindi scripture content remains `CONTENT_UNAVAILABLE` with status
   `NO_APPROVED_CONTENT_FOR_LANGUAGE`.
    - **Zero silent fallback** to English or to copyrighted Gita Press text.
    - The UI and Report Viewer explicitly display the absence of approved Hindi source content.
5. **CHECKSUM INTEGRITY ≠ RIGHTS CLEARANCE**:
   Generating a SHA-256 checksum proves file authenticity against corruption. Rights clearance is
   governed by the jurisdiction matrix above.
