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

## Change Rule
If a requested feature conflicts with a rule, stop and document the conflict before implementation.
