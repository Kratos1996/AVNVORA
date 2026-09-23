# AYNVORA Performance
Version: 1.0

## Goals
Fast startup, responsive UI, efficient calculations, low memory use and low battery/network cost.

## Rules
- avoid unnecessary allocations in calculation-heavy paths
- cache deterministic results when safe
- keep heavy work off the UI thread
- minimize network usage
- avoid continuous decorative animation
- measure before optimizing

## KMP
Use platform-appropriate dispatching and concurrency without duplicating domain logic.
