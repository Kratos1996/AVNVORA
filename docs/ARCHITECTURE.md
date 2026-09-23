# AYNVORA SDK Architecture — Foundation

## Principles

- Offline-first core calculations.
- Deterministic Astro Engine is separate from AI/explanation.
- UI consumes design-system tokens/components only.
- Public SDK API is separated from internal implementation.
- Platform-specific code is isolated behind expect/actual or platform adapters where required.
- Features are entitlement-aware and must not expose disabled capabilities.

## Dependency direction

`androidApp / iOS host / desktopApp` → `ui` → `design-system` + feature APIs → `astro-engine` / future data modules.

The Astro Engine must not depend on Compose UI.

## Planned layers

1. Public API
2. Feature/domain layer
3. Deterministic calculation engine
4. Data and rules
5. Offline storage
6. Licensing/entitlements
7. Platform adapters
8. UI/design system

## Non-negotiable UI rule

The AYNVORA Master UI Style Guide is the single source of truth. A new visual pattern must be formalized as a token/component/pattern before reuse.
