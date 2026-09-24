# PHASE 8.0 — ASTROLOGY PREDICTIVE CORE & TIMING ENGINE

## 1. Architectural Overview

Phase 8.0 elevates AYNVORA from a pure deterministic astronomical calculation framework into a
structured, provenance-aware, and reproducible Astrological Prediction & Timing Engine.

The architecture adheres strictly to:

- **Clean Architecture & Separation of Concerns**: Astrological calculation authority resides
  entirely within `:astro-engine`. Prediction interpretation, rule matching, evidence graphs, and
  timing window generation reside in `:aynvora-core`.
- **Evidence-First Interpretation**: No predictions or guidance statements are generated without
  explicit contributing evidence nodes in the `EvidenceGraph`.
- **Deterministic & Offline-First**: Calculations execute locally without network access or cloud
  dependencies.
- **AI/SLM Barrier**: Machine learning agents act strictly as explanation engines. They never
  calculate planetary positions, Dasha dates, or yoga matches, nor do they access Room databases
  directly.

```mermaid
graph TD
    subgraph AstroEngine [astro-engine (Calculation Authority)]
        Chart[Birth Chart Calculation]
        Dasha[Vimshottari Dasha Engine]
        Transit[Planetary Transit Engine]
        Panchang[Five-Limb Panchang Engine]
    end

    subgraph CoreEngine [aynvora-core (Prediction & Timing Engine)]
        TopicReq[Topic Requirement Validator]
        Rules[Classical Rule Engine]
        Converge[Multi-Signal Convergence]
        TimingGen[Timing Window Generator]
        Graph[Prediction Evidence Graph]
    end

    subgraph Output [Structured Result]
        PredResult[PredictionResult]
        WhyTrace[traceWhy Backwards Traceability]
    end

    Chart --> TopicReq
    Dasha --> TopicReq
    Transit --> TopicReq
    Panchang --> TopicReq

    TopicReq --> Rules
    Rules --> Converge
    Converge --> TimingGen
    TimingGen --> Graph
    Graph --> PredResult
    Graph --> WhyTrace
```

---

## 2. Calculation Engines (:astro-engine)

### 2.1 Vimshottari Dasha Engine (`VimshottariDashaCalculator`)

- **Tradition**: Parashara Classical (BPHS Ch. 46).
- **Cycle**: Standard 120-year cycle across 9 Grahas (Ketu, Venus, Sun, Moon, Mars, Rahu, Jupiter,
  Saturn, Mercury).
- **Nakshatra-Based Balance**: Computes balance of the birth Nakshatra at the Moon's longitude down
  to millisecond epoch precision.
- **Hierarchy Supported**:
    - Mahadasha (Level 1)
    - Antardasha / Bhukti (Level 2)
    - Pratyantardasha (Level 3)
- **Provenance**: Carries source reference (`Brihat Parashara Hora Shastra, Ch. 46`), engine
  version (`8.0.0`), and calculation timestamp.

### 2.2 Planetary Transit Engine (`TransitCalculator`)

- **Authority**: Meeus Astronomical Algorithms / VSOP87 analytical core.
- **Calculations**:
    - Geocentric planetary snapshots at any target epoch.
    - Multi-day transit timelines across requested date ranges.
    - Planetary ingress and sign transitions.
    - Retrograde / direct motion detection.
    - Natal chart interactions (transiting Grahas relative to natal Janma Rashi and Lagna).
- **Timezone Safety**: Explicit epoch conversions ensuring deterministic repeatability across all
  timezones.

### 2.3 Five-Limb Panchang Engine (`PanchangCalculator`)

- **Components**:
    1. **Tithi**: Angular distance between Moon and Sun (12° segments, 1–30, Shukla/Krishna Paksha).
    2. **Nakshatra**: Moon's sidereal longitude in 13°20' spans (1–27).
    3. **Yoga**: Sum of Sun and Moon sidereal longitudes in 13°20' spans (1–27, Vishkambha to
       Vaidhriti).
    4. **Karana**: Half-tithi segments (6° spans, 1–11, 7 repeating movable + 4 fixed).
    5. **Vara**: Solar weekday (Ravivara to Shanivara) derived from solar sunrise/epoch.
- **Profile**: `SURYA_SIDDHANTA_V1` & `MODERN_EPHEMERIS_CHITRAPAKSHA`.

---

## 3. Rule Evaluation & Prediction Engine (:aynvora-core)

### 3.1 Life Topic Taxonomy (`PredictionTopic`)

Predictions are categorized into 13 typed life domains:

1. `CAREER`: Profession, status, work changes, authority.
2. `BUSINESS`: Enterprise, partnerships, commercial ventures.
3. `FINANCE`: Wealth accumulation, assets, investments.
4. `RELATIONSHIP`: Love, emotional connections, courtship.
5. `MARRIAGE`: Formal union, partnership harmony, matrimonial timing.
6. `EDUCATION`: Academic pursuits, higher learning, intellectual acquisition.
7. `HEALTH_WELLBEING`: Traditional dosha/vitality balance (Strict non-medical advisory disclaimer).
8. `FAMILY`: Domestic harmony, lineage, parents, progeny.
9. `TRAVEL`: Journeys, pilgrimages, temporary relocations.
10. `RELOCATION`: Permanent residence changes, foreign settlement.
11. `PROPERTY`: Land, real estate, vehicle acquisition.
12. `SPIRITUALITY`: Sadhana, inner evolution, philosophical orientation.
13. `DAILY_FOCUS`: Micro-guidance for daily activities.

### 3.2 Classical Rule Catalog (`CanonicalAstrologyRules`)

Implemented classical rules from foundational texts:

- `RULE_BPHS_YOGAKARAKA`: Yogakaraka Graha activation for Taurus, Cancer, Libra, Capricorn, and
  Aquarius Ascendants (BPHS Ch. 34).
- `RULE_BPHS_DHANA_YOGA`: Conjunction/aspect of 2nd, 5th, 9th, or 11th house lords for financial
  abundance (BPHS Ch. 41).
- `RULE_BPHS_GAJAKESARI`: Jupiter in Kendra from Moon, free of malefic combustion (BPHS Ch. 36).
- `RULE_GOCHARA_10TH_HOUSE`: Beneficial Jupiter or Saturn transiting the 10th house from natal
  Ascendant or Moon (Phaladeepika Ch. 26).
- `RULE_GOCHARA_7TH_LORD`: Benefic activation of the 7th house or 7th lord via transit (Saravali Ch.
  31).

### 3.3 Evaluation States

Every rule evaluation returns one of:

- `MATCHED`: Conditions fully satisfied.
- `NOT_MATCHED`: Baseline criteria unmet.
- `BLOCKED_BY_EXCEPTION`: Cancelled or blocked by opposing classical yoga/combustion.
- `CANCELLED`: Neutralized by Neecha Bhanga or opposing planetary aspect.
- `INSUFFICIENT_DATA`: Missing required planetary positions, houses, or Dasha periods.
- `CONFLICTING`: Contradictory signals detected across traditions.

---

## 4. Multi-Signal Convergence & Timing Windows

### 4.1 Convergence Logic

Signals are combined deterministically based on four weighted tiers:

- **Primary Signals**: Active Mahadasha/Antardasha lord alignments.
- **Supporting Signals**: Major transit triggers (Jupiter/Saturn transiting key houses).
- **Modifier Signals**: Natal strength, Panchang auspiciousness (Shubha Tithi/Yoga).
- **Contradicting Signals**: Saturn Sade Sati, debilitation, or enemy house placements.

Confidence is classified as `HIGH`, `MEDIUM`, or `LOW` based on multi-tier convergence. Arbitrary
numerical percentages (e.g., "98.5% accurate") are forbidden by governance.

### 4.2 Timing Window Generator

Instead of claiming minute-precision future events, the engine bounds events within structured
`TimingWindow` ranges:

- **Horizon**: Configurable search window (typically 30 to 365 days).
- **Window Hierarchy**: `PRIMARY` (peak dasha/transit intersection) and `SECONDARY` (supporting
  sub-periods).
- **Date Ranges**: Expressed as ISO-8601 start/end timestamps accompanied by the contributing rule
  IDs and graha activations.

---

## 5. Traceability & The Prediction Evidence Graph

### 5.1 Evidence Graph Structure

Every prediction constructs an `EvidenceGraph` where each node represents an observed astrological
fact:

- **Source Data**: Natal Graha positions, houses, and degrees.
- **Timing Factors**: Active Dasha level, start and end dates.
- **Transit Events**: Ingress dates, retrograde stations.
- **Evaluated Rules**: Exact rule ID, tradition, and textual source reference.

### 5.2 `traceWhy` Mechanism

Clients can invoke `traceWhy(predictionId)` or traverse `prediction.evidenceGraph` to retrieve the
complete causal tree of any prediction. This guarantees that no prediction appears as a "black box"
and empowers users to see the exact classical scriptures and chart configurations behind every
insight.

---

## 6. AI/SLM Execution Boundary

```
[User Query]
     │
     ▼
[AI Tool Router] ──(Dispatches Request)──► [AynvoraSdk / Prediction Engine]
                                                     │
                                                     ▼
                                            (Deterministic Room / Engine)
                                                     │
                                                     ▼
[Local SLM Explainer] ◄──(Structured Result)─────────┘
     │
     ▼
[Ethical & Clear Natural Language Explanation]
```

The SLM operates under strict constraints:

1. **Zero Calculation Authority**: May not compute planetary longitudes, Dasha dates, or rule
   matches.
2. **Zero Direct DB Access**: Interacts exclusively through typed use cases and SDK methods.
3. **Evidence Fidelity**: Explanations must strictly reflect the structured facts present in the
   `PredictionResult`.
4. **Deterministic Fallback**: If the SLM is unavailable, offline template-based rendering provides
   the user-facing interpretation seamlessly.
