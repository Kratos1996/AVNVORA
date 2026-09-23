# AYNVORA Error Handling
Version: 1.0

## Categories
- validation
- unsupported feature
- calculation/data error
- storage error
- network error
- license/entitlement error
- security/integrity error
- platform error

## Rules
Expected failures use typed results/domain errors. Do not expose sensitive internals to users.

## UI
Every user-facing flow defines an actionable error state where applicable.

## Logging
Internal diagnostic detail must not leak secrets or unnecessary personal data.
