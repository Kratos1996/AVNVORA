# ASTROLOGY TIMING ENGINE ARCHITECTURE

## 1. Timing Principles

The AYNVORA Timing Engine solves the classical question: *"When does a planetary potential manifest
into an active window?"*

In accordance with Master Rule 61, the engine explicitly rejects false minute-level precision for
life events. Planetary influences operate across observable ranges bounded by Dasha sub-periods and
Gochara (transit) triggers.

---

## 2. Multi-Signal Convergence Architecture

Astrological timing is determined by the intersection of three concentric timing cycles:

```
+---------------------------------------------------------+
| Level 1: Mahadasha (Macro Climate - Years to Decades)   |
|   +---------------------------------------------------+ |
|   | Level 2: Antardasha (Focused Phase - Months)       | |
|   |   +---------------------------------------------+ | |
|   |   | Level 3: Gochara / Transit (Trigger - Days)  | | |
|   |   +---------------------------------------------+ | |
|   +---------------------------------------------------+ |
+---------------------------------------------------------+
```

1. **Mahadasha**: Defines the broad qualitative theme of the native's life chapter.
2. **Antardasha (Bhukti)**: Narrowers the scope to specific house rulerships and yogas.
3. **Gochara (Transits)**: Serves as the localized catalyst or trigger when slow-moving planets (
   Saturn, Jupiter, Rahu/Ketu) or fast-moving planets (Sun, Mars, Venus, Mercury) aspect or enter
   key natal points.

---

## 3. Timing Window Generation Process

When a client queries `generateTimingWindows(chart, topic, horizonDays)`:

1. **Active Dasha Resolution**:
    - The engine looks up all Mahadasha, Antardasha, and Pratyantardasha periods spanning
      `[epoch, epoch + horizonDays]`.
    - Planetary lords governing the query topic (e.g. 10th lord for `CAREER`, 2nd/11th lords for
      `FINANCE`) are flagged.

2. **Transit Timeline Calculation**:
    - The engine queries `TransitCalculator.calculateTransitTimeline` to obtain daily or multi-day
      planetary coordinates over the horizon.
    - Significant events (sign ingresses, stationary retrograde turns, Kendra transits) are
      isolated.

3. **Convergence Filtering**:
    - Overlapping date intervals where both Dasha and Gochara provide supporting evidence are
      grouped into contiguous `TimingWindow` objects.
    - Intervals are classified into:
        - `PRIMARY`: Both Dasha and Transit signals converge strongly.
        - `SECONDARY`: Dasha or Transit signal is active with supporting natal configurations.

4. **Bounding & Presentation**:
    - Windows output an ISO-8601 start and end timestamp.
    - Windows attach references to the exact `ruleIds` and `evidenceIds` responsible for the
      determination.

---

## 4. Bounded Horizon Safety

- The engine avoids scanning open-ended infinite ranges to guarantee bounded CPU execution and
  determinism.
- Maximum recommended horizon is 365 days for standard predictions and 1825 days (5 years) for
  long-range dasha overviews.
- All calculations execute synchronously in under 20 milliseconds on mobile runtimes.

## 5. Reference boundary classification

Dasha boundaries are first compared as exact instants; a non-identical boundary is never
`EXACT_MATCH`. Against JKR p.127 dates interpreted as 00:00 UTC, Rahu MD starts 2.3457 days later
and ends 2.8457 days later. This is the exact difference under that interpretation, not an accepted
tolerance match. The date-only report omits timezone, month/year arithmetic, and endpoint-time
conventions, while p.4/p.5 also conflict with p.127 on subperiod dates. Classify the boundary as
`REFERENCE_AMBIGUITY`; see [REFERENCE_VALIDATION_JKR.md](REFERENCE_VALIDATION_JKR.md).
