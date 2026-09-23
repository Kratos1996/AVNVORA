# Phase 5.2 — Aspects + Conjunctions + Planet States Engine

## 1. Scope
Phase 5.2 establishes a deterministic, offline-first factual calculation engine for:
- Pairwise shortest circular angular separations in $[0.0^\circ, 180.0^\circ]$.
- Astrological major aspect detection (Conjunction, Sextile, Square, Trine, Opposition).
- Configurable aspect definitions and orb tolerances via `AspectProfile`.
- Deduplicated, non-self distinct pair evaluations with deterministic ordering.
- Apparent planetary motion states (`DIRECT`, `RETROGRADE`).
- Classical Vedic solar combustion (Asta) detection across all planetary bodies.
- Public SDK integration through `:aynvora-core`.

Strictly **out of scope**: Astrological interpretations, psychological profiling, predictions, house lord relationships, yogas, dashas, divisional charts, and UI screens.

---

## 2. Angular Separation
For any two sidereal ecliptic longitudes $A$ and $B$, the shortest circular angular separation is defined as:
$$\text{diff} = |A - B| \bmod 360.0^\circ$$
$$\text{separation}(A, B) = \begin{cases} 360.0^\circ - \text{diff} & \text{if } \text{diff} > 180.0^\circ \\ \text{diff} & \text{otherwise} \end{cases}$$
**Mathematical Properties**:
- $0.0^\circ \le \text{separation}(A, B) \le 180.0^\circ$
- $\text{separation}(A, B) = \text{separation}(B, A)$ (Symmetry)
- $\text{separation}(A, A) = 0.0^\circ$ (Identity)

---

## 3. Aspect Model & Classification
Aspects are defined by target angles and allowed orb tolerances:
- **`CONJUNCTION`**: Exact angle $= 0.0^\circ$, Default orb $= 8.0^\circ$
- **`SEXTILE`**: Exact angle $= 60.0^\circ$, Default orb $= 6.0^\circ$
- **`SQUARE`**: Exact angle $= 90.0^\circ$, Default orb $= 7.0^\circ$
- **`TRINE`**: Exact angle $= 120.0^\circ$, Default orb $= 8.0^\circ$
- **`OPPOSITION`**: Exact angle $= 180.0^\circ$, Default orb $= 8.0^\circ$

An aspect exists between body $A$ and body $B$ if and only if:
$$\text{orb} = |\text{separation}(A, B) - \text{exactAngle}| \le \text{allowedOrb}$$

---

## 4. Aspect Profiles & Participating Bodies
- `AspectProfile` encapsulates the active aspect set and body inclusion policies.
- By default, all 9 celestial bodies (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn, Rahu, Ketu) participate.
- Lunar nodes (Rahu/Ketu) can be excluded via `includeLunarNodes = false`.
- Self-pairs ($A \leftrightarrow A$) and duplicate reversed pairs ($B \leftrightarrow A$) are strictly prevented. Results are sorted by `firstBody.ordinal`, `secondBody.ordinal`, and `type.ordinal`.

---

## 5. Planetary Motion States
- Reuses the Phase 4 longitudinal velocity calculation:
  - $\text{rate} = \frac{d\lambda}{dt}$
  - `RETROGRADE`: $\text{rate} < 0$
  - `DIRECT`: $\text{rate} \ge 0$
- Sun and Moon are always `DIRECT`.
- Mean Rahu and Ketu are always `RETROGRADE`.

---

## 6. Planetary Combustion (Asta)
Combustion is evaluated based on the angular separation from the Sun ($\text{separation}(\lambda_{\text{Sun}}, \lambda_{\text{Planet}})$) using classical Vedic standards (Surya Siddhanta & Brihat Parashara Hora Shastra):
| Celestial Body | Motion State | Combustion Threshold |
| :--- | :--- | :--- |
| **Moon** | Direct | $12.0^\circ$ |
| **Mars** | Direct / Retrograde | $17.0^\circ$ |
| **Mercury** | Direct | $14.0^\circ$ |
| **Mercury** | Retrograde | $12.0^\circ$ |
| **Jupiter** | Direct / Retrograde | $11.0^\circ$ |
| **Venus** | Direct | $10.0^\circ$ |
| **Venus** | Retrograde | $8.0^\circ$ |
| **Saturn** | Direct / Retrograde | $15.0^\circ$ |
| **Sun** | Direct | `NOT_APPLICABLE` |
| **Rahu / Ketu** | Retrograde | `NOT_APPLICABLE` |

---

## 7. Public SDK Models
In `:aynvora-core`:
- `ChartResult.aspects`: `List<Aspect>` (`firstBody`, `secondBody`, `type`, `exactAngle`, `actualSeparation`, `orb`).
- `ChartResult.planetStates`: `List<PlanetState>` (`body`, `motionState`, `combustionState`, `separationFromSun`, `combustionThresholdDegrees`).
- `PlanetaryPosition`: Enriched with `motionState: PlanetMotionState` and `combustionState: CombustionState`.

---

## 8. Localization Isolation
All aspect types (`CONJUNCTION`, `TRINE`, etc.), motion states (`DIRECT`, `RETROGRADE`), and combustion conditions (`COMBUST`, `NORMAL`, `NOT_APPLICABLE`) are exposed as strongly typed Kotlin enums and identifiers without presentation strings inside the calculation engine.

---

## 9. Verification & Quality Gate Results
- **Unit Tests**: 100% tests passing across all modules (`:astro-engine`, `:aynvora-core`, `:aynvora-data`, `:design-system`, `:aynvora-localization`).
- **Android App**: `./gradlew :androidApp:assembleDebug` builds cleanly.
- **Desktop App**: `./gradlew :desktopApp:packageDistributionForCurrentOS` builds cleanly.
- **iOS KMP Targets**: `:compileKotlinIosArm64` builds cleanly across all modules.

---

## 10. Deferred Work & Next Phase
- **Divisional Charts (Vargas: D9 Navamsha, D10, D12, etc.)**: Deferred to Phase 5.3+.
- **Astrological Yogas, Dashas, Shadbala, and Interpretation**: Deferred to subsequent phases.
