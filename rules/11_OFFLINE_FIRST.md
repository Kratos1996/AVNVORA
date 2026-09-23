# AYNVORA Offline-First Specification
Version: 1.0

## Offline Core
Birth-chart calculations and supported deterministic astrology features must work without network access after required reference data is installed.

## Network Use
Network is optional for:
- signed reference-data updates
- license/entitlement refresh
- optional AI/cloud services
- diagnostics where explicitly enabled

## Failure Rule
Network failure must not break supported offline calculations.

## Update Safety
Validate signature/integrity, schema compatibility and data version before activation. Keep the previous known-good dataset until the new dataset is validated.
