# AYNVORA Astro Engine
Version: 1.0

## Purpose
Provide deterministic, testable astrological calculations independent of UI and AI.

## Input Foundation
Birth date/time, place, coordinates, timezone and DST rules where applicable, plus calculation profile/convention.

## Calculation Domains
- planetary positions
- ascendant and houses
- nakshatra/pada
- retrograde/combustion
- aspects/conjunctions
- divisional charts
- dasha hierarchy
- transits
- panchang
- yoga
- shadbala
- ashtakavarga
- Jaimini
- optional KP
- matching
- prashna
- muhurta

## Determinism
Same inputs + same calculation profile + same data version must produce the same result.

## Traceability
Results should expose calculation/profile/data versions where useful.

## No Fake Results
Until a domain is implemented and validated, it must return an explicit unsupported/unavailable state rather than a fabricated value.
