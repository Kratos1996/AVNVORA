# AYNVORA Data Architecture
Version: 1.0

## Principles
- Offline-first for core calculation.
- Data is versioned and integrity checked.
- Minimize stored personal data.
- Prefer immutable/versioned reference data.
- Separate user data, astrology reference data, configuration and licensing data.

## Data Classes
1. Reference astrology data
2. User profile/birth data
3. Generated calculation results/cache
4. App configuration
5. License/entitlement state
6. Diagnostics/telemetry

## Storage
Use platform-secure storage for secrets and privacy-sensitive values. Use a local database for structured offline data where justified.

## Updates
Updates must be signed/integrity checked, versioned, backward compatible where required, and safe to roll back.
