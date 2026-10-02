# Tājika Nīlakaṇṭhī classical rule extraction

**Phase:** 10.25 — Classical Tajika Verification, Primary-Source Rule Extraction, Full Varshaphal Engine, Differential Testing, and Real Tool Registration
**Review date:** 2026-10-02
**Primary source:** Nīlakaṇṭha Daivajña, *Tājika Nīlakaṇṭhī*, with Mahidhara Hindi commentary, Khemraj Shri Venkateshwar Steam Press, Bombay, 1907 edition. The digital scan is 280 PDF pages. Scan SHA-256: `a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989`.
**Scan record:** [Wikimedia Commons file record](https://commons.wikimedia.org/wiki/File:%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_(%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%B9%E0%A4%BF%E0%A4%A4%E0%A4%BE).pdf) · [Wikisource index/transcription](https://sa.wikisource.org/wiki/%E0%A4%85%E0%A4%A8%E0%A5%81%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%AE%E0%A4%A3%E0%A4%BF%E0%A4%95%E0%A4%BE:%E0%A4%A4%E0%A4%BE%E0%A4%9C%E0%A4%BF%E0%A4%95%E0%A4%A8%E0%A5%80%E0%A4%B2%E0%A4%95%E0%A4%A3%E0%A5%8D%E0%A4%A0%E0%A5%80_(%E0%A4%AE%E0%A4%B9%E0%A5%80%E0%A4%A7%E0%A4%B0%E0%A4%95%E0%A5%83%E0%A4%A4%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE%E0%A4%9F%E0%A5%80%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%B9%E0%A4%BF%E0%A4%A4%E0%A4%BE).pdf).

---

## 1. Primary source rights and handling

| Field | Record |
|---|---|
| sourceId | `tajika-neelakanthi-1907-scan` |
| publicationYear | 1907 CE (Samvat 1964 / Śaka 1829) |
| author | Nīlakaṇṭha Daivajña (commentary by Mahīdhara) |
| edition | Khemraj Shri Venkateshwar Steam Press, Bombay |
| repository | Wikimedia Commons (Digitalized Sanskrit Corps) |
| sourceUrl | `https://commons.wikimedia.org/wiki/File:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf` |
| rightsStatus | `PUBLIC_DOMAIN_INDIA`, `PUBLIC_DOMAIN_US` |
| territorialNote | Public domain in India and the United States per Commons record; territorial status may vary elsewhere. Do not redistribute binary scan globally without clearance. |
| attributionRequirement | Retain attribution to original 1907 edition and Wikimedia Commons scan record. |
| shareAlikeRequirement | Wikisource text is licensed under `CC_BY_SA_4_0`. Adaptations of transcribed textual summaries preserve CC BY-SA 4.0 attribution and share-alike notices. |
| retrievalDate | 2026-10-01 / 2026-10-02 |
| contentHash | SHA-256 `a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989` |

---

## 2. Work coverage and location map

| Book portion | PDF pages | Printed pages | Contents and verified folios |
|---|---:|---:|---|
| **Saṃjñā tantra** | 1–107 | 1–99 | Planetary characteristics, friendship/enmity, aspects & Deeptamshas (Ch. 2 vv. 13–14 / PDF 49), Trirashipati (vv. 61–62 / PDF 40–41), Sixteen Tajika Yogas (Itthashala, Ishrafa, etc., Ch. 3 vv. 1–10 / PDF 50–55), Sahams & Saika-bham (+30°) arc correction (vv. 5, 6, 12 / PDF 87–89). |
| **Varṣa tantra** | 108–201 | 100–193 | Ingress rules, Panchadhikaris & Varsheshwara selection (vv. 5–8 / PDF 110–111), Muntha progression & house indications (vv. 1–18 / PDF 120–124), Monthly/Daily charts, Mudda Dasha discussion (p. 192 vv. 14–15). |
| **Praśna tantra** | 202–280 | 194–272 | Horary Tajika astrology (outside the Varshaphal scope of this phase). |

---

## 3. Extraction table

The table below documents every extracted classical rule against the 1907 scan of *Tājika Nīlakaṇṭhī*.

| Rule ID | Feature | Primary Source | Book Page (PDF) | Printed Page | Chapter | Verse / Sloka | Sanskrit Text Reference | Commentary Reference | Interpretation | Algorithm | Inputs | Outputs | Day/Night Variation | Ambiguity | Independent Cross-check | Status |
|---|---|---|---:|---:|---|---|---|---|---|---|---|---|---|---|---|---|
| **TN-MUN-01** | Muntha annual progression | Tājika Nīlakaṇṭhī (1907) | 120 | 112 | Varṣa tantra, Muntha Adhyāya | 1 | स्वजन्मराशेः प्रतिवर्षमेकैकाराशिप्रवृद्ध्या... | टीका: जन्मलग्न के अंश वही रहते हैं, प्रतिवर्ष एक-एक राशि आगे बढ़ती है | Muntha advances by exactly one sign per elapsed annual cycle from natal ascendant, retaining the within-sign degree. | `(natalSign + elapsedCycles) % 12`; `long = sign*30 + (natalLong % 30)` | `natalAscendantLongitude: Double`, `elapsedSolarReturnCycles: Int` | `MunthaCalculation(sign, longitude)` | None | None | MayaAstrolib, BV Raman Varshaphal | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-MUN-02** | Muntha Lord (Munthesha) | Tājika Nīlakaṇṭhī (1907) | 121 | 113 | Varṣa tantra, Muntha Adhyāya | 3 | यस्यां राशौ मुन्था... स्वामी मुन्थेशः | टीका: जिस राशि में मुन्था हो, उस राशि का स्वामी मुन्थेश कहलाता है | The planetary ruler of the sign occupied by Muntha is the Muntha Lord. | Standard 7-graha sign rulership applied to `muntha.sign` | `muntha.sign: Rashi` | `MunthaLordResult(lord, munthaSign)` | None | None | Classic Tajika standard | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-VAR-01** | Panchadhikāri candidates | Tājika Nīlakaṇṭhī (1907) | 110–111 | 102–103 | Varṣa tantra, Varṣeśa Adhyāya | 5–7 | जन्मेशवर्षेशमुन्थेशास्त्रिराशीशश्च वासरे सूर्य्यः... | टीका: पांच अधिकारी: जन्मलग्नेश, वर्षलग्नेश, मुन्थेश, त्रिराशिपति, दिनाधिप/रात्र्यधिप | Five candidates qualify for Year Lord: Janma Lagnesha, Varsha Lagnesha, Munthesha, Trirashipati, and Dina/Ratri-pati. | Evaluate the 5 candidate roles from chart placements | `natalAscendant`, `annualChart`, `muntha`, `isDay` | `List<VarsheshwaraCandidateResult>` | Day: Sun sign lord; Night: Moon sign lord | None on candidate list | BV Raman, Dr. KS Charak | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-VAR-02** | Trirāśipati rulership | Tājika Nīlakaṇṭhī (1907) | 40–41 | 32–33 | Saṃjñā tantra | 61–62 | कुजेन्दुशुक्राः... दिने रात्रौ च त्रिराशीशाः | टीका: दिन और रात्रि के अनुसार मेषादि १२ राशियों के त्रिराशिपति | Each sign has a designated day ruler and night ruler for Trirāśi lordship. | Table lookup of `(sign, isDay)` per v. 61 | `sign: Rashi`, `isDay: Boolean` | `trirashipati: String` | Distinct day vs night rulers for all 12 signs | None | Standard Tajika treatises | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-VAR-03** | Varsheshwara aspect qualification & Panchavargiya selection | Tājika Nīlakaṇṭhī (1907) | 110–111 | 102–103 | Varṣa tantra | 7–8 | लग्नावलोककाः श्रेष्ठाः... बलेन संयुताः | टीका: जो ग्रह वर्षलग्न को देखता हो वही वर्षेश हो सकता है, उनमें जो पंचवर्गीय बल में श्रेष्ठ हो | Candidate must cast an aspect on annual Lagna. Planets in houses 2, 6, 8, 12 cannot aspect. Highest Panchavargiya Bala among eligible wins. If none aspects, highest overall wins per v. 7. | Filter candidates by aspect to Lagna; calculate 5-fold Panchavargiya virupas (Kshetra, Uccha, Hadda, Drekkana, Navamsha); select maximum | Candidates, annual placements, Lagna sign | `VarsheshwaraResult(selectedPlanet, eligibility, strengthBreakdown)` | Dina/Ratri-pati depends on solar/lunar ingress | Alternative fallback opinions exist when multiple planets tie | Cross-checked with classical commentaries; tie-break favors Munthesha / Varsha Lagnesha | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-SAH-01** | Puṇya Saham & Saika-bham (+30°) arc correction | Tājika Nīlakaṇṭhī (1907) | 87 | 79 | Saṃjñā tantra, Saham chapter | 5 | दिने चन्द्राद्विशोध्योऽर्कः... सैकभम् | टीका: दिन में चन्द्र - सूर्य + लग्न; रात्रि में सूर्य - चन्द्र + लग्न। यदि लग्न शोध्य-शुद्ध्याश्रय के बीच न हो तो ३०° (१ राशि) जोड़े | Fortune/Merit Saham. Day: Moon - Sun + Lagna; Night: Sun - Moon + Lagna. If Lagna is outside the forward arc from subtrahend to minuend, add 30°. | Base = (A - B + Lagna). If Lagna outside forward arc B->A, add 30°. | Sun, Moon, Lagna longitudes, `isDay: Boolean` | `SahamResult(PUNYA, longitude, formula)` | Formula inverts between Day and Night | Arc boundary condition resolved via commentary example (1907 ed.) | Cross-checked against Mahidhara 1907 printed calculation | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-SAH-02** | Vidyā / Guru Saham | Tājika Nīlakaṇṭhī (1907) | 88 | 80 | Saṃjñā tantra, Saham chapter | 6 | विद्याख्यं सहमं ज्ञेयं विपरीतेन भास्वता | टीका: पुण्य सहम का विपरीत: दिन में सूर्य - चन्द्र + लग्न; रात्रि में चन्द्र - सूर्य + लग्न | Knowledge/Wisdom Saham. Inverted calculation of Punya Saham, subject to identical +30° arc rule. | Day: Sun - Moon + Lagna; Night: Moon - Sun + Lagna (+30° if Lagna outside arc) | Sun, Moon, Lagna longitudes, `isDay: Boolean` | `SahamResult(VIDYA, longitude, formula)` | Inverts day vs night | None | Standard Tajika treatises | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-SAH-03** | Yaśas Saham | Tājika Nīlakaṇṭhī (1907) | 88 | 80 | Saṃjñā tantra, Saham chapter | 6 | यशः सहमं जीवात्पुण्यात्... | टीका: दिन में गुरु - पुण्य + लग्न; रात्रि में पुण्य - गुरु + लग्न | Fame/Renown Saham. Day: Jupiter - Punya + Lagna; Night: Punya - Jupiter + Lagna (+30° if Lagna outside arc). | Day: Jupiter - Punya + Lagna; Night: Punya - Jupiter + Lagna | Jupiter, Punya Saham, Lagna, `isDay` | `SahamResult(YASAS, longitude, formula)` | Inverts day vs night | Depends on Punya Saham result | Consistent across classical texts | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-SAH-04** | Karma Saham | Tājika Nīlakaṇṭhī (1907) | 89 | 81 | Saṃjñā tantra, Saham chapter | 12 (comm. 37) | कर्मसहमं कुजाज्ज्ञाच्च... | टीका: दिन में मंगल - बुध + लग्न; रात्रि में बुध - मंगल + लग्न | Profession/Action Saham. Day: Mars - Mercury + Lagna; Night: Mercury - Mars + Lagna (+30° if Lagna outside arc). | Day: Mars - Mercury + Lagna; Night: Mercury - Mars + Lagna | Mars, Mercury, Lagna, `isDay` | `SahamResult(KARMA, longitude, formula)` | Inverts day vs night | None | Standard Tajika texts | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-ASP-01** | Tajika Aspects & Deeptāṃśa (Orbs) | Tājika Nīlakaṇṭhī (1907) | 49 | 41 | Saṃjñā tantra Ch. 2 | 13–14 | दीप्तांशाः सूर्य्यस्य १५ चन्द्रस्य १२ कुजस्य ८ बुधस्य ७ गुरोः ९ शुक्रस्य ७ शनेः ९... | टीका: ग्रहों के दीप्तांश... दोनों के दीप्तांशों का योग कर आधा करने से दृष्टि की सीमा बनती है | Planetary orbs: Sun 15°, Moon 12°, Mars 8°, Mercury 7°, Jupiter 9°, Venus 7°, Saturn 9°. Effective aspect orb is the mean of both planets' Deeptamshas. Recognized aspects: 1-1 (0°), 3-11 (60°), 4-10 (90°), 5-9 (120°), 1-7 (180°). Houses 2, 6, 8, 12 cast no aspect (Adrishti). | Calculate angular separation; if within combined orb `(orb1+orb2)/2`, record aspect | Two planet longitudes and identities | `TajikaAspectResult(orb, relationship, aspectType)` | None | Some later authors suggest flat 12° orb, but verse 13 planetary values are primary | Ptolemaic/Tajika consensus | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-ASP-02** | Itthashāla (Muthashila) & Ishrāfa (Musaripha) Yogas | Tājika Nīlakaṇṭhī (1907) | 50–55 | 42–47 | Saṃjñā tantra Ch. 3 | 1–5 | शीघ्रो मन्दगतेर्भागे न्यूने स्यादित्थशालकम्... विपरीते त्वीसराफः | टीका: शीघ्रगामी ग्रह मन्दगामी ग्रह से कम अंशों पर होकर जब दीप्तांश के भीतर हो तो इत्थशाल योग; यदि शीघ्रगामी के अंश अधिक हों तो ईसराफल योग | Faster planet (by speed hierarchy: Moon>Mer>Ven>Sun>Mars>Jup>Sat) having fewer degrees than slower planet in sign = Applying / Itthashala Yoga. If faster planet has greater degrees = Separating / Ishrafa Yoga. | Compare speed ranks; if within combined orb: `fasterDeg < slowerDeg` -> Itthashala; `fasterDeg > slowerDeg` -> Ishrafa | Two planets in aspect | `TajikaAspectResult(applying, separating, itthashala, ishrafa)` | None | None on core definition | Universally recognized in Tajika | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |
| **TN-MUD-01** | Muddā Dashā Proportional Sequence | Tājika Nīlakaṇṭhī (1907) & Tājika Muktāvalī | 192 | 184 | Varṣa tantra | 14–15 | मुद्दा दशा विंशोत्तरीक्रमेण... | टीका: वर्षप्रवेश काल से विंशोत्तरी दशा के अनुपात से १ वर्ष में ९ ग्रहों की दशा | Scales 120-year Vimshottari sequence proportionally to the exact annual solar return interval (~365.242 days). Sub-period duration = `(years / 120) * annualInterval`. | 9 contiguous planetary periods starting from natal Moon nakshatra lord shifted by elapsed cycles; sum of durations equals solar return interval exactly. | `solarReturnMoment`, `natalMoonLongitude`, `elapsedCycles` | `List<MuddaDashaPeriodResult>` | None | Neelakantha names Mudda and refers method to Tajika Muktavali | Aligned with standard Varshaphal treatises | **VERIFIED_PRIMARY_WITH_CROSSCHECK** |

---

## 4. Operational implementation status

All six engines have been implemented and verified in the SDK:
- **`MunthaEngine`**: Full classical progression and within-sign degree preservation.
- **`MunthaLordEngine`**: Strict sign-lord mapping with observable annual chart placement.
- **`VarsheshwaraEngine`**: Evaluates all 5 office-bearers, enforces Lagna aspect qualification, computes 5-fold Panchavargiya Bala, resolves day/night ingress and tie-breaking.
- **`SahamEngine`**: Implements Punya, Vidya, Yasas, and Karma Sahams with exact circular-arc Saika-bham (+30°) correction and day/night formula inversion.
- **`TajikaAspectEngine`**: Implements 5 geometric aspect relationships, planetary Deeptamshas (orbs), speed ranking, applying/separating detection, and Itthashala / Ishrafa yogas.
- **`MuddaDashaEngine`**: Implements proportional Vimshottari annual scaling over the exact solar return duration with zero gap and zero overlap.
- **`AynvoraAiToolExecutor`**: Full registration and execution of all 7 classical Tajika tools calling actual engines on the acceptance path without mock fixtures.
