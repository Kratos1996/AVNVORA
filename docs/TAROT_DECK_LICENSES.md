# Tarot Deck Licensing & Rights Verification Matrix

**Phase**: 8.5 — Tarot Complete Production Experience  
**Date of Verification**: 2026-09-25  
**Auditor**: AYNVORA Core Intellectual Property & Legal Verification  
**Primary Source Repository**: `https://github.com/mixvlad/TarotCards`  
**Inspected Commit**: `840b84d012c6b74dc1634fa2d00d1b21f5b28093` (Branch: `main`)  
**Repository License**: MIT License (Mike Koz, 2025)

---

## 1. Executive Summary & Policy

AYNVORA operates under a strict **fail-closed** copyright policy. A public repository or open-source
license on repository code does **not** grant automatic commercial or redistribution rights to the
underlying visual assets contained within it. Each tarot deck in `mixvlad/TarotCards` originates
from a distinct historical or contemporary source with differing legal rights.

- **Primary Bundled Deck**: **Rider-Waite-Smith (1909)** — Validated as **PUBLIC DOMAIN** worldwide.
  Exactly 78 cards + card back are packaged and bundled into the offline application.
- **Optional Downloadable Deck**: **Soimoi Tarot** — Licensed under **Creative Commons Attribution
  4.0 International (CC BY 4.0)**. Eligible for optional on-demand download with explicit
  attribution.
- **Other Decks**: Documented as **REFERENCE_ONLY** or **REFERENCE_ONLY_INCOMPLETE**. Not bundled in
  Phase 8.5.

---

## 2. Comprehensive Deck Rights Matrix

| Deck Identifier                              | Deck Name               | Origin Year | Original Artist / Creator                                         | Underlying Source                                                          | Rights Status          | Commercial Redistribution        | Phase 8.5 Treatment                   |
|----------------------------------------------|-------------------------|-------------|-------------------------------------------------------------------|----------------------------------------------------------------------------|------------------------|----------------------------------|---------------------------------------|
| `rider-waite` (`rider_waite_smith_standard`) | Rider-Waite-Smith Tarot | 1909        | Pamela Colman Smith (1878–1951) & Arthur Edward Waite (1857–1942) | Steve-P.org (Scans of original 1909 "Pam-A" printing restored by Steve P.) | **PUBLIC DOMAIN**      | **PERMITTED**                    | **PRIMARY_BUNDLED (78 Cards + Back)** |
| `soimoi`                                     | Soimoi Tarot Deck       | 2025        | Mike Koz (koz.tv)                                                 | Generative image model deck by repository author                           | **CC BY 4.0**          | **PERMITTED** (with attribution) | **OPTIONAL_DOWNLOADABLE**             |
| `sola-busca`                                 | Sola Busca Tarot        | c. 1491     | Workshop of Nicola di maestro Antonio (Italy)                     | Wikimedia Commons                                                          | **PUBLIC DOMAIN**      | **PERMITTED**                    | **REFERENCE_ONLY**                    |
| `marseille`                                  | Tarot de Marseille      | c. 1700s    | Traditional French pattern                                        | Wikimedia Commons                                                          | **PUBLIC DOMAIN**      | **PERMITTED**                    | **REFERENCE_ONLY**                    |
| `etteilla`                                   | Grand Etteilla          | c. 1880     | Jean-Baptiste Alliette (Etteilla) / H. Pussey                     | BnF Gallica via Wikimedia Commons                                          | **INCOMPLETE (75/78)** | **NON_REDISTRIBUTABLE**          | **REFERENCE_ONLY_INCOMPLETE**         |
| `vieville`                                   | Tarot de Viéville       | c. 1650     | Jacques Viéville (Paris)                                          | BnF Gallica via Wikimedia Commons                                          | **PUBLIC DOMAIN**      | **PERMITTED**                    | **REFERENCE_ONLY**                    |
| `visconti-sforza`                            | Visconti-Sforza         | c. 1450     | Bonifacio Bembo (Milan)                                           | Wikimedia Commons                                                          | **INCOMPLETE (73/78)** | **NON_REDISTRIBUTABLE**          | **REFERENCE_ONLY_INCOMPLETE**         |
| `oswald-wirth`                               | Oswald Wirth Tarot      | 1889        | Oswald Wirth (Paris)                                              | BnF Gallica via Wikimedia Commons                                          | **MAJOR_ONLY (22/78)** | **NON_REDISTRIBUTABLE**          | **REFERENCE_ONLY_INCOMPLETE**         |
| `tarot-nouveau`                              | Tarot Nouveau           | 1898        | B.P. Grimaud (Paris)                                              | BnF Gallica via Wikimedia Commons                                          | **PUBLIC DOMAIN**      | **PERMITTED**                    | **REFERENCE_ONLY_NON_OCCULT**         |

---

## 3. In-Depth Legal Analysis: Primary Bundled Deck

### Deck: Rider-Waite-Smith Standard (`rider_waite_smith_standard`)

#### A. Historical Publication & Authorship

- **Publication**: Published in London in **December 1909** by **William Rider & Son, Ltd.**
- **Illustrator**: **Pamela Colman Smith** (born February 16, 1878; died September 18, 1951 in Bude,
  Cornwall, England).
- **Designer & Conceptualizer**: **Arthur Edward Waite** (born October 2, 1857; died May 19, 1942 in
  London, England).

#### B. Territorial Copyright Expiration

1. **United States**:
    - Works published before January 1, 1929 are in the public domain in the United States
      regardless of author death date.
    - The 1909 Rider-Waite deck entered the US public domain decades ago.
2. **United Kingdom & European Union**:
    - The standard term of copyright is the author's lifetime plus 70 years (*p.m.a.*).
    - Waite died in 1942; his rights expired on January 1, 1913 + 70 = January 1, 2013.
    - Pamela Colman Smith died on September 18, 1951. Under UK/EU law (70 years after death), her
      copyright expired on **January 1, 2022**.
    - Therefore, since January 1, 2022, the Rider-Waite-Smith illustrations are in the **public
      domain throughout the UK and the European Union**.
3. **India**:
    - Under Section 22 of the Indian Copyright Act, 1957, the copyright term for published works is
      the lifetime of the author plus 60 years.
    - Waite (d. 1942): rights expired January 1, 2003.
    - Smith (d. 1951): rights expired on **January 1, 2012**.
    - Therefore, the deck has been in the public domain in India for over a decade.

#### C. Scan and Restoration Rights (Bridgeman v. Corel Doctrine)

- The images in `mixvlad/TarotCards/tarot/rider-waite` are scans of an original 1909 "Pam-A"
  printing cleaned and restored by Steve P. of `steve-p.org`.
- Steve P. explicitly dedicates the cleaned scans to the public domain (
  `https://steve-p.org/cards/RWSa.html`).
- Under *Bridgeman Art Library v. Corel Corp.* (36 F. Supp. 2d 191, S.D.N.Y. 1999) and analogous
  UK/EU jurisprudence (*Hyperion Records v. Sawkins*, *EU Directive 2019/790 Article 14*), faithful
  reproductions of public domain two-dimensional visual art do not generate a new copyright.

#### D. Attribution

Ethical and historical attribution is preserved in all manifests, metadata, card detail dialogs, and
reports:
> *"Original artwork by Pamela Colman Smith (1878–1951) and Arthur Edward Waite (1857–1942),
published December 1909 by William Rider & Son, London. Scan restoration by Steve P. (steve-p.org).
Public Domain worldwide."*

---

## 4. In-Depth Legal Analysis: Optional Secondary Deck

### Deck: Soimoi Tarot (`soimoi`)

- **Author**: Mike Koz (`https://koz.tv/`).
- **Year**: 2025.
- **License**: **Creative Commons Attribution 4.0 International (CC BY 4.0)**.
- **Redistribution Eligibility**: Permitted for commercial and non-commercial redistribution with
  attribution.
- **Attribution Text**:
  > *"Soimoi Tarot by Mike Koz (https://koz.tv/), licensed under CC BY
  4.0 (https://creativecommons.org/licenses/by/4.0/)."*
- **AYNVORA Deployment**: Handled as an **optional on-demand downloadable deck** via
  `TarotAssetDownloader` and `TarotDeckAssetManager`.

---

## 5. Verification Checksums

All card assets are validated against SHA-256 digests generated during developer asset preparation:

- Canonical manifest checksum is cryptographically verified upon bundle loading.
- Mismatched, corrupt, or truncated images trigger `TarotAssetVerificationException` and fail
  closed.
