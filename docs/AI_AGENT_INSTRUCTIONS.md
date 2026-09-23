# AYNVORA AI/Codex Instructions
Version: 1.0

Before changing code:
1. Read `00_MASTER_RULES.md`.
2. Read `docs/31_DOC_INDEX.md`.
3. Read every domain document relevant to the requested change.
4. Search for reusable code/components before creating new ones.
5. Inspect existing architecture and tests.
6. Do not make unrelated refactors.

For UI tasks read:
- 05_DESIGN_SYSTEM.md
- 06_UI_STYLE_GUIDE.md
- 07_COMPONENT_LIBRARY.md
- 22_ACCESSIBILITY.md

For astrology tasks read:
- 08_ASTRO_ENGINE.md
- 09_ASTROLOGY_RULES.md
- 20_TESTING.md

For security tasks read:
- 12_SECURITY.md
- 13_PRIVACY.md
- 14_AUTHENTICATION.md
- 28_THREAT_MODEL.md
- 29_SECURITY_CHECKLIST.md

For data/offline tasks read:
- 10_DATA_ARCHITECTURE.md
- 11_OFFLINE_FIRST.md

Never:
- invent calculations
- invent UI styling
- add duplicate functionality
- expose secrets
- claim unverified success
- silently change domain rules

At the end of every task report:
- files changed
- reusable code used
- tests/builds run
- security/privacy considerations
- remaining limitations
