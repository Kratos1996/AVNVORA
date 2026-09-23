# AYNVORA API Contracts
Version: 1.0

## Public API
Public APIs must be small, documented and stable.

## Rules
- Prefer immutable request/result models.
- Use explicit versioning for breaking changes.
- Do not expose internal implementation types unnecessarily.
- Validate inputs at API boundaries.
- Define error/result states rather than throwing uncontrolled exceptions for expected domain conditions.

## SDK Entry Point
A future public facade should provide configuration, capability discovery, calculation requests and supported feature access without exposing internal modules directly.
