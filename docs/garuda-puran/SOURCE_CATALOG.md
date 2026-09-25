# Garuda Puran Source Catalog

This catalog documents the source materials researched, inspected, and verified for **PHASE 8.4A and
PHASE 8.4B** of the AYNVORA project. In accordance with the strict licensing and commercial safety
rules of the project, each source has been investigated for bibliographic provenance, legal status,
cryptographic checksums, and redistribution eligibility.

> [!CRITICAL]
> **COMMERCIAL SAFETY & JURISDICTION-AWARE RIGHTS MANDATE**:
> No copyrighted modern translations or unverified materials may be bundled into the shipped AYNVORA
> SDK.
> Where rights are restricted or uncertain, material is classified as
`REFERENCE_ONLY_NON_DISTRIBUTABLE` or `LICENSE_UNCERTAIN` and kept strictly outside the application
> content packages.
> The phrase "Public Domain Worldwide" is avoided; rights are tracked per target distribution
> jurisdiction.

---

## 1. Summary of Candidate Sources & Verification Status

| # | Source ID                         | Edition Title                       | Language                        | Editor / Translator                       | Publisher / Origin                                       | Year            | SHA-256 Checksum                                                   | Rights Status                          | Distribution Eligibility              | Ingestion Status (Phase 8.4B)                                 |
|---|-----------------------------------|-------------------------------------|---------------------------------|-------------------------------------------|----------------------------------------------------------|-----------------|--------------------------------------------------------------------|----------------------------------------|---------------------------------------|---------------------------------------------------------------|
| 1 | `garuda-puran-s1-summary-pdf`     | गरुड़ पुराण सम्पूर्ण कथा            | Hindi (`hi`)                    | Anonymous / Unknown                       | User-supplied PDF                                        | 2021 (file)     | `2eafe015dd3aa5b6dcbb24578b8cf441e8c04ec4aa31e9c5a1762c93883a8b41` | `LICENSE_UNCERTAIN`                    | Non-distributable                     | REFERENCE_ONLY                                                |
| 2 | `garuda-puran-s2-gitapress-pdf`   | गरुडपुराण-सारोद्धार (Code 1416)     | Hindi (`hi`), Sanskrit (`sa`)   | Pt. Naunidhirama / Gita Press editorial   | Gita Press, Gorakhpur                                    | 2017 CE reprint | `34394f4c8035a77ddb2f0a1420b9e8a7199c0cf3ca50785f269a844979fa4f1a` | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | Non-distributable                     | REFERENCE_ONLY (Cross-reference only)                         |
| 3 | `garuda-puran-gretil-etext`       | Garudapurana (GRETIL E-Text)        | Sanskrit (`sa`)                 | Venkatesvara Steam Press basis / Sansknet | GRETIL (Univ. of Göttingen)                              | 1906 / 2000s    | N/A (Online web)                                                   | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | Non-distributable (CC BY-NC-SA 4.0)   | REFERENCE_ONLY                                                |
| 4 | `garuda-puran-sanskritdocs-etext` | Garudapurana Saroddhara             | Sanskrit (`sa`)                 | SanskritDocuments volunteers              | sanskritdocuments.org                                    | 2005            | N/A (Online web)                                                   | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | Non-distributable (Personal use only) | REFERENCE_ONLY                                                |
| 5 | `garuda-puran-wood-1911-en`       | The Garuda Purana (Saroddhara)      | English (`en`), Sanskrit (`sa`) | Ernest Wood & S.V. Subrahmanyam           | Panini Office, Allahabad                                 | 1911            | `4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5` | `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` | **Eligible** (US, India)              | **APPROVED & INGESTED** (`garuda-content-wood-1911-en-v1`)    |
| 6 | `garuda-puran-dutt-1908-en`       | The Garuda Puranam (3 Vols)         | English (`en`)                  | Manmatha Nath Dutt                        | Society for Resuscitation of Indian Literature, Calcutta | 1908            | `2313f4e0472e3ec9a97b46cb47e6d74a675ba4d7da150214d4759da94960319e` | `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS` | **Eligible** (US, India, UK/EU)       | **APPROVED CROSS-REFERENCE** (Secondary comparative registry) |
| 7 | `garuda-puran-mlbd-1978-en`       | The Garuda-Purana (AITM Vols 12–14) | English (`en`)                  | Prof. J.L. Shastri / Board of Scholars    | Motilal Banarsidass, Delhi                               | 1978            | Commercial print                                                   | `REFERENCE_ONLY_NON_DISTRIBUTABLE`     | Non-distributable (Copyright © MLBD)  | REFERENCE_ONLY (Reference benchmark)                          |

---

## 2. Detailed Ingested & Inspected Source Dossiers

### Primary Ingested Source: Ernest Wood & S.V. Subrahmanyam (1911)

- **Source ID:** `garuda-puran-wood-1911-en`
- **Edition ID:** `paninioffice-garuda-puran-saroddhar-1911`
- **Source Name:** The Garuda Purana (Saroddhara)
- **Edition:** First Edition, Volume IX of "The Sacred Books of the Hindus" series (Edited by Major
  B.D. Basu, I.M.S.)
- **Language:** English (`en`) translation with parallel Sanskrit shlokas (`sa`)
- **Editor / Translator:** Translated into English by Ernest Wood and S.V. Subrahmanyam; Series
  Editor: Major B.D. Basu
- **Publisher:** The Panini Office, Bhuvaneshwari Asrama, Bahadurganj, Allahabad (Printed by Apurva
  Krishna Bose at the Indian Press, Allahabad)
- **Publication Year:** 1911
- **File Name:** `garudapuranasaro00woodrich.pdf`
- **File Size:** 11,842,506 bytes
- **SHA-256 Checksum:** `4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5`
- **Page Count:** 188 pages (169 numbered pages of scripture + front matter)
- **Text Line Count:** ~4,280 lines
- **Source URL / Location:** `https://archive.org/details/garudapuranasaro00woodrich`
- **Legal Basis:**
    - **United States:** Pre-1929 publication entered public domain under 17 U.S.C. § 305.
    - **India:** Author Ernest Wood died December 27, 1965. Indian Copyright Act 1957 Section 22
      grants Life + 60 years, which expired January 1, 2026. S.V. Subrahmanyam died prior to 1940.
    - **EU/UK Note:** Life + 70 jurisdictions remain under review until 2035 unless Rule of the
      Shorter Term applies.
- **Rights Status:** `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS`
- **Verification Status:** `CONTENT_VISUALLY_VERIFIED`
- **Phase 8.4B Ingestion Status:** **APPROVED_FOR_APP**. 23 curated, structurally representative
  passages spanning all 16 chapters ingested into package `garuda-content-wood-1911-en-v1`.

---

### Secondary Cross-Reference Source: Manmatha Nath Dutt (1908)

- **Source ID:** `garuda-puran-dutt-1908-en`
- **Edition ID:** `elysiumpress-garuda-puranam-1908`
- **Source Name:** The Garuda Puranam (3 Volumes)
- **Edition:** First Edition, "The Wealth of India" Series
- **Language:** English (`en`) prose translation
- **Editor / Translator:** Edited and published by Manmatha Nath Dutt, M.A., M.R.A.S.
- **Publisher:** Society for the Resuscitation of Indian Literature, Calcutta (Printed by H.C. Dass
  at the Elysium Press, Calcutta)
- **Publication Year:** 1908
- **File Name:** `garudapuranam00duttgoog.pdf`
- **File Size:** 52,148,912 bytes
- **SHA-256 Checksum:** `2313f4e0472e3ec9a97b46cb47e6d74a675ba4d7da150214d4759da94960319e`
- **Page Count:** 786 pages
- **Text Line Count:** ~28,450 lines
- **Source URL / Location:** `https://archive.org/details/garudapuranam00duttgoog`
- **Legal Basis:** Published 1908 (US Public Domain). M.N. Dutt died in 1912 (>110 years
  post-mortem; expired under Indian §22 and UK/EU Life+70).
- **Rights Status:** `PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS`
- **Verification Status:** `BIBLIOGRAPHIC_VERIFIED`
- **Phase 8.4B Ingestion Status:** Integrated as secondary cross-reference in
  `GarudaSourceComparisonRegistry`. Not merged into Wood 1911 text. Differences tracked as
  `SOURCE_VARIANT` and `EDITION_VARIANT`.

---

### Reference-Only Source: Gita Press Code 1416 Saroddhara Edition

- **Source ID:** `garuda-puran-s2-gitapress-pdf`
- **Edition ID:** `gitapress-garud-puran-code1416`
- **Source Name:** गरुडपुराण-सारोद्धार (सानुवाद)
- **Edition:** Code 1416, 29th reprint
- **Language:** Hindi translation (`hi`) with Sanskrit shlokas (`sa`) in Devanagari
- **Publisher:** Gita Press, Gorakhpur (Govind Bhavan Karyalaya, Kolkata)
- **Publication Year:** सं. २०७४ (2017 CE reprint)
- **File Name:** `GarudPuran.pdf`
- **File Size:** 31,586,838 bytes
- **SHA-256 Checksum:** `34394f4c8035a77ddb2f0a1420b9e8a7199c0cf3ca50785f269a844979fa4f1a`
- **Page Count:** 275 pages
- **Rights Status:** `REFERENCE_ONLY_NON_DISTRIBUTABLE`
- **Verification Status:** `REFERENCE_VERIFIED`
- **Phase 8.4B Status:** Strictly non-distributable. Retained purely for cross-edition structural
  mapping and variant analysis. Zero text bundled into shipped app package.
