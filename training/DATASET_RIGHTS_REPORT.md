# AYNVORA Dataset Rights & Provenance Report (Phase 10.28)

**Audit Date:** October 2, 2026  
**Auditor:** Antigravity Autonomous Systems Engineering & Compliance  
**Scope:** `training/dataset-v1`, `training/dataset-v2`, `training/dataset-v3`, `training/dataset-v4`

---

## 1. Executive Summary & Policy Compliance

AYNVORA enforces a zero-trust, source-first data provenance policy across all machine learning artifacts:
- **No Private User Data:** 100% of astrological positions, coordinates, names, and query cases are synthetically generated test fixtures. Zero telemetry, user profiles, conversation histories, or cloud query logs are ingested.
- **No Unlicensed Literary Excerpts:** Literary prose, modern commentaries, and translations under copyright are strictly excluded from dataset text.
- **No Scraped Raw Web Text:** No web scrapers or uncurated text dumps are utilized.
- **No PII:** Datasets contain zero telephone numbers, email addresses, geographical home addresses, or device IDs.
- **Separation of Expression vs. Mathematical Algorithm:** Astrological facts, mathematical formulas (e.g. Placidus trigonometric relations, Vimshottari fractional sub-divisions, Tajika planetary orbs), and public-domain classical Sanskrit definitions (*BPHS*, *Jaimini Upadesha Sutras*, *Muhurta Chintamani*, *Tajika Neelakanthi*) are cleanly separated from protected literary expression.

---

## 2. Granular Source Provenance Audit

| Source Identifier | Classical / Primary Source | Rights & Legal Status | Content Transformed / Utilized |
| :--- | :--- | :--- | :--- |
| `tajika-neelakanthi-1907-scan` | *Tajika Neelakanthi* (1907 Benares Scan) | Public Domain (Author 16th c., Edition 1907) | Classical Saham formulas, Muntha rule, Mudda Dasha ratio |
| `tajika-neelakanthi-wikisource-pages-112-122-124` | *Tajika Neelakanthi* (Mahidhara Sanskrit recension) | Public Domain (Wikisource public domain canon) | Varsheshwara criteria, Panchavargiya bala principles |
| `jaimini-sutras-maharishi-jaimini-public-domain` | *Jaimini Upadesha Sutras* | Public Domain (Ancient classical Sanskrit) | 7 Chara Karakas, Arudhas, Karakamsha, Rashi Drishti |
| `brihat-parashara-hora-shastra-public-domain` | *Brihat Parashara Hora Shastra* | Public Domain (Ancient classical Sanskrit) | Ashtakoota 36 Gunas, Pancha Mahapurusha, Yogini/Ashtottari |
| `muhurta-chintamani-public-domain` | *Muhurta Chintamani* by Rama Daivajna | Public Domain (Classical 16th c. Sanskrit) | Chaldean Horas, Choghadiyas, Rahu Kalam, Yamaganda |
| `kalaprakasika-classical-muhurta-public-domain` | *Kalaprakasika* | Public Domain (Classical recension) | South Indian 10-Poruthams, Tara Bala, Abhijit exceptions |
| `jyotish-tattva-classical-upagrahas-public-domain` | *Jyotish Tattva* & *BPHS* Ch. 3 | Public Domain (Classical Sanskrit) | Aprakash Grahas (Dhuma, Vyatipata, Parivesha, Upaketu) |
| `prasna-marga-public-domain` | *Prasna Marga* | Public Domain (Classical 17th c. Kerala text) | Horary query house significations and query moment Lagna |
| `kp-algorithm-independent-reconstruction-public-domain` | Krishnamurti Paddhati Mathematical Principles | Unprotected Mathematical/Astronomical Algorithms | 249 sub-division arithmetic, Placidus cusps, 291 AD Ayanamsha |
| `lal-kitab-1939-1952-research-public-domain` | Classical *Lal Kitab* (1939–1952 Urdu/Hindi) | Research Only / Classical Editions | Research boundaries only; models instructed to refuse mixing |
| `brihat-samhita-vastu-research-public-domain` | *Brihat Samhita* by Varahamihira | Public Domain (Classical 6th c. Sanskrit) | Research boundaries only; commercial claims refused |

---

## 3. KP Copyright Compliance Determination

- **Literary Works:** K.S. Krishnamurti (1908–1972) published his Readers between 1966 and 1971. Under Section 22 of the Indian Copyright Act, 1957, literary copyright persists for 60 years from the end of the author's death year (1972 + 60 = end of 2032). Therefore, verbatim textual excerpts from the Readers are legally protected.
- **Mathematical / Algorithmic Derivation:** Copyright law worldwide (17 U.S.C. § 102(b), Indian Copyright Act Sections 13–14) specifically excludes mathematical formulas, ideas, concepts, and astronomical calculations.
- **AYNVORA Implementation:** 
  - The KP 249 table in AYNVORA is generated dynamically via independent arithmetic: `(800' * VimshottariYears) / 120`. No table was copied from any book or repository.
  - Zero sentences of K.S. Krishnamurti's proprietary prose are stored in production knowledge packs or training datasets.
  - In AYNVORA, KP status is certified as `KP_PRODUCTION_STATUS = ALGORITHM_ONLY`.

---

## 4. Verification Checksums

- Dataset v3 Checksum (`verified_sft.jsonl`): `4d436a1622cb378e9f2a74c676ebfefae5d9859f518e906b4dcce4b9eeefb5ba`
- Approved Sources Checksum (`verified_sources.json`): `43105ff761895a94ee8992e2e604f8b9ecf02f9c8ef12822a101f37e4085b3be`

All examples are confirmed 100% compliant with copyright law and user privacy standards.
