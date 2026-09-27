# AYNVORA Master Rules
Version: 1.0

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
26. Every user-triggered business interaction must emit a typed event (`AynvoraEvent`).
27. UI must not perform business logic directly.
28. UI must not directly perform navigation for business flows; navigation must be emitted as a
    typed one-shot effect (`AynvoraEffect.Navigate`).
29. UI must not directly call analytics for business interactions when the event pipeline can
    observe the event.
30. ViewModels receive events (`onEvent`) and privately handle them (`private handleEvent`).
31. Event payloads must be strictly typed (`AynvoraEventPayload`); `Map<String, Any>` is prohibited
    as the primary event contract.
32. Event payloads must contain only the minimum required data.
33. Sensitive data (passwords, tokens, birth data, private questions, AI prompt/output, raw image
    bytes) must never be automatically included in events or analytics.
34. Events must be validated (`AynvoraEventGuard`) before execution.
35. Critical actions must be idempotent/deduplicated (`AynvoraEventDeduplicator`) where appropriate.
36. Analytics failure must never break business behavior; tracking failures must be isolated.
37. Persist only domain events that have a legitimate business purpose; ephemeral UI clicks must
    remain in-memory.
38. State is for persistent observable UI state; Effect is for one-shot commands (navigation,
    dialogs, sheets, snacks).
39. AI cannot bypass the event/domain security layer.
40. Composables are strictly prohibited from directly mutating repositories or data sources; all
    business mutations must be routed via typed events (`AynvoraEvent`) to ViewModel private
    handlers. Direct business-action analytics from Composables is prohibited (must use
    observer-only analytics bridge).
41. Key-driven dynamic localization (`LocalizationProvider`, `LocalAynvoraTranslator`,
    `TranslationKey`) is strictly mandatory for all user-facing text. Direct language branching (
    `isHindi`, `if (language == "hi")`, `when (locale)`) is forbidden. Offline-first bundled
    catalogs with deterministic fallback (`requested -> fallback -> English -> key`) must be
    preserved without exposing user data to analytics.
42. Multi-Language Localization Matrix (Phase 9.4). All 11 canonical languages (English, Hindi,
    Arabic, Bengali, Gujarati, Marathi, Punjabi, Tamil, Telugu, Kannada, Malayalam) must adhere to
    the single canonical 723 TranslationKey contract with 100% placeholder parity. Adding a new
    language requires translation data only, never modifying feature code, ViewModels, or
    astro-engine. Arabic must enforce RTL text direction. Report and PDF generation must resolve
    strings strictly through the centralized LocalizationProvider.
43. Numerology Domain Architecture & Determinism Rule (Phase 10.0). Numerology is an independent,
    pure domain engine in `:aynvora-core`, completely decoupled from `:astro-engine`, Compose, Room,
    Firebase, Android, and iOS. All calculations must be source-gated with explicit
    `NumerologyRuleset` declarations (`CHALDEAN_CHEIRO_V1`, `PYTHAGOREAN_WESTERN_V1`). Inventing
    rules or silently mixing traditions is strictly prohibited. The engine returns structured
    numerical domain models and immutable calculation traces (`NumerologyCalculationTrace`), never
    preformatted language strings. AI/SLM layers may consume structured `EvidenceGraph` nodes for
    explanation but must never recalculate, override, or invent numbers. Analytics events must
    remain observer-only and strictly exclude PII (no names, no birth dates/times, no private
    texts).

## Change Rule
If a requested feature conflicts with a rule, stop and document the conflict before implementation.
