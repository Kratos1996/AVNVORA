# AYNVORA Architecture Specification
Version: 1.0

## Architectural Layers
1. Platform/host applications
2. SDK public API
3. Feature/domain modules
4. Astro Engine and deterministic rules
5. Data/offline layer
6. Security/crypto layer
7. Licensing/entitlements
8. Design System/UI
9. Platform adapters

## Dependency Direction
UI → feature/application APIs → domain engines → data abstractions.
Security and licensing are cross-cutting infrastructure with strict boundaries.

## Rules
- UI cannot calculate astrology.
- Astro Engine cannot depend on Compose UI.
- Domain logic cannot depend on platform UI.
- Shared code is preferred.
- Platform code is isolated behind interfaces where practical.
- Public SDK API is smaller than internal implementation.

## Target SDK Shape
aynvora-core
aynvora-astro-engine
aynvora-astro-rules
aynvora-panchang
aynvora-charts
aynvora-dasha
aynvora-transit
aynvora-yoga
aynvora-ashtakavarga
aynvora-shadbala
aynvora-jaimini
aynvora-kp
aynvora-muhurta
aynvora-matching
aynvora-prashna
aynvora-intelligence
aynvora-guidance
aynvora-reports
aynvora-design-system
aynvora-ui
aynvora-data
aynvora-storage
aynvora-security
aynvora-licensing
aynvora-analytics
aynvora-platform

Modules are introduced only when their boundary is justified; do not create empty modules just for naming.
