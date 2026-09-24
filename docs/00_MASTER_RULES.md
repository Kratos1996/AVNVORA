# AYNVORA Master Rules
Version: 2.0 (Phase 7.0 — Clean Architecture + Analytics + Offline-First)

## Purpose
This document is the highest-level engineering and product governance rulebook for AYNVORA. Every contributor, developer, designer, automation, and AI coding agent must follow it.

## Non-Negotiable Rules
1. Read the relevant AYNVORA specification before modifying code.
2. Never bypass architecture for speed.
3. Reuse existing functionality before creating duplicate functionality.
4. Reuse approved UI components before creating one-off UI.
5. Never invent colors, typography, spacing, radius, motion, icons, or visual patterns outside the Design System.
6. Astro calculations belong to the Astro Engine, never inside UI code.
7. Never fabricate astrology results, calculation progress, confidence, or sources.
8. Never store secrets, private keys, tokens, or credentials in source control.
9. Security-sensitive changes require threat analysis and tests.
10. Minimize collection and retention of personal data.
11. Common KMP code is preferred unless platform-specific behavior is genuinely required.
12. Business/domain rules must not be duplicated across platforms.
13. Every interactive feature must define loading, empty, error, disabled and success states where applicable.
14. Every user-facing feature must meet accessibility requirements.
15. New dependencies require a documented reason and security/license review.
16. Do not make breaking public API changes without versioning and migration notes.
17. Do not silently change astrology rules or calculation conventions.
18. Traditional interpretations must be distinguishable from deterministic calculated facts.
19. Never use fear, false certainty, or manipulative engagement as a product mechanism.
20. If an existing abstraction is insufficient, extend it before creating a parallel abstraction.
21. Tests are required for deterministic domain logic and security-critical behavior.
22. Documentation must be updated when architecture, public APIs, security rules, or design tokens change.
23. Never claim a build, test, platform, or security control is verified unless it was actually verified.
24. Preserve backward compatibility where contractually required by the SDK.
25. When requirements conflict, follow: security/privacy → architecture → Style Guide → feature requirement → convenience.

## Phase 7.0 Permanent Governance Rules (Clean Architecture + Analytics + Offline-First)

26. **Clean Architecture is mandatory for every current and future AYNVORA feature.**
    Dependencies must point inward: Presentation → Domain ← Data.
    Domain (`:aynvora-core`, `:astro-engine`) must remain independent of Android, iOS, Compose, Room, Firebase, and all platform APIs.
    Repository interfaces belong in Domain. Repository implementations belong in Data.
    Platform-specific code must be isolated behind platform interfaces.

27. **Firebase Analytics is mandatory for all applicable current and future AYNVORA user-facing functionality.**
    New phases must audit existing analytics infrastructure and integrate meaningful, privacy-safe analytics
    without introducing Firebase dependencies into Domain or the deterministic astrology engine.
    Analytics must be decoupled via `AnalyticsTracker` and `AnalyticsEvent` abstractions in `:aynvora-core`.
    Firebase SDK imports are confined to `androidApp` only.

28. **The deterministic astrology engine must remain completely analytics-independent.**
    `:astro-engine` must have zero dependency on Firebase, analytics, or platform APIs.
    Analytics must never affect calculation inputs, outputs, precision, determinism, or performance.
    The identical calculation result must be produced whether analytics is enabled, disabled, or unavailable.

29. **Offline-first architecture is mandatory.**
    All applicable AYNVORA features must operate completely without network connectivity.
    Room KMP is the local source of truth for content, sync metadata, and structured data.
    Network is used only for content synchronization — never as a direct UI data source.
    A network failure must never destroy or corrupt existing local data.

30. **Content updates must be validated before committing to the local database.**
    Sync operations must be transactional. Partial or unverified content must never replace verified local content.
    A `ContentVerifier` implementation (not a stub) is required before any production content release.
    Signing keys must not be committed to source control.

31. **Analytics consent must be obtained before any analytics data is collected.**
    Default consent state is PENDING — analytics must be suppressed until the user grants consent.
    `ConsentAwareAnalyticsTracker` must gate all analytics calls behind `AnalyticsConsentManager`.
    Firebase must not be assumed available before initialization and consent are satisfied.

## Change Rule
If a requested feature conflicts with a rule, stop and document the conflict before implementation.
