# CANONICAL ASTROLOGY PREDICTION RULES

This document serves as the authoritative registry of astrological rules evaluated by the AYNVORA
Prediction Engine (`:aynvora-core`). All rules are source-grounded in classical Vedic texts.

---

## 1. Rule Structure

Every rule in the AYNVORA rule engine conforms to the following schema:

- **`id`**: Unique string identifier (e.g. `RULE_BPHS_YOGAKARAKA`).
- **`name`**: Human-readable canonical title.
- **`tradition`**: Astrological tradition (e.g., `PARASHARA_CLASSICAL_V1`).
- **`sourceReference`**: Primary classical source (Text, Chapter, Sloka).
- **`primaryTopic`**: Target life topic (`PredictionTopic`).
- **`conditions`**: Logical requirements on Grahas, Bhavas, Rashis, or Dashas.
- **`exceptions`**: Conditions that suppress or block the rule (e.g., combustion, debility).
- **`cancellations`**: Conditions that neutralize a negative rule or restore a positive one (e.g.,
  Neecha Bhanga).
- **`weight`**: Relative contribution weight during multi-signal convergence.

---

## 2. Implemented Rule Catalog

### RULE_BPHS_YOGAKARAKA

- **Title**: Yogakaraka Graha Activation
- **Tradition**: `PARASHARA_CLASSICAL_V1`
- **Source**: *Brihat Parashara Hora Shastra*, Chapter 34 ("Effects of Planetary Periods and
  Yogakarakas")
- **Topic**: `CAREER`, `BUSINESS`
- **Weight**: `1.5`
- **Condition**: Single planet simultaneously owns a Kendra (1st, 4th, 7th, 10th) and a Trikona (
  5th, 9th) house.
    - Taurus Ascendant: Saturn (Lord of 9th and 10th)
    - Cancer Ascendant: Mars (Lord of 5th and 10th)
    - Leo Ascendant: Mars (Lord of 4th and 9th)
    - Libra Ascendant: Saturn (Lord of 4th and 5th)
    - Capricorn Ascendant: Venus (Lord of 5th and 10th)
    - Aquarius Ascendant: Venus (Lord of 4th and 9th)
- **Interpretation**: Dasha or significant transit of the Yogakaraka planet marks an auspicious
  period of professional elevation, strategic clarity, and administrative status.

---

### RULE_BPHS_DHANA_YOGA

- **Title**: Classical Dhana (Wealth) Yoga
- **Tradition**: `PARASHARA_CLASSICAL_V1`
- **Source**: *Brihat Parashara Hora Shastra*, Chapter 41 ("Special Yogas for Wealth")
- **Topic**: `FINANCE`, `PROPERTY`
- **Weight**: `1.4`
- **Condition**: Mutual reception, conjunction, or mutual aspect between lords of the 2nd house (
  Accumulated Wealth), 5th house (Speculation/Intellect), 9th house (Fortune/Bhagya), and 11th
  house (Gains/Labha).
- **Interpretation**: Activation of wealth houses through planetary synergy indicates favorable
  conditions for asset acquisition, enhanced resource inflows, and financial stability.

---

### RULE_BPHS_GAJAKESARI

- **Title**: Gaja Kesari Yoga
- **Tradition**: `PARASHARA_CLASSICAL_V1`
- **Source**: *Brihat Parashara Hora Shastra*, Chapter 36, Sloka 3–4
- **Topic**: `EDUCATION`, `CAREER`
- **Weight**: `1.3`
- **Condition**: Jupiter occupies a Kendra (1st, 4th, 7th, 10th) from the Moon, free from combustion
  by the Sun and not debilitated in Capricorn (unless Neecha Bhanga applies).
- **Interpretation**: Indicates enduring wisdom, academic excellence, public respect, and steadfast
  character. Brings clarity in institutional matters and scholastic pursuits.

---

### RULE_GOCHARA_10TH_HOUSE

- **Title**: Beneficial 10th House Transit
- **Tradition**: `PARASHARA_CLASSICAL_V1`
- **Source**: *Phaladeepika*, Chapter 26 ("Effects of Transits / Gochara")
- **Topic**: `CAREER`
- **Weight**: `1.2`
- **Condition**: Transiting Jupiter or Saturn traversing the 10th house from natal Lagna or Janma
  Rashi (Moon sign) with benefic aspects.
- **Interpretation**: The transit stimulates career responsibilities, structural professional
  transitions, and prominent project visibility.

---

### RULE_GOCHARA_7TH_LORD

- **Title**: 7th House / 7th Lord Benefic Transit Activation
- **Tradition**: `PARASHARA_CLASSICAL_V1`
- **Source**: *Saravali*, Chapter 31 ("Planetary Transits and Conjugal Life")
- **Topic**: `MARRIAGE`, `RELATIONSHIP`
- **Weight**: `1.2`
- **Condition**: Transiting Jupiter or Venus casting benefic aspect upon the natal 7th house or
  natal 7th lord.
- **Interpretation**: Favorable window for interpersonal harmony, formalization of alliances,
  courtship milestone progress, and diplomatic settlements.

---

## 3. Governance Constraints

1. **No Artificial Rules**: Rules must possess an explicit chapter and sloka attribution from
   classical literature.
2. **Conflict Preservation**: If multiple rules yield opposing results (e.g., benefic transit during
   a Maraka Dasha), both rules must be emitted with `CONFLICTING` status and documented in the
   convergence evidence.
3. **No Material Guarantees**: Interpretations must explicitly describe potential and qualitative
   influences rather than deterministic material certainties.

## Reference validation status

Calculation evidence consumed by a rule must retain its explicit engine/profile provenance. JKR
golden comparison statuses and currently unavailable coverage are documented
in [REFERENCE_VALIDATION_JKR.md](REFERENCE_VALIDATION_JKR.md). A test count does not imply an
equivalent count of reference-validated rule inputs. Existing prediction rules and their evidence
semantics are unchanged by the Phase 8.2 audit.
