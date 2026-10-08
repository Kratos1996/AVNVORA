# AYNVORA Versioning & Compatibility Matrix

This document outlines the versioning scheme and compatibility boundaries for the AYNVORA modular SDK.

---

## 1. Version Identifiers

Every engine invocation returns a canonical provenance header including:

- `sdkVersion`: Version of the public SDK facade (e.g. `10.45.0`)
- `contractsVersion`: Version of `:aynvora-contracts` protocol (e.g. `1.0.0`)
- `engineVersion`: Version of the executing feature engine (e.g. `10.45.0`)
- `schemaVersion`: Version of the request/response JSON schema (e.g. `1.0.0`)

---

## 2. Compatibility Guarantees

1. **Deterministic Canonical JSON**:
   Schema version bumps are backwards-compatible. New fields are always optional.

2. **No Breaking Removals**:
   Any deprecated fields remain populated for a minimum of two minor releases.

3. **Module Independence**:
   Engine patch upgrades do not require changes in consuming applications unless schema versions change.
