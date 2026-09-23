# AYNVORA Testing Strategy
Version: 1.0

## Layers
- unit tests
- domain/property tests
- rule regression tests
- component/UI tests
- platform integration tests
- security tests
- serialization/data migration tests
- performance tests

## Astro Engine
Deterministic calculations require known test vectors and regression suites.

## Security
Authentication, authorization, entitlement signatures, update verification and secure storage boundaries require tests.

## Rule
A test that merely executes code without checking behavior is not considered sufficient validation.
